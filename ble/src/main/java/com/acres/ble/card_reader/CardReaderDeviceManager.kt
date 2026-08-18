/*
 * Copyright © 2022 ACRES. All rights reserved.
 * Created by Acres developer.
 */
package com.acres.ble.card_reader

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.os.ParcelUuid
import com.acres.ble.core.BaseDeviceManager
import com.acres.ble.core.BleLogger
import com.acres.ble.core.model.BleScannerError
import com.acres.ble.util.getCharacteristic
import com.acres.ble.util.toHexString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
import no.nordicsemi.android.ble.ktx.state.ConnectionState
import no.nordicsemi.android.ble.ktx.stateAsFlow
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat
import no.nordicsemi.android.support.v18.scanner.ScanResult
import java.io.IOException
import java.util.UUID

/** Track (characteristic) to be written to. */
enum class Track {
    TRACK_1,
    TRACK_2
}

class CardReaderDeviceManager(context: Context, private val logger: BleLogger) :
    BaseDeviceManager(
        context,
        Dispatchers.IO,
        Dispatchers.Default,
        BluetoothLeScannerCompat.getScanner(),
        logger
    ) {

    private var playerCardStatusCharacteristics: BluetoothGattCharacteristic? = null
    private var playerCardTrack1Characteristics: BluetoothGattCharacteristic? = null
    private var playerCardTrack2Characteristics: BluetoothGattCharacteristic? = null
    private var playerCardInsertCharacteristics: BluetoothGattCharacteristic? = null
    private var sasSerialCharacteristics: BluetoothGattCharacteristic? = null

    private val _cardReaderStateFlow = MutableStateFlow<CardReaderState>(CardReaderState.Scanning)
    val cardReaderStateFlow: StateFlow<CardReaderState> = _cardReaderStateFlow

    // Guards every public operation so the second call waits for the first to finish.
    // Without this, two stateAsFlow collectors raced on the same Ready emission and one
    // disconnected mid-write, nulling out the cached characteristics.
    private val operationMutex = Mutex()

    private val deviceMap = HashMap<String, Int>()

    private var scannerJob: Job? = null

    init {
        // Persistent watcher that surfaces every disconnect — both consumer-initiated
        // (via the inherited disconnectDevice()) and unexpected (EGM out of range, etc.).
        // insertPlayerCard / removePlayerCard intentionally do NOT disconnect on
        // success; the consumer must call disconnectDevice() when done with the EGM.
        deviceManagerScope.launch {
            stateAsFlow().collect { state ->
                if (state is ConnectionState.Disconnected) {
                    _cardReaderStateFlow.value = CardReaderState.DeviceDisconnected
                }
            }
        }
    }

    /**
     * Cards a player into an EGM. Scans for an Acres device with RSSI >= [MINIMUM_RSSI], connects,
     * reads the player-card-busy characteristic, and on a non-busy device writes the user id to the
     * chosen track and then flips the insert characteristic to 1. Stays connected on success — the
     * consumer must call the inherited [disconnectDevice] when done with the EGM, otherwise the open
     * connection keeps the EGM locked to this phone.
     *
     * @param selectedTrack track (characteristic) to write to.
     * @param userId user identification number; <= 79 bytes for TRACK_1, <= 40 for TRACK_2.
     */
    suspend fun insertPlayerCard(selectedTrack: Track, userId: String) {
        operationMutex.withLock {
            try {
                logger.logDebug("insertPlayerCard: start track=$selectedTrack idLen=${userId.length}")
                val device = ensureConnected()
                logger.logDebug("insertPlayerCard: connected to ${device.address}")
                _cardReaderStateFlow.value = CardReaderState.DeviceConnected(device)

                // Settle time between GATT operations. Some EGMs report discovery
                // complete before their GATT server will actually serve the
                // player-presence characteristics, which shows up as a clean
                // connect followed by track/insert writes that never take.
                val busy = isDeviceBusy(selectedTrack, userId)
                logger.logDebug("insertPlayerCard: isDeviceBusy=$busy")
                if (busy) {
                    writePlayerCardRemoved()
                    _cardReaderStateFlow.value = CardReaderState.DeviceBusy
                }
                _cardReaderStateFlow.value = CardReaderState.DeviceAvailable(device)

                val data = userId.toByteArray()
                val (limit, characteristic) =
                    when (selectedTrack) {
                        Track.TRACK_1 -> TRACK_1_MAX_BYTE_LENGTH to playerCardTrack1Characteristics
                        Track.TRACK_2 -> TRACK_2_MAX_BYTE_LENGTH to playerCardTrack2Characteristics
                    }
                if (data.size > limit) {
                    throw IOException("Cannot write more than $limit bytes to ${characteristic?.uuid}")
                }

                logger.logDebug("insertPlayerCard: writing ${data.size}B to track ${characteristic?.uuid}")
                val trackWritten = writeRequest(characteristic, data)
                logger.logDebug("insertPlayerCard: track write success=$trackWritten")
                if (!trackWritten) return

                val insertWritten = writePlayerCardInserted()
                logger.logDebug("insertPlayerCard: insert write success=$insertWritten")
                if (!insertWritten) return

                logger.logDebug("insertPlayerCard: CardInserted")
                _cardReaderStateFlow.value = CardReaderState.CardInserted
            } catch (_: TimeoutCancellationException) {
                logger.logDebug("insertPlayerCard: scan timeout")
                _cardReaderStateFlow.value = CardReaderState.ScanTimeout
            } catch (e: Exception) {
                logger.logError("insertPlayerCard failed", e)
                _cardReaderStateFlow.value = CardReaderState.DeviceError(e)
            }
        }
    }

    /**
     * Cards a player out of an EGM by writing 0 to the insert characteristic. Reuses an existing
     * connection if present, otherwise scans/connects. Stays connected on success — call
     * [disconnectDevice] when done.
     */
    suspend fun removePlayerCard() {
        operationMutex.withLock {
            try {
                ensureConnected()
                if (!writePlayerCardRemoved()) return
                _cardReaderStateFlow.value = CardReaderState.CardRemoved
            } catch (_: TimeoutCancellationException) {
                _cardReaderStateFlow.value = CardReaderState.ScanTimeout
            } catch (e: Exception) {
                _cardReaderStateFlow.value = CardReaderState.DeviceError(e)
            }
        }
    }

    /** Connects to the nearest Acres device and reads its SAS serial. Stays connected on success. */
    suspend fun findDevice() {
        operationMutex.withLock {
            try {
                val device = ensureConnected()
                val sas = readSasSerial()
                _cardReaderStateFlow.value = CardReaderState.DeviceFound(device, sas)
            } catch (_: TimeoutCancellationException) {
                _cardReaderStateFlow.value = CardReaderState.ScanTimeout
            } catch (e: Exception) {
                _cardReaderStateFlow.value = CardReaderState.DeviceError(e)
            }
        }
    }

    // Returns when connection state is Ready, scanning first if not already connected.
    // .first (one-shot) replaces an indefinite collect so no stale collector survives the call.
    private suspend fun ensureConnected(): BluetoothDevice =
        withTimeout(SCAN_TIMEOUT_MS) {
            if (!isConnected) {
                deviceMap.clear()
                scannerJob?.cancel()
                _cardReaderStateFlow.value = CardReaderState.Scanning
                scannerJob = deviceManagerScope.launch { startScanFlow() }
            }
            try {
                stateAsFlow().first { it == ConnectionState.Ready }
            } finally {
                scannerJob?.cancel()
                scannerJob = null
                stopScan()
            }
            bluetoothDevice
                ?: throw IllegalStateException("connection ready but bluetoothDevice is null")
        }

    private suspend fun writePlayerCardInserted(): Boolean =
        writeRequest(playerCardInsertCharacteristics, byteArrayOf(1))

    private suspend fun writePlayerCardRemoved(): Boolean {
        var result = writeRequest(playerCardInsertCharacteristics, byteArrayOf(0))
        delay(CARD_REMOVAL_SETTLE_MS)
        return result
    }

    private suspend fun isDeviceBusy(selectedTrack: Track, userId: String): Boolean {
        val occupied =
            readRequest(playerCardInsertCharacteristics) { data ->
                // Firmware writes 1 when a card is currently inserted (device busy), 0 when free.
                // Treat a missing byte as busy to be conservative.
                val raw = data.getByte(0)
                logger.logDebug("isDeviceBusy raw byte:$raw")
                raw == null || raw == 0x01.toByte()
            } == true

        if (!occupied) return false

        // Occupied — but it may be this player's own stale session, which we should
        // rewrite rather than clear.
        val expected = userId.toByteArray()
        val characteristic =
            when (selectedTrack) {
                Track.TRACK_1 -> playerCardTrack1Characteristics
                Track.TRACK_2 -> playerCardTrack2Characteristics
            }
        val stored = readRequest(characteristic) { data -> data.value }
        if (stored == null) {
            // Either the read failed or the characteristic is write-only. Either way
            // we cannot claim the session, so treat it as somebody else's.
            logger.logDebug("current track: <unreadable>; treating as another session")
            return true
        }

        logger.logDebug("current track:  ${stored.toHexString()} \"${String(stored)}\"")
        logger.logDebug("expected track: ${expected.toHexString()} \"${String(expected)}\"")
        val ours = tracksMatch(stored, expected)
        logger.logDebug("track belongs to this player=$ours")
        return !ours
    }

    /**
     * True when the track already on the EGM is this player's.
     *
     * The stored value rarely comes back byte-identical to what was written: EGMs pad the buffer to
     * the characteristic's fixed width, and some return only the portion they kept. So compare with
     * padding trimmed, and accept either value being a prefix of the other.
     */
    private fun tracksMatch(stored: ByteArray, expected: ByteArray): Boolean {
        val a = stored.trimTrackPadding()
        val b = expected.trimTrackPadding()
        if (a.isEmpty() || b.isEmpty()) return false
        return when {
            a.contentEquals(b) -> true
            a.size > b.size -> a.copyOfRange(0, b.size).contentEquals(b)
            else -> b.copyOfRange(0, a.size).contentEquals(a)
        }
    }

    private fun BluetoothGattCharacteristic?.isReadable(): Boolean =
        this != null && (properties and BluetoothGattCharacteristic.PROPERTY_READ) > 0

    private fun ByteArray.trimTrackPadding(): ByteArray {
        val padding = setOf(0x00.toByte(), 0x20.toByte(), 0xFF.toByte())
        var start = 0
        var end = size
        while (start < end && this[start] in padding) start++
        while (end > start && this[end - 1] in padding) end--
        return copyOfRange(start, end)
    }

    private suspend fun readSasSerial(): String =
        readRequest(sasSerialCharacteristics) { data -> data.getStringValue(0) } ?: ""

    override fun setCommonCharacteristics(gatt: BluetoothGatt) {
        playerCardStatusCharacteristics =
            gatt.getCharacteristic(CardReaderCharacteristics.PLAYER_CARD_STATUS_UUID)
        playerCardTrack1Characteristics =
            gatt.getCharacteristic(CardReaderCharacteristics.PLAYER_CARD_TRACK1_UUID)
        playerCardTrack2Characteristics =
            gatt.getCharacteristic(CardReaderCharacteristics.PLAYER_CARD_TRACK2_UUID)
        playerCardInsertCharacteristics =
            gatt.getCharacteristic(CardReaderCharacteristics.PLAYER_CARD_INSERT_UUID)
        sasSerialCharacteristics = gatt.getCharacteristic(CardReaderCharacteristics.SERIAL_UUID)

        logger.logDebug("services: " + (gatt.services?.joinToString { it.uuid.toString() } ?: "none"))
        logger.logDebug(
            "characteristics resolved: status=${playerCardStatusCharacteristics != null}" +
                " track1=${playerCardTrack1Characteristics != null}" +
                " track2=${playerCardTrack2Characteristics != null}" +
                " insert=${playerCardInsertCharacteristics != null}" +
                " serial=${sasSerialCharacteristics != null}"
        )
        // The ownership check reads back the track it wrote. If the EGM exposes
        // that characteristic as write-only the read returns nothing and every
        // session looks like somebody else's.
        logger.logDebug(
            "readable: track1=${playerCardTrack1Characteristics.isReadable()}" +
                " track2=${playerCardTrack2Characteristics.isReadable()}" +
                " insert=${playerCardInsertCharacteristics.isReadable()}"
        )
    }

    override fun clearCharacteristics() {
        playerCardStatusCharacteristics = null
        playerCardTrack1Characteristics = null
        playerCardTrack2Characteristics = null
        playerCardInsertCharacteristics = null
        sasSerialCharacteristics = null
    }

    override fun handleScannerError(error: BleScannerError, message: String?) {
        _cardReaderStateFlow.value = CardReaderState.ScannerError(error, message)
    }

    override fun handleException(e: Exception) {
        logger.logError("device exception", e)
        _cardReaderStateFlow.value = CardReaderState.DeviceError(e)
    }

    override fun handleScanStarted() {
        logger.logDebug("scan started")
    }

    override fun handleScannedDevices(result: List<ScanResult>) {
        val device =
            result.sortedByDescending { it.rssi }.firstOrNull { scan ->
                scan.scanRecord?.serviceUuids?.any {
                    it == ParcelUuid(UUID.fromString(ACRES_SERVICE_UUID))
                } == true
            }

        val currentDeviceMac = device?.device?.address ?: ""
        // Count per device rather than clearing the map whenever a different one
        // wins a batch. Two EGMs at similar RSSI used to reset each other every
        // scan, so neither could ever reach REQUIRED_CONSECUTIVE_HITS.
        if (!deviceMap.containsKey(currentDeviceMac)) {
            deviceMap[currentDeviceMac] = 0
        }

        var currentCount = deviceMap[currentDeviceMac] ?: 0
        val rssi = device?.rssi ?: -99
        if (rssi >= MINIMUM_RSSI) {
            currentCount += 1
            deviceMap[currentDeviceMac] = currentCount
        } else if (rssi > -99) {
            deviceMap[currentDeviceMac] = 0
            currentCount = 0
        }

        if (currentCount > REQUIRED_CONSECUTIVE_HITS) {
            device?.device?.let {
                _cardReaderStateFlow.value = CardReaderState.DiscoveredDevice(it, device.rssi)
                logger.logDebug("connecting to device:$device ")
                stopScan()
                deviceManagerScope.launch { connectDevice(it) }
            }
        }
    }

    companion object {
        const val MINIMUM_RSSI = -50
        const val TRACK_1_MAX_BYTE_LENGTH = 79
        const val TRACK_2_MAX_BYTE_LENGTH = 40
        private const val REQUIRED_CONSECUTIVE_HITS = 6

        /** Settle time between GATT operations during card-in. Tune as needed. */
        private const val OPERATION_DELAY_MS = 500L

        /** Pause after clearing another session before writing ours. */
        private const val CARD_REMOVAL_SETTLE_MS = 2_000L
        // Covers scan + connect + service discovery. A successful card-in once
        // measured 9.6s of a 10s budget; with reportDelay now 0 the scan phase
        // is far quicker, so this is headroom rather than a hard requirement.
        private const val SCAN_TIMEOUT_MS = 15_000L
        private const val ACRES_SERVICE_UUID = "c83fe52e-0ab5-49d9-9817-98982b4c48a3"
    }
}
