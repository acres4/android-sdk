/*
 * Copyright © 2022 ACRES. All rights reserved.
 * Created by Acres developer.
 */
package com.acres.blesdk.ui.card_reader

import androidx.lifecycle.ViewModel
import com.acres.ble.card_reader.CardReaderDeviceManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CardReaderViewModel @Inject constructor(cardReaderDeviceManager: CardReaderDeviceManager) :
    ViewModel()
