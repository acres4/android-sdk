/*
 * Copyright © 2022 ACRES. All rights reserved.
 * Created by Acres developer.
 */
package com.acres.ble.core

import android.bluetooth.BluetoothGatt
import android.content.Context
import kotlinx.coroutines.CoroutineDispatcher
import no.nordicsemi.android.ble.BleManager

abstract class BaseDeviceManager
constructor(
    context: Context,
    private val ioDispatcher: CoroutineDispatcher,
    private val computationDispatcher: CoroutineDispatcher,
) : BleManager(context) {

    override fun getGattCallback(): BleManagerGattCallback = AcresBleManagerGattCallback()

    private inner class AcresBleManagerGattCallback : BleManagerGattCallback() {
        override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
            setCommonCharacteristics(gatt)
            return true
        }

        override fun onServicesInvalidated() {
            clearCharacteristics()
        }
    }

    abstract fun setCommonCharacteristics(
        gatt: BluetoothGatt,
    )

    abstract fun clearCharacteristics()
}
