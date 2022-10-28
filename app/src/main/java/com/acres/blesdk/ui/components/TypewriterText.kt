/*
 * Copyright © 2022 ACRES. All rights reserved.
 * Created by Acres developer.
 */
package com.acres.blesdk.ui.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.streams.toList

@Composable
fun TypewriterText(
    modifier: Modifier = Modifier,
    texts: List<String>,
) {
    var textIndex by remember { mutableStateOf(0) }
    var textToDisplay by remember { mutableStateOf("") }
    val textCharsList: List<List<String>> = remember { texts.map { it.splitToCodePoints() } }

    LaunchedEffect(
        key1 = texts,
    ) {
        while (textIndex < textCharsList.size) {
            textCharsList[textIndex].forEachIndexed { charIndex, _ ->
                textToDisplay =
                    textCharsList[textIndex]
                        .take(
                            n = charIndex + 1,
                        )
                        .joinToString(
                            separator = "",
                        )

                delay(160)
            }
            textIndex = (textIndex + 1) % texts.size
            delay(1000)
        }
    }

    Text(text = textToDisplay, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = modifier)
}

@RequiresApi(Build.VERSION_CODES.N)
fun String.splitToCodePoints(): List<String> {
    return codePoints().toList().map { String(Character.toChars(it)) }
}
