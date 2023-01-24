/*
 * Copyright © 2022 ACRES. All rights reserved.
 * Created by Acres developer.
 */
package com.acres.ble.core

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Build
import android.util.Log
import com.acres.ble.core.model.BleScannerError
import com.acres.ble.core.model.BleScannerError.*
import com.acres.ble.core.model.ScannerState
import com.acres.ble.util.isBluetoothConnectPermissionGranted
import com.acres.ble.util.isLocationDisabled
import com.acres.ble.util.isPermissionNotGranted
import com.acres.ble.util.toHexString
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import no.nordicsemi.android.ble.BleManager
import no.nordicsemi.android.ble.PhyRequest
import no.nordicsemi.android.ble.data.Data
import no.nordicsemi.android.ble.ktx.suspend
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

    private var scanCallback: ScanCallback? = null

    override fun getGattCallback(): BleManagerGattCallback = AcresBleManagerGattCallback()
    val deviceManagerScope = CoroutineScope(Job() + ioDispatcher)

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

    suspend fun connectDevice(device: BluetoothDevice) {
        withContext(ioDispatcher) {
            try {
                connect(device)
                    // Automatic retries are supported, in case of 133 error.
                    .retry(3 /* times, with */, 100 /* ms interval */)
                    // A connection timeout can be set. This is additional to the Android's connection
                    // timeout which is 30 seconds.
                    .timeout(15_000 /* ms */)
                    // The auto connect feature from connectGatt is available as well
                    .useAutoConnect(false)
                    // This API can be set on any Android version, but will only be used on devices running
                    // Android 8+ with
                    // support to the selected PHY.
                    .usePreferredPhy(
                        PhyRequest.PHY_LE_1M_MASK or
                            PhyRequest.PHY_LE_2M_MASK or
                            PhyRequest.PHY_LE_CODED_MASK
                    )
                    .suspend()
            } catch (e: Exception) {
                handleException(e)
                logger.logError(
                    "failed to connect to Bluetooth device with address:${device.address}", error = e
                )
            }
        }
    }

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

            val targetSdkVersion = context.applicationInfo.targetSdkVersion
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S &&
                targetSdkVersion < Build.VERSION_CODES.S
            ) {
                // Check if Location services are on because they are required to make scanning work for
                // SDK < 31
                if (context.isLocationDisabled()) {
                    close(Throwable(message = SCAN_FAILED_LOCATION_PERMISSION_NOT_GRANTED.name))
                }
                if (context.isPermissionNotGranted()) {
                    close(Throwable(message = SCAN_FAILED_BLUETOOTH_PERMISSION_NOT_GRANTED.name))
                }
            }
            if (!context.isBluetoothConnectPermissionGranted()) {
                close(Throwable(message = SCAN_FAILED_BLUETOOTH_PERMISSION_ABOVE_S_NOT_GRANTED.name))
            }

            val bleAdapter =
                (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
            if (bleAdapter?.isEnabled == false) {
                close(Throwable(message = SCAN_FAILED_BLUETOOTH_NOT_ENABLED.name))
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
            .catch {
                handleScannerError(SCAN_FAILED_UNKNOWN_ERROR, it.message)
                logger.logError("scan flow failed with message:${it.message}")
            }
            .collect {
                Log.d("scannerState", "state$it")
                when (it) {
                    is ScannerState.Error -> {
                        if (it.error != NO_ERROR) {
                            stopScan()
                        }
                        handleScannerError(it.error, "")
                        logger.logError("scan failed with ${it.error.name}")
                    }
                    is ScannerState.Scanning -> {

                        handleScannedDevices(it.result)
                    }
                    is ScannerState.Started -> {}
                    is ScannerState.Stopped -> {}
                }
            }

    abstract fun handleScannedDevices(result: List<ScanResult>)
    abstract fun handleScannerError(error: BleScannerError, message: String?)
    abstract fun handleException(e: Exception)

    fun stopScan() = scanCallback?.let { scanner.stopScan(it) }

    suspend fun <T> readRequest(
        bluetoothGattCharacteristic: BluetoothGattCharacteristic?,
        mapToResponse: (Data) -> T?,
    ): T? {
        return withContext(ioDispatcher) {
            try {
                mapToResponse(readCharacteristic(bluetoothGattCharacteristic).suspend())
            } catch (e: Exception) {
                Log.e(
                    "read request failed",
                    "failed to read data from characteristic:${bluetoothGattCharacteristic?.uuid}"
                )
                handleException(e)
                null
            }
        }
    }

    suspend fun writeRequest(
        bluetoothGattCharacteristic: BluetoothGattCharacteristic?,
        data: ByteArray,
    ) {
        withContext(ioDispatcher) {
            try {
                writeCharacteristic(
                    bluetoothGattCharacteristic, data, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                )
                    .enqueue()
            } catch (e: Exception) {
                handleException(e)
                Log.e(
                    "write request failed",
                    "failed to write data:${data.toHexString()}, to characteristic:${bluetoothGattCharacteristic?.uuid}"
                )
            }
        }
    }

    open suspend fun disconnectDevice() {
        stopScan()
        withContext(ioDispatcher) {
            try {
                disconnect().suspend()
            } catch (e: Exception) {
                handleException(e)
                Log.e("disconnect", "disconnect failed with exception:$e")
            }
        }
    }
}
