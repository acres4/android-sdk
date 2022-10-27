/*
 * Copyright © 2022 ACRES. All rights reserved.
 * Created by Acres developer.
 */
package com.acres.blesdk.ble.logger

import com.acres.ble.core.BleLogger

class BleLogger : BleLogger {
    override fun logDebug(message: String) {}

    override fun logError(message: String, error: Exception?) {}
}
