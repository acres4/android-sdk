/*
 * Copyright © 2020 ACRES. All rights reserved.
 * Created by Acres developer.
 */
package com.acres.ble.core.model

enum class BleScannerError(val value: Int) {
    NO_ERROR(0),

    /** Fails to start scan as BLE scan with the same settings is already started by the app. */
    SCAN_FAILED_ALREADY_STARTED(1),

    /** Fails to start scan as app cannot be registered. */
    SCAN_FAILED_APPLICATION_REGISTRATION_FAILED(2),

    /** Fails to start scan due an internal error */
    SCAN_FAILED_INTERNAL_ERROR(3),

    /** Fails to start power optimized scan as this feature is not supported. */
    SCAN_FAILED_FEATURE_UNSUPPORTED(4),

    /** Fails to start scan as it is out of hardware resources. */
    SCAN_FAILED_OUT_OF_HARDWARE_RESOURCES(5),

    /** Fails to start scan as application tries to scan too frequently. */
    SCAN_FAILED_SCANNING_TOO_FREQUENTLY(6),

    /** Bluetooth disabled error */
    SCAN_FAILED_BLUETOOTH_NOT_ENABLED(7),

    /** Bluetooth permission above S not granted */
    SCAN_FAILED_BLUETOOTH_PERMISSION_ABOVE_S_NOT_GRANTED(8),

    /** Location permission not granted */
    SCAN_FAILED_LOCATION_PERMISSION_NOT_GRANTED(9),

    /** Bluetooth permission not granted */
    SCAN_FAILED_BLUETOOTH_PERMISSION_NOT_GRANTED(10),

    /** Unknown error */
    SCAN_FAILED_UNKNOWN_ERROR(11);

    companion object {

        fun fromInt(value: Int?) =
            values().firstOrNull { it.value == value } ?: SCAN_FAILED_UNKNOWN_ERROR
    }
}
