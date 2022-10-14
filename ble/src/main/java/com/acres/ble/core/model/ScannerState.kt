/*
 * Copyright © 2020 ACRES. All rights reserved.
 * Created by Acres developer.
 */
package com.acres.ble.core.model

sealed class ScannerState<out T> {
    class Started<T> : ScannerState<T>()
    data class Scanning<T>(val result: T) : ScannerState<T>()
    class Stopped<T> : ScannerState<T>()
    data class Error<T>(val error: BleScannerError) : ScannerState<T>()
}
