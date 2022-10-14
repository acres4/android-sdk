/*
 * Copyright © 2022 ACRES. All rights reserved.
 * Created by Acres developer.
 */
package com.acres.ble.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.core.content.ContextCompat

fun Context.isLocationDisabled() =
    (this.getSystemService(Context.LOCATION_SERVICE) as LocationManager?)?.isLocationEnabled ==
        false

fun Context.isPermissionNotGranted() =
    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
        PackageManager.PERMISSION_GRANTED

fun Context.isBluetoothConnectPermissionGranted(): Boolean {
    return if (!isSorAbove()) true
    else
        ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) ==
            PackageManager.PERMISSION_GRANTED
}

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.S)
fun isSorAbove(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
}
