/*
 * Copyright © 2022 ACRES. All rights reserved.
 * Created by Acres developer.
 */
package com.acres.blesdk.ui.card_reader

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.acres.ble.card_reader.CardReaderDeviceManager
import com.acres.ble.card_reader.Track
import com.acres.blesdk.ui.components.DropdownMenu
import com.acres.blesdk.ui.theme.Purple500

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun CardReaderScreen(
    viewModel: CardReaderViewModel = hiltViewModel(),
) {

    val state by viewModel.cardReaderState.collectAsStateWithLifecycle()

    var userId by rememberSaveable { mutableStateOf("") }
    var selectedTrack by rememberSaveable { mutableStateOf(Track.TRACK_1) }

    val maximumAllowableLength =
        if (selectedTrack == Track.TRACK_1) CardReaderDeviceManager.TRACK_1_MAX_BYTE_LENGTH
        else CardReaderDeviceManager.TRACK_2_MAX_BYTE_LENGTH

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = state)
        Spacer(modifier = Modifier.height(15.dp))
        DropdownMenu(
            title = "Select track",
            options = Track.values(),
            onValueChange = { track ->
                userId = ""
                selectedTrack = track
            }
        )
        Spacer(modifier = Modifier.height(15.dp))
        TextField(
            value = userId,
            onValueChange = { text ->
                if (text.length <= maximumAllowableLength) {
                    userId = text
                }
            },
            colors = TextFieldDefaults.textFieldColors(unfocusedIndicatorColor = Purple500),
            singleLine = true,
            trailingIcon = {
                Text(
                    text = "${userId.toByteArray().size}/$maximumAllowableLength",
                )
            }
        )
        Spacer(modifier = Modifier.height(15.dp))
        if (state == "Device is available to use")
            Button(onClick = { viewModel.disconnect() }) { Text(text = "Disconnect") }
        else
            Button(onClick = { viewModel.insertPlayerCard(selectedTrack, userId) }) {
                Text(text = "Insert player card")
            }
    }
}
