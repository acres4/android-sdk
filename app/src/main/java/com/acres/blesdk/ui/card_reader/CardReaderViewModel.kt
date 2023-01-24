/*
 * Copyright © 2022 ACRES. All rights reserved.
 * Created by Acres developer.
 */
package com.acres.blesdk.ui.card_reader

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.acres.ble.card_reader.CardReaderDeviceManager
import com.acres.ble.card_reader.CardReaderState
import com.acres.ble.card_reader.Track
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class CardReaderScreenState(
    val deviceState: CardReaderState = CardReaderState.Scanning,
    val infoText: String = "Scanning"
)

@HiltViewModel
class CardReaderViewModel @Inject constructor(private val deviceManager: CardReaderDeviceManager) :
    ViewModel() {

    private val _state = MutableStateFlow(CardReaderScreenState())
    val state: StateFlow<CardReaderScreenState> = _state

    init {
        viewModelScope.launch {
            deviceManager.cardReaderStateFlow.collect { deviceState ->
                Timber.d("cardReaderState: $deviceState")
                val infoText =
                    when (deviceState) {
                        is CardReaderState.DeviceAvailable ->
                            "Device ${deviceState.device} is available to use"
                        CardReaderState.DeviceBusy -> {
                            "Device is busy"
                        }
                        is CardReaderState.DeviceConnected -> {
                            "Device ${deviceState.device.address} connected"
                        }
                        is CardReaderState.DeviceError -> {
                            "Device error ${deviceState.exception.message}"
                        }
                        is CardReaderState.DiscoveredDevice -> {
                            "Device discovered ${deviceState.result.address} with rssi:${deviceState.rssi}"
                        }
                        CardReaderState.Scanning -> {
                            "Scanning"
                        }
                        CardReaderState.DeviceDisconnected -> {
                            "Disconnected"
                        }
                        is CardReaderState.ScannerError -> {
                            if (deviceState.message.isNullOrEmpty()) {
                                "Scan failed with error: ${deviceState.error}"
                            } else {
                                "Scan failed with error: ${deviceState.message}"
                            }
                        }
                    }
                _state.update { it.copy(deviceState = deviceState, infoText = infoText) }
            }
        }
    }

    fun insertPlayerCard(selectedTrack: Track, userId: String) =
        deviceManager.insertPlayerCard(selectedTrack, userId)

    fun disconnect() = viewModelScope.launch { deviceManager.disconnectDevice() }
}
