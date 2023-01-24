/*
 * Copyright © 2022 ACRES. All rights reserved.
 * Created by Acres developer.
 */
package com.acres.ble.slot_and_table

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.os.ParcelUuid
import com.acres.ble.core.BaseDeviceManager
import com.acres.ble.core.BleLogger
import com.acres.ble.core.model.BleScannerError
import com.acres.ble.util.getCharacteristic
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import no.nordicsemi.android.ble.ktx.state.ConnectionState
import no.nordicsemi.android.ble.ktx.stateAsFlow
import no.nordicsemi.android.ble.ktx.suspend
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat
import no.nordicsemi.android.support.v18.scanner.ScanResult
import java.util.Base64
import java.util.UUID
import kotlin.Exception

class SlotAndTableDeviceManager(context: Context, private val logger: BleLogger) :
    BaseDeviceManager(
        context,
        Dispatchers.IO,
        Dispatchers.Default,
        BluetoothLeScannerCompat.getScanner(),
        logger
    ) {

    private var sasSerialCharacteristics: BluetoothGattCharacteristic? = null
    private var cancelCharacteristic: BluetoothGattCharacteristic? = null
    private var amountCharacteristic: BluetoothGattCharacteristic? = null

    private val _slotAndTableStateFlow =
        MutableStateFlow<SlotAndTableReaderState>(SlotAndTableReaderState.Scanning)
    val slotAndTableStateFlow: StateFlow<SlotAndTableReaderState> =
        _slotAndTableStateFlow.asStateFlow()

    init {
        deviceManagerScope.launch {
            val scannerJob = launch { startScanFlow() }
            val connectionStateJob = launch {
                stateAsFlow().collect {
                    logger.logDebug("device manager connection state:$it")
                    if (it == ConnectionState.Ready) {
                        bluetoothDevice?.let { device ->
                            val sas = readSasSerial()
                            _slotAndTableStateFlow.value = SlotAndTableReaderState.DeviceAvailable(device, sas)
                            logger.logDebug(
                                "device with address: ${bluetoothDevice?.address}, sas: $sas is ready"
                            )
                        }
                    } else if (it is ConnectionState.Disconnected) {
                        _slotAndTableStateFlow.value = SlotAndTableReaderState.DeviceDisconnected(it.reason)
                    }
                }
            }
            listOf(scannerJob, connectionStateJob).joinAll()
        }
    }

    /**
     * Used to fund table by writing to amountCharacteristic with the specified amount
     *
     * @param amount desired amount to be funded
     */
    suspend fun fundTable(amount: Int) {
        writeRequest(amountCharacteristic, Base64.getEncoder().encode(amount.toString().toByteArray()))
    }

    /** Writes zero to amountCharacteristic in order to request cash out from dealer */
    suspend fun cashOut() {
        fundTable(0)
    }

    /**
     * Used to reject cash out amount suggested by the dealer by writing zero to cancelCharacteristic
     */
    suspend fun cancel() {
        writeRequest(cancelCharacteristic, Base64.getEncoder().encode(0.toString().toByteArray()))
    }

    override fun setCommonCharacteristics(gatt: BluetoothGatt) {

        cancelCharacteristic = gatt.getCharacteristic(SlotAndTableCharacteristics.CANCEL_UUID)
        amountCharacteristic = gatt.getCharacteristic(SlotAndTableCharacteristics.AMOUNT_UUID)
        sasSerialCharacteristics = gatt.getCharacteristic(SlotAndTableCharacteristics.SERIAL_UUID)

        var amount: String? = ""
        var sas: String? = ""

        deviceManagerScope.launch {
            setNotificationCallback(amountCharacteristic).with { _, data ->
                amount = data.getStringValue(0)
                when (amount) {
                    "-1" ->
                        _slotAndTableStateFlow.value =
                            SlotAndTableReaderState.DeviceError(Exception("Cash out failed"))
                    null ->
                        _slotAndTableStateFlow.value =
                            SlotAndTableReaderState.DeviceError(Exception("Unknown error"))
                    else ->
                        _slotAndTableStateFlow.value =
                            SlotAndTableReaderState.Success(amount = amount ?: "", sas = sas ?: "")
                }
                logger.logDebug("amount notification data: $amount")
            }
            enableNotifications(amountCharacteristic).suspend()

            setNotificationCallback(sasSerialCharacteristics).with { _, data ->
                sas = data.getStringValue(0)
                when (sas) {
                    "-1" ->
                        _slotAndTableStateFlow.value =
                            SlotAndTableReaderState.DeviceError(Exception("Fund failed"))
                    "table" ->
                        _slotAndTableStateFlow.value =
                            SlotAndTableReaderState.Success(amount = amount ?: "", sas = sas ?: "")
                    null ->
                        _slotAndTableStateFlow.value =
                            SlotAndTableReaderState.DeviceError(Exception("Unknown error"))
                }
                logger.logDebug("sas serial notification data: $sas")
            }
            enableNotifications(sasSerialCharacteristics).suspend()
        }
    }

    override fun clearCharacteristics() {
        cancelCharacteristic = null
        amountCharacteristic = null
        sasSerialCharacteristics = null
    }

    override fun handleScannedDevices(result: List<ScanResult>) {
        logger.logDebug(result.size.toString())

        val device =
            result.sortedByDescending { it.rssi }.filter { it.rssi >= MINIMUM_RSSI }.firstOrNull { scan
                ->
                scan.scanRecord?.serviceUuids?.any {
                    it == ParcelUuid(UUID.fromString("c83fe52e-0ab5-49d9-9817-98982b4c48a3"))
                } == true
            }
        logger.logDebug("scanned devices found acres device:$device ")

        device?.device?.let {
            _slotAndTableStateFlow.value = SlotAndTableReaderState.DiscoveredDevice(it, device.rssi)
            logger.logDebug("connecting to device:$device ")
            stopScan()
            deviceManagerScope.launch { connectDevice(it) }
        }
    }

    override fun handleScannerError(error: BleScannerError, message: String?) {
        _slotAndTableStateFlow.value = SlotAndTableReaderState.ScannerError(error, message)
    }

    override fun handleException(e: Exception) {
        _slotAndTableStateFlow.value = SlotAndTableReaderState.DeviceError(e)
    }

    private suspend fun readSasSerial(): String =
        readRequest(sasSerialCharacteristics) { data -> data.getStringValue(0) } ?: ""

    companion object {
        const val MINIMUM_RSSI = -65
    }
}

/**
 * ScanResult{device=51:20:08:74:5B:74, scanRecord=ScanRecord [advertiseFlags=26,
 * serviceUuids=[c83fe52e-0ab5-49d9-9817-98982b4c48a3], manufacturerSpecificData=null,
 * serviceData=null, txPowerLevel=12, deviceName=Dealer Application], rssi=-26,
 * timestampNanos=77752478528782, eventType=17, primaryPhy=1, secondaryPhy=0}
 */
