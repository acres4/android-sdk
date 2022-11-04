/*
 * Copyright © 2022 ACRES. All rights reserved.
 * Created by Acres developer.
 */
package com.acres.ble.slot_and_table

import android.bluetooth.BluetoothDevice

sealed class SlotAndTableReaderState {

    object Scanning : SlotAndTableReaderState()
    data class DiscoveredDevice(val result: BluetoothDevice, val rssi: Int) :
        SlotAndTableReaderState()
    data class DeviceConnected(val device: BluetoothDevice) : SlotAndTableReaderState()
    data class DeviceError(val exception: Exception) : SlotAndTableReaderState()
    object DeviceAvailable : SlotAndTableReaderState()
}
