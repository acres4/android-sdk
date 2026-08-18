/*
 * Copyright © 2022 ACRES. All rights reserved.
 * Created by Acres developer.
 */
package com.acres.blesdk.ui.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExposedDropdownMenuDefaults.TrailingIcon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.acres.ble.card_reader.Track

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownMenu(title: String, options: Array<Track>, onValueChange: (Track) -> Unit) {

    var expanded by remember { mutableStateOf(false) }
    var selectedOptionText by remember { mutableStateOf(options[0]) }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        TextField(
            readOnly = true,
            value = selectedOptionText.name,
            onValueChange = {},
            label = { Text(title) },
            trailingIcon = { TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.textFieldColors()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { selectionOption ->
                androidx.compose.material3.DropdownMenuItem(
                    onClick = {
                        onValueChange.invoke(selectionOption)
                        selectedOptionText = selectionOption
                        expanded = false
                    },
                    text = { Text(text = selectionOption.name) }
                )
            }
        }
    }
}
