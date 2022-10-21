/*
 * Copyright © 2022 ACRES. All rights reserved.
 * Created by Acres developer.
 */
package com.acres.ble.card_reader

import android.bluetooth.BluetoothGatt
import android.content.Context
import com.acres.ble.core.BaseDeviceManager
import com.acres.ble.core.BleLogger
import kotlinx.coroutines.Dispatchers
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat

class CardReaderDeviceManager(context: Context, logger: BleLogger) :
    BaseDeviceManager(
        context,
        Dispatchers.IO,
        Dispatchers.Default,
        BluetoothLeScannerCompat.getScanner(),
        logger
    ) {

    override fun setCommonCharacteristics(gatt: BluetoothGatt) {}

    override fun clearCharacteristics() {}
}
