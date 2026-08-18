/*
 * Copyright © 2022 ACRES. All rights reserved.
 * Created by Acres developer.
 */
package com.acres.blesdk.ble.logger

import com.acres.ble.core.BleLogger
import timber.log.Timber

class BleLogger : BleLogger {
    override fun logDebug(message: String) {
        Timber.d("ble lib log message:$message")
    }

    override fun logError(message: String, error: Exception?) {
        Timber.e("error from ble lib message:$message, error:$error")
    }
}
