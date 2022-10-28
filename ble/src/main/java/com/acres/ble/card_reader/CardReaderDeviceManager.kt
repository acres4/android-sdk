/*
 * Copyright © 2022 ACRES. All rights reserved.
 * Created by Acres developer.
 */
package com.acres.ble.card_reader

import android.bluetooth.BluetoothGatt
import android.content.Context
import android.os.ParcelUuid
import com.acres.ble.core.BaseDeviceManager
import com.acres.ble.core.BleLogger
import kotlinx.coroutines.Dispatchers
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat
import no.nordicsemi.android.support.v18.scanner.ScanResult
import java.util.UUID

class CardReaderDeviceManager(context: Context, private val logger: BleLogger) :
    BaseDeviceManager(
        context,
        Dispatchers.IO,
        Dispatchers.Default,
        BluetoothLeScannerCompat.getScanner(),
        logger
    ) {

    override fun setCommonCharacteristics(gatt: BluetoothGatt) {}

    override fun clearCharacteristics() {}
    override fun handleScannedDevices(result: List<ScanResult>) {
        logger.logDebug("scanned devices:$result ")
        val device =
            result.sortedByDescending { it.rssi }.firstOrNull { scan ->
                scan.scanRecord?.serviceUuids?.any {
                    it == ParcelUuid(UUID.fromString("c83fe52e-0ab5-49d9-9817-98982b4c48a3"))
                } == true
            }
        logger.logDebug("scanned devices found acres device:$device ")
    }
}
