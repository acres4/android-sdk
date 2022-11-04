/*
 * Copyright © 2022 ACRES. All rights reserved.
 * Created by Acres developer.
 */
package com.acres.blesdk.ui.slot_and_table

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun FundTableScreen(viewModel: SlotAndTableViewModel = hiltViewModel()) {

    val state by viewModel.state.collectAsStateWithLifecycle()

    Column {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(15.dp))
        Text(
            text = "Funding in progress...\n\nPlease wait for the dealer confirmation.",
            textAlign = TextAlign.Center
        )
    }
}
