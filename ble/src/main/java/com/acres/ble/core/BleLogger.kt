/*
 * Copyright © 2022 ACRES. All rights reserved.
 * Created by Acres developer.
 */
package com.acres.ble.core

interface BleLogger {

    fun logDebug(message: String)

    fun logError(message: String, error: Exception? = null)
}
