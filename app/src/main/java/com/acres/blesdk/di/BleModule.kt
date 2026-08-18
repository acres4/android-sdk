/*
 * Copyright © 2022 ACRES. All rights reserved.
 * Created by Acres developer.
 */
package com.acres.blesdk.di

import android.content.Context
import com.acres.ble.card_reader.CardReaderDeviceManager
import com.acres.ble.slot_and_table.SlotAndTableDeviceManager
import com.acres.blesdk.ble.logger.BleLogger
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext

@Module
@InstallIn(ViewModelComponent::class)
object BleModule {

    @Provides fun provideBleLogger(): BleLogger = BleLogger()

    @Provides
    fun provideCardManager(
        @ApplicationContext context: Context,
        bleLogger: BleLogger,
    ): CardReaderDeviceManager = CardReaderDeviceManager(context, bleLogger)

    @Provides
    fun provideSlotAndTableManager(
        @ApplicationContext context: Context,
        bleLogger: BleLogger,
    ): SlotAndTableDeviceManager = SlotAndTableDeviceManager(context, bleLogger)
}
