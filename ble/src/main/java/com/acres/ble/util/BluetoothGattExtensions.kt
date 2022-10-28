/*
 * Copyright © 2020 ACRES. All rights reserved.
 * Created by Acres developer.
 */
package com.acres.ble.util

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import java.util.*

fun BluetoothGattCharacteristic?.isReadAvailable(): Boolean {
    if (this == null) return false
    val properties = this.properties
    return (properties and BluetoothGattCharacteristic.PROPERTY_READ) > 0
}

fun BluetoothGattCharacteristic?.isWriteAvailable(): Boolean {
    if (this == null) return false
    val properties = this.properties
    return (properties and BluetoothGattCharacteristic.PROPERTY_WRITE) > 0
}

fun BluetoothGattCharacteristic?.isNotifyAvailable(): Boolean {
    if (this == null) return false
    val properties = this.properties
    return (properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0
}

fun BluetoothGatt.getCharacteristic(uuid: UUID) =
    this.services?.flatMap { it.characteristics }?.firstOrNull { it.uuid == uuid }
