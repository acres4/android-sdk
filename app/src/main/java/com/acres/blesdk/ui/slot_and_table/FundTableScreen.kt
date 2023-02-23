/*
 * Copyright © 2022 ACRES. All rights reserved.
 * Created by Acres developer.
 */
package com.acres.blesdk.ui.slot_and_table

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.acres.ble.slot_and_table.SlotAndTableReaderState
import com.acres.blesdk.ui.theme.Purple500

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun FundTableScreen(viewModel: SlotAndTableViewModel = hiltViewModel()) {

    val state by viewModel.state.collectAsStateWithLifecycle()
    var value by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when (val targetState = state) {
            is SlotAndTableReaderState.DeviceAvailable -> {
                TextField(
                    value = value,
                    onValueChange = { text -> value = text },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = TextFieldDefaults.textFieldColors(unfocusedIndicatorColor = Purple500),
                    singleLine = true,
                    placeholder = { Text(text = "Enter amount") }
                )
                Spacer(modifier = Modifier.height(15.dp))
                Button(
                    onClick = {
                        if (value.isNotEmpty()) {
                            viewModel.fundTable(value.toInt())
                        }
                    }
                ) { Text(text = "Fund") }
                Button(onClick = { viewModel.disconnect() }) { Text(text = "Disconnect device") }
            }
            is SlotAndTableReaderState.DeviceError -> {
                Text(text = "Something went wrong. Please try again")
                Button(onClick = { viewModel.disconnect() }) { Text(text = "Disconnect device") }
            }
            is SlotAndTableReaderState.DeviceDisconnected -> {
                Text(text = "Device disconnected: ${targetState.reason}")
            }
            is SlotAndTableReaderState.DiscoveredDevice -> {
                CircularProgressIndicator()
                Text(text = "Device is discovered!")
            }
            is SlotAndTableReaderState.None -> {}
            SlotAndTableReaderState.Scanning -> {
                CircularProgressIndicator()
                Text(text = "Scanning...")
            }
            is SlotAndTableReaderState.Success -> {
                Text(text = "Fund success")
                Button(onClick = { viewModel.disconnect() }) { Text(text = "Disconnect device") }
            }
            is SlotAndTableReaderState.ScannerError -> {
                Text(
                    text =
                    if (targetState.message.isNullOrEmpty()) {
                        "Scan failed with error: ${targetState.error}"
                    } else {
                        "Scan failed with error: ${targetState.message}"
                    }
                )
            }
        }
    }
}
