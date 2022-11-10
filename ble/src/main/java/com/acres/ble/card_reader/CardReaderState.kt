/*
 * Copyright © 2022 ACRES. All rights reserved.
 * Created by Acres developer.
 */
package com.acres.ble.card_reader

import android.bluetooth.BluetoothDevice

sealed class CardReaderState {

    object Scanning : CardReaderState()
    data class DiscoveredDevice(val result: BluetoothDevice, val rssi: Int) : CardReaderState()
    data class DeviceConnected(val device: BluetoothDevice) : CardReaderState()
    data class DeviceError(val exception: Exception) : CardReaderState()
    object DeviceBusy : CardReaderState()
    object DeviceAvailable : CardReaderState()
    object DeviceDisconnected : CardReaderState()
}
