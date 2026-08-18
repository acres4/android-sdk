/*
 * Copyright © 2022 ACRES. All rights reserved.
 * Created by Acres developer.
 */
package com.acres.blesdk.ui.main_navigation

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityOptionsCompat
import timber.log.Timber

class BluetoothLauncher : ActivityResultContract<Void?, Boolean>() {
    override fun createIntent(context: Context, input: Void?) =
        Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)

    override fun parseResult(resultCode: Int, intent: Intent?): Boolean =
        resultCode == Activity.RESULT_OK
}

class LocationEnabler : ActivityResultContract<Void?, Boolean>() {
    override fun createIntent(context: Context, input: Void?) =
        Intent(
            Settings.ACTION_LOCATION_SOURCE_SETTINGS,
        )
            .apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }

    override fun parseResult(resultCode: Int, intent: Intent?): Boolean {
        Timber.d("loaction enabler result $resultCode")
        return resultCode == Activity.RESULT_OK
    }
}

@Composable
fun MainNavigationScreen(onCardReaderClicked: () -> Unit, onSlotAndTableClicked: () -> Unit) {
    val context = LocalContext.current
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    val isLocationPermissionGranted =
        (
            context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
            ) &&
            (
                context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED
                )
    val isBluetoothPermissionGranted =
        (
            context.checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) ==
                PackageManager.PERMISSION_GRANTED
            ) &&
            (
                context.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) ==
                    PackageManager.PERMISSION_GRANTED
                )

    val locationEnabled = remember { mutableStateOf(locationManager.isLocationEnabled) }
    val bluetoothEnabled = remember { mutableStateOf(bluetoothManager.adapter.isEnabled) }

    val locationPermissionGranted = remember { mutableStateOf(isLocationPermissionGranted) }

    val bluetoothPermissionGranted = remember { mutableStateOf(isBluetoothPermissionGranted) }

    val locationEnabler = rememberLauncherForActivityResult(contract = LocationEnabler()) {}

    val requestLocationPermission =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) {
            Timber.d("permissions location permission granted $it")
            locationPermissionGranted.value = it
        }

    val requestBluetoothLauncher =
        rememberLauncherForActivityResult(contract = BluetoothLauncher()) {}

    val requestBluetoothLauncherAboveS =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            Timber.d(
                "permissions bluetooth permission granted ${
                permissions.map { it.value }.all { it }
                }"
            )
            bluetoothPermissionGranted.value = permissions.map { it.value }.all { it }
        }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (locationEnabled.value &&
            locationPermissionGranted.value &&
            bluetoothEnabled.value &&
            bluetoothPermissionGranted.value
        ) {
            Button(onClick = onCardReaderClicked) { Text(text = "Card reader") }

            Button(onClick = onSlotAndTableClicked) { Text(text = "Slot and table") }
        } else {

            Button(
                onClick = {
                    if (!locationPermissionGranted.value) {
                        requestLocationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    } else if (!locationEnabled.value) {
                        locationEnabler.launch()
                    } else if (!bluetoothPermissionGranted.value) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            requestBluetoothLauncherAboveS.launch(
                                arrayOf(
                                    Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT
                                )
                            )
                        }
                    } else if (!bluetoothEnabled.value) {
                        requestBluetoothLauncher.launch(ActivityOptionsCompat.makeBasic())
                    }
                }
            ) {
                Text(
                    text =
                    "Bluetooth permissions must be enabled for this application. Click to grant necessary permissions."
                )
            }
        }
    }
}
