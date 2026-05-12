/*
 * Copyright © 2022 ACRES. All rights reserved.
 * Created by Acres developer.
 */
package com.acres.ble.card_reader

import android.bluetooth.BluetoothDevice
import com.acres.ble.core.model.BleScannerError

sealed class CardReaderState {

    object Scanning : CardReaderState()
    data class DiscoveredDevice(val result: BluetoothDevice, val rssi: Int) : CardReaderState()
    data class DeviceConnected(val device: BluetoothDevice) : CardReaderState()
    data class DeviceError(val exception: Exception) : CardReaderState()
    data class ScannerError(val error: BleScannerError, val message: String?) : CardReaderState()
    object DeviceBusy : CardReaderState()
    data class DeviceAvailable(val device: BluetoothDevice) : CardReaderState()
    data class DeviceFound(val device: BluetoothDevice, val sasSerial: String?) : CardReaderState()
    object DeviceDisconnected : CardReaderState()
    object CardInserted : CardReaderState()
    object CardRemoved : CardReaderState()
}
