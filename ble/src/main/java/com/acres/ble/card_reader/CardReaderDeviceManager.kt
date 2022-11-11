/*
 * Copyright © 2022 ACRES. All rights reserved.
 * Created by Acres developer.
 */
package com.acres.ble.card_reader

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.os.ParcelUuid
import com.acres.ble.core.BaseDeviceManager
import com.acres.ble.core.BleLogger
import com.acres.ble.util.getCharacteristic
import com.acres.ble.util.toBoolean
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import no.nordicsemi.android.ble.ktx.state.ConnectionState
import no.nordicsemi.android.ble.ktx.stateAsFlow
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat
import no.nordicsemi.android.support.v18.scanner.ScanResult
import java.io.IOException
import java.util.UUID

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

    private val _cardReaderStateFlow = MutableStateFlow<CardReaderState>(CardReaderState.Scanning)
    val cardReaderStateFlow: StateFlow<CardReaderState> = _cardReaderStateFlow

    fun insertPlayerCard(selectedTrack: Track, userId: String) {
        try {
            deviceManagerScope.launch {
                val scannerJob = launch { startScanFlow() }
                val connectionStateJob = launch {
                    stateAsFlow().collect {
                        logger.logDebug("device manager connection state:$it")
                        if (it == ConnectionState.Ready) {
                            bluetoothDevice?.let { device ->
                                _cardReaderStateFlow.value = CardReaderState.DeviceConnected(device)
                                logger.logDebug("device manager checking device status")
                                if (isDeviceBusy()) {
                                    _cardReaderStateFlow.value = CardReaderState.DeviceBusy
                                } else {
                                    _cardReaderStateFlow.value = CardReaderState.DeviceAvailable(device)
                                    handleWriteToPlayerCardCharacteristics(selectedTrack, userId)
                                }
                            }

                            logger.logDebug("device with address:${bluetoothDevice?.address} is ready")
                            //                } else if (it is ConnectionState.Disconnected) {
                            //                    logger.logDebug("scanner started:$it")
                            //                    startScanFlow()
                            //                }
                        } else if (it == ConnectionState.Disconnecting) {
                            _cardReaderStateFlow.value = CardReaderState.DeviceDisconnected
                        }
                    }
                }
                listOf(scannerJob, connectionStateJob).joinAll()
            }
        } catch (e: Exception) {
            _cardReaderStateFlow.value = CardReaderState.DeviceError(e)
        }
    }

    private suspend fun handleWriteToPlayerCardCharacteristics(selectedTrack: Track, userId: String) {
        when (selectedTrack) {
            Track.TRACK_1 ->
                if (userId.toByteArray().size <= TRACK_1_MAX_BYTE_LENGTH) writePlayerCardTrack1(userId)
                else
                    throw IOException(
                        "Cannot write more than $TRACK_1_MAX_BYTE_LENGTH bytes to the ${playerCardTrack1Characteristics?.uuid} characteristic"
                    )
            Track.TRACK_2 ->
                if (userId.toByteArray().size <= TRACK_2_MAX_BYTE_LENGTH) writePlayerCardTrack2(userId)
                else
                    throw IOException(
                        "Cannot write more than $TRACK_2_MAX_BYTE_LENGTH bytes to the ${playerCardTrack2Characteristics?.uuid} characteristic"
                    )
        }
    }

    private suspend fun writePlayerCardTrack1(userId: String) {
        writeRequest(playerCardTrack1Characteristics, userId.toByteArray())
    }

    private suspend fun writePlayerCardTrack2(userId: String) {
        writeRequest(playerCardTrack2Characteristics, userId.toByteArray())
    }

    private suspend fun isDeviceBusy(): Boolean =
        readRequest(playerCardStatusCharacteristics) {
            logger.logDebug("isDeviceBusy:${it.getByte(0)?.toBoolean()}")
            it.getByte(0)?.toBoolean() ?: true
        } == true

    override fun setCommonCharacteristics(gatt: BluetoothGatt) {

        playerCardStatusCharacteristics =
            gatt.getCharacteristic(CardReaderCharacteristics.PLAYER_CARD_STATUS_UUID)
        playerCardTrack1Characteristics =
            gatt.getCharacteristic(CardReaderCharacteristics.PLAYER_CARD_TRACK1_UUID)
        playerCardTrack2Characteristics =
            gatt.getCharacteristic(CardReaderCharacteristics.PLAYER_CARD_TRACK2_UUID)
        playerCardInsertCharacteristics =
            gatt.getCharacteristic(CardReaderCharacteristics.PLAYER_CARD_INSERT_UUID)
    }

    override fun clearCharacteristics() {
        playerCardStatusCharacteristics = null
        playerCardTrack1Characteristics = null
        playerCardTrack2Characteristics = null
        playerCardInsertCharacteristics = null
    }

    override fun handleScannedDevices(result: List<ScanResult>) {
        val device =
            result.sortedByDescending { it.rssi }.filter { it.rssi >= MINIMUM_RSSI }.firstOrNull { scan
                ->
                scan.scanRecord?.serviceUuids?.any {
                    it == ParcelUuid(UUID.fromString("c83fe52e-0ab5-49d9-9817-98982b4c48a3"))
                } == true
            }
        logger.logDebug("scanned devices found acres device:$device ")

        device?.device?.let {
            _cardReaderStateFlow.value = CardReaderState.DiscoveredDevice(it, device.rssi)
            logger.logDebug("connecting to device:$device ")
            stopScan()
            deviceManagerScope.launch { connectDevice(it) }
        }
    }

    companion object {
        const val MINIMUM_RSSI = -65
        const val TRACK_1_MAX_BYTE_LENGTH = 79
        const val TRACK_2_MAX_BYTE_LENGTH = 40
    }
}
