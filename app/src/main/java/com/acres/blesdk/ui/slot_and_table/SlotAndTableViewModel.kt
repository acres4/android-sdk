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
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SlotAndTableViewModel
@Inject
constructor(private val deviceManager: SlotAndTableDeviceManager) : ViewModel() {

    val state: StateFlow<SlotAndTableReaderState> = deviceManager.slotAndTableStateFlow

    fun fundTable(amount: Int) = viewModelScope.launch { deviceManager.fundTable(amount) }

    fun cancelCashOut() = viewModelScope.launch { deviceManager.cancel() }

    fun cashOutTable() = viewModelScope.launch { deviceManager.cashOut() }

    fun disconnect() = viewModelScope.launch { deviceManager.disconnectDevice() }
}
