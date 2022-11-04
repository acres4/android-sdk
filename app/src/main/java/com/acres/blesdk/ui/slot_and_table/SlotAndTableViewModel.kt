/*
 * Copyright © 2022 ACRES. All rights reserved.
 * Created by Acres developer.
 */
package com.acres.blesdk.ui.slot_and_table

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.acres.ble.slot_and_table.SlotAndTableDeviceManager
import com.acres.ble.slot_and_table.SlotAndTableReaderState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SlotAndTableViewModel
@Inject
constructor(private val slotAndTableDeviceManager: SlotAndTableDeviceManager) : ViewModel() {

    private val _state = MutableStateFlow("Scanning")
    val state: StateFlow<String> = _state

    init {

        viewModelScope.launch {
            slotAndTableDeviceManager.slotAndTableStateFlow.collect {
                Timber.d("slotAndTableState :$it")
                when (it) {
                    SlotAndTableReaderState.DeviceAvailable -> {
                        fundTable(10)
                    }
                    is SlotAndTableReaderState.DeviceConnected -> {}
                    is SlotAndTableReaderState.DeviceError -> {}
                    is SlotAndTableReaderState.DiscoveredDevice -> {}
                    SlotAndTableReaderState.Scanning -> {}
                }
            }
        }
    }

    fun fundTable(amount: Int) = viewModelScope.launch { slotAndTableDeviceManager.fundTable(amount) }

    fun cancelCashOut() = viewModelScope.launch { slotAndTableDeviceManager.cancelCashOut() }

    fun cashOutTable() = viewModelScope.launch { slotAndTableDeviceManager.cashOut() }
}
