/*
 * Copyright © 2022 ACRES. All rights reserved.
 * Created by Acres developer.
 */
package com.acres.ble.slot_and_table

import android.bluetooth.BluetoothDevice
import com.acres.ble.core.model.BleScannerError
import no.nordicsemi.android.ble.ktx.state.ConnectionState.Disconnected.Reason

sealed class SlotAndTableReaderState {

    data class None(val state: String) : SlotAndTableReaderState()
    object Scanning : SlotAndTableReaderState()
    data class Success(val amount: String, val sas: String) : SlotAndTableReaderState()
    data class DiscoveredDevice(val result: BluetoothDevice, val rssi: Int) :
        SlotAndTableReaderState()
    data class DeviceError(val exception: Exception) : SlotAndTableReaderState()
    data class ScannerError(val error: BleScannerError, val message: String?) :
        SlotAndTableReaderState()
    data class DeviceAvailable(val device: BluetoothDevice, val sasSerial: String?) :
        SlotAndTableReaderState()
    data class DeviceDisconnected(val reason: Reason) : SlotAndTableReaderState()
}
