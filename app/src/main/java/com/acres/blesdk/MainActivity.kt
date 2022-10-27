/*
 * Copyright © 2022 ACRES. All rights reserved.
 * Created by Acres developer.
 */
package com.acres.blesdk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.acres.AppNavGraph
import com.acres.blesdk.ui.theme.BleSdkTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { BleSdkTheme { AppNavGraph() } }
    }
}
