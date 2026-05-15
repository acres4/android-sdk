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
import com.acres.ble.util.toBoolean
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
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
        // Single persistent watcher that surfaces unexpected/intentional disconnects as state.
        // Replaces the per-call disconnect handling that used to live in ad-hoc collectors.
        deviceManagerScope.launch {
            stateAsFlow().collect { state ->
                if (state is ConnectionState.Disconnected) {
                    _cardReaderStateFlow.value = CardReaderState.DeviceDisconnected
                }
            }
        }
    }

    /**
     * Cards a player into an EGM. Scans for an Acres device with RSSI >= -65, connects, reads the
     * player-card-busy characteristic, and on a non-busy device writes the user id to the chosen
     * track and then flips the insert characteristic to 1. Disconnects on success.
     *
     * Disconnect-after-success is deliberate: if the player walks away with the card still inserted
     * and the phone stays in BLE range, an open connection would keep the EGM locked. Diverges from
     * the iOS SDK on purpose.
     *
     * @param selectedTrack track (characteristic) to write to.
     * @param userId user identification number; <= 79 bytes for TRACK_1, <= 40 for TRACK_2.
     */
    suspend fun insertPlayerCard(selectedTrack: Track, userId: String) {
        operationMutex.withLock {
            try {
                val device = ensureConnected()
                _cardReaderStateFlow.value = CardReaderState.DeviceConnected(device)

                if (isDeviceBusy()) {
                    writePlayerCardRemoved()
                    safeDisconnect()
                    _cardReaderStateFlow.value = CardReaderState.DeviceBusy
                    return
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
                if (!writeRequest(characteristic, data)) return
                if (!writePlayerCardInserted()) return

                safeDisconnect()
                // Set after disconnect so the persistent disconnect watcher doesn't overwrite us.
                _cardReaderStateFlow.value = CardReaderState.CardInserted
            } catch (_: TimeoutCancellationException) {
                _cardReaderStateFlow.value = CardReaderState.ScanTimeout
                safeDisconnect()
            } catch (e: Exception) {
                _cardReaderStateFlow.value = CardReaderState.DeviceError(e)
                safeDisconnect()
            }
        }
    }

    /**
     * Cards a player out of an EGM by writing 0 to the insert characteristic. Scans/connects fresh
     * (insertPlayerCard already disconnected on success), then disconnects again after the write.
     */
    suspend fun removePlayerCard() {
        operationMutex.withLock {
            try {
                ensureConnected()
                if (!writePlayerCardRemoved()) return
                safeDisconnect()
                _cardReaderStateFlow.value = CardReaderState.CardRemoved
            } catch (_: TimeoutCancellationException) {
                _cardReaderStateFlow.value = CardReaderState.ScanTimeout
                safeDisconnect()
            } catch (e: Exception) {
                _cardReaderStateFlow.value = CardReaderState.DeviceError(e)
                safeDisconnect()
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
                safeDisconnect()
            } catch (e: Exception) {
                _cardReaderStateFlow.value = CardReaderState.DeviceError(e)
                safeDisconnect()
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

    private suspend fun safeDisconnect() {
        try {
            if (isConnected) disconnectDevice()
        } catch (e: Exception) {
            logger.logError("safeDisconnect failed", error = e)
        }
    }

    private suspend fun writePlayerCardInserted(): Boolean =
        writeRequest(playerCardInsertCharacteristics, byteArrayOf(1))

    private suspend fun writePlayerCardRemoved(): Boolean =
        writeRequest(playerCardInsertCharacteristics, byteArrayOf(0))

    private suspend fun isDeviceBusy(): Boolean =
        readRequest(playerCardStatusCharacteristics) {
            logger.logDebug("isDeviceBusy:${it.getByte(0)?.toBoolean()}")
            it.getByte(0)?.toBoolean() ?: true
        } == true

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
        if (!deviceMap.containsKey(currentDeviceMac)) {
            deviceMap.clear()
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
        const val MINIMUM_RSSI = -65
        const val TRACK_1_MAX_BYTE_LENGTH = 79
        const val TRACK_2_MAX_BYTE_LENGTH = 40
        private const val REQUIRED_CONSECUTIVE_HITS = 3
        private const val SCAN_TIMEOUT_MS = 10_000L
        private const val ACRES_SERVICE_UUID = "c83fe52e-0ab5-49d9-9817-98982b4c48a3"
    }
}
