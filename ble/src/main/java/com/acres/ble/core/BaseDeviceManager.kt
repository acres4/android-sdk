/*
 * Copyright © 2022 ACRES. All rights reserved.
 * Created by Acres developer.
 */
package com.acres.ble.core

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothManager
import android.content.Context
import androidx.activity.result.contract.ActivityResultContracts
import com.acres.ble.core.model.BleScannerError
import com.acres.ble.core.model.BleScannerError.*
import com.acres.ble.core.model.ScannerState
import com.acres.ble.util.isBluetoothConnectPermissionGranted
import com.acres.ble.util.isLocationDisabled
import com.acres.ble.util.isPermissionNotGranted
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import no.nordicsemi.android.ble.BleManager
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat
import no.nordicsemi.android.support.v18.scanner.ScanCallback
import no.nordicsemi.android.support.v18.scanner.ScanResult
import no.nordicsemi.android.support.v18.scanner.ScanSettings

abstract class BaseDeviceManager
constructor(
    context: Context,
    private val ioDispatcher: CoroutineDispatcher,
    private val computationDispatcher: CoroutineDispatcher,
    private val scanner: BluetoothLeScannerCompat = BluetoothLeScannerCompat.getScanner(),
    private val logger: BleLogger,
) : BleManager(context) {

    val bluetoothPermissionLauncher = ActivityResultContracts.RequestMultiplePermissions()

    private var scanCallback: ScanCallback? = null

    override fun getGattCallback(): BleManagerGattCallback = AcresBleManagerGattCallback()

    override fun log(priority: Int, message: String) {
        logger.logDebug("priority:$priority,message:$message} ")
    }

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

    suspend fun startScanFlow(reportDelay: Long = 2000) =
        callbackFlow {
            scanCallback =
                object : ScanCallback() {
                    override fun onScanResult(callbackType: Int, result: ScanResult) {
                        trySend(ScannerState.Scanning(listOf(result)))
                    }

                    override fun onBatchScanResults(results: MutableList<ScanResult>) {
                        trySend(ScannerState.Scanning(results))
                    }

                    override fun onScanFailed(errorCode: Int) {
                        trySend(ScannerState.Error(BleScannerError.fromInt(errorCode)))
                    }
                }

            if (context.isLocationDisabled()) {
                send(ScannerState.Error(SCAN_FAILED_LOCATION_PERMISSION_NOT_GRANTED))
            }
            if (context.isPermissionNotGranted()) {
                send(ScannerState.Error(SCAN_FAILED_BLUETOOTH_PERMISSION_NOT_GRANTED))
            }
            if (!context.isBluetoothConnectPermissionGranted()) {
                send(ScannerState.Error(SCAN_FAILED_BLUETOOTH_PERMISSION_ABOVE_S_NOT_GRANTED))
            }

            val bleAdapter =
                (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
            if (bleAdapter?.isEnabled == false) {
                send(ScannerState.Error(SCAN_FAILED_BLUETOOTH_NOT_ENABLED))
            }

            val scanSettings =
                ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .setReportDelay(reportDelay)
                    .setUseHardwareBatchingIfSupported(false)
                    .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                    .build()

            scanCallback?.let { scanner.startScan(null, scanSettings, it) }

            send(ScannerState.Started())

            awaitClose()
        }
            .flowOn(ioDispatcher)
            .collect {
                when (it) {
                    is ScannerState.Error -> {}
                    is ScannerState.Scanning -> {}
                    is ScannerState.Started -> {}
                    is ScannerState.Stopped -> {}
                }
            }

    private fun stopScan() = scanCallback?.let { scanner.stopScan(it) }

    private fun handleError(error: BleScannerError) {
        logger.logError("scan failed with ${error.name}")
        if (error != NO_ERROR) {
            stopScan()
        }
        when (error) {
            NO_ERROR -> TODO()
            SCAN_FAILED_ALREADY_STARTED -> TODO()
            SCAN_FAILED_APPLICATION_REGISTRATION_FAILED -> TODO()
            SCAN_FAILED_INTERNAL_ERROR -> TODO()
            SCAN_FAILED_FEATURE_UNSUPPORTED -> TODO()
            SCAN_FAILED_OUT_OF_HARDWARE_RESOURCES -> TODO()
            SCAN_FAILED_SCANNING_TOO_FREQUENTLY -> TODO()
            SCAN_FAILED_BLUETOOTH_NOT_ENABLED -> TODO()
            SCAN_FAILED_BLUETOOTH_PERMISSION_ABOVE_S_NOT_GRANTED -> TODO()
            SCAN_FAILED_LOCATION_PERMISSION_NOT_GRANTED -> TODO()
            SCAN_FAILED_BLUETOOTH_PERMISSION_NOT_GRANTED -> TODO()
            SCAN_FAILED_UNKNOWN_ERROR -> TODO()
        }
    }
}
