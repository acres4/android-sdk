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
import com.acres.ble.util.getCharacteristic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import no.nordicsemi.android.ble.ktx.state.ConnectionState
import no.nordicsemi.android.ble.ktx.stateAsFlow
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat
import no.nordicsemi.android.support.v18.scanner.ScanResult
import java.util.UUID

class SlotAndTableDeviceManager(context: Context, private val logger: BleLogger) :
    BaseDeviceManager(
        context,
        Dispatchers.IO,
        Dispatchers.Default,
        BluetoothLeScannerCompat.getScanner(),
        logger
    ) {

    private var cancelCharacteristic: BluetoothGattCharacteristic? = null
    private var amountCharacteristic: BluetoothGattCharacteristic? = null

    private val _slotAndTableStateFlow =
        MutableStateFlow<SlotAndTableReaderState>(SlotAndTableReaderState.Scanning)
    val slotAndTableStateFlow: StateFlow<SlotAndTableReaderState> = _slotAndTableStateFlow

    init {
        deviceManagerScope.launch {
            val scannerJob = launch { startScanFlow() }
            val connectionStateJob = launch {
                stateAsFlow().collect {
                    logger.logDebug("device manager connection state:$it")
                    if (it == ConnectionState.Ready) {
                        bluetoothDevice?.let { device ->
                            _slotAndTableStateFlow.value = SlotAndTableReaderState.DeviceConnected(device)
                            logger.logDebug("device manager checking device status")
                            _slotAndTableStateFlow.value = SlotAndTableReaderState.DeviceAvailable
                        }

                        logger.logDebug("device with address:${bluetoothDevice?.address} is ready")
                        //                } else if (it is ConnectionState.Disconnected) {
                        //                    logger.logDebug("scanner started:$it")
                        //                    startScanFlow()
                        //                }
                    }
                }
            }
            listOf(scannerJob, connectionStateJob).joinAll()
        }
    }

    suspend fun fundTable(amount: Int) {
        logger.logDebug("Fund table with: $amount")
        writeRequest(amountCharacteristic, byteArrayOf(amount.toByte()))
    }

    suspend fun cashOut() {
        logger.logDebug("Cash out $$$")
        fundTable(0)
    }

    suspend fun cancelCashOut() {
        logger.logDebug("Cancel cash out")
        writeRequest(cancelCharacteristic, byteArrayOf(1.toByte()))
    }

    override fun setCommonCharacteristics(gatt: BluetoothGatt) {

        cancelCharacteristic = gatt.getCharacteristic(SlotAndTableCharacteristics.CANCEL_UUID)
        amountCharacteristic = gatt.getCharacteristic(SlotAndTableCharacteristics.AMOUNT_UUID)
    }

    override fun clearCharacteristics() {
        cancelCharacteristic = null
        amountCharacteristic = null
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
            _slotAndTableStateFlow.value = SlotAndTableReaderState.DiscoveredDevice(it, device.rssi)
            logger.logDebug("connecting to device:$device ")
            stopScan()
            deviceManagerScope.launch { connectDevice(it) }
        }
    }

    companion object {
        const val MINIMUM_RSSI = -65
    }
}
