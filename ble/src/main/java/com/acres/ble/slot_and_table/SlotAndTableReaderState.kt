/*
 * Copyright © 2022 ACRES. All rights reserved.
 * Created by Acres developer.
 */
package com.acres.ble.slot_and_table

import android.bluetooth.BluetoothDevice

sealed class SlotAndTableReaderState {

    object Scanning : SlotAndTableReaderState()
    data class Success(val amount: String) : SlotAndTableReaderState()
    data class DiscoveredDevice(val result: BluetoothDevice, val rssi: Int) :
        SlotAndTableReaderState()
    data class DeviceError(val exception: Exception) : SlotAndTableReaderState()
    data class DeviceAvailable(val device: BluetoothDevice) : SlotAndTableReaderState()
}
