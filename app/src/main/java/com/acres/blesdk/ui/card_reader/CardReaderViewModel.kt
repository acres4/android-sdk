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
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class CardReaderViewModel
@Inject
constructor(private val cardReaderDeviceManager: CardReaderDeviceManager) : ViewModel() {

    private val _cardReaderState = MutableStateFlow("Scanning")
    val cardReaderState: StateFlow<String> = _cardReaderState

    init {
        viewModelScope.launch {
            cardReaderDeviceManager.cardReaderStateFlow.collect {
                Timber.d("cardReaderState :$it")
                when (it) {
                    is CardReaderState.DeviceAvailable ->
                        _cardReaderState.value = "Device ${it.device.name} is available to use"
                    CardReaderState.DeviceBusy -> _cardReaderState.value = "Device is busy"
                    is CardReaderState.DeviceConnected ->
                        _cardReaderState.value = "Device ${it.device.address} connected"
                    is CardReaderState.DeviceError ->
                        _cardReaderState.value = "Device error ${it.exception.message}"
                    is CardReaderState.DiscoveredDevice ->
                        _cardReaderState.value = "Device discovered ${it.result.address} with rssi:${it.rssi}"
                    CardReaderState.Scanning -> _cardReaderState.value = "Scanning"
                    CardReaderState.DeviceDisconnected -> _cardReaderState.value = "Disconnected"
                }
            }
        }
    }

    fun insertPlayerCard(selectedTrack: Track, userId: String) {
        cardReaderDeviceManager.insertPlayerCard(selectedTrack, userId)
    }

    fun disconnect() {
        viewModelScope.launch { cardReaderDeviceManager.disconnectDevice() }
    }
}
