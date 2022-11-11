/*
 * Copyright © 2022 ACRES. All rights reserved.
 * Created by Acres developer.
 */
package com.acres.blesdk.ui.slot_and_table

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.acres.ble.slot_and_table.SlotAndTableReaderState

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun CashOutScreen(viewModel: SlotAndTableViewModel = hiltViewModel()) {

    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when (state) {
            is SlotAndTableReaderState.DeviceAvailable -> {
                Button(onClick = { viewModel.cashOutTable() }) { Text(text = "Cash out") }
                Button(onClick = { viewModel.cancelCashOut() }) { Text(text = "Cancel") }
            }
            is SlotAndTableReaderState.DeviceError -> {
                Text(text = "Something went wrong. Please try again")
            }
            is SlotAndTableReaderState.DiscoveredDevice -> {
                CircularProgressIndicator()
                Text(text = "Device is discovered!")
            }
            SlotAndTableReaderState.Scanning -> {
                CircularProgressIndicator()
                Text(text = "Scanning...")
            }
            is SlotAndTableReaderState.Success -> {
                Text(text = "Cash out success")
                Button(onClick = { viewModel.disconnect() }) { Text(text = "Disconnect device") }
            }
        }
    }
}
