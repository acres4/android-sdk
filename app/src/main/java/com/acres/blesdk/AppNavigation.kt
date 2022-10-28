/*
 * Copyright © 2022 ACRES. All rights reserved.
 * Created by Acres developer.
 */
package com.acres.blesdk

import androidx.navigation.NavHostController
import com.acres.blesdk.AppScreens.CARD_READER_SCREEN
import com.acres.blesdk.AppScreens.MAIN_NAVIGATION_SCREEN
import com.acres.blesdk.AppScreens.SLOT_AND_TABLE_SCREEN

private object AppScreens {
    const val MAIN_NAVIGATION_SCREEN = "main_navigation_screen"
    const val CARD_READER_SCREEN = "card_reader_screen"
    const val SLOT_AND_TABLE_SCREEN = "slot_and_table_screen"
}

object AppDestinationArgs

object AppDestinations {
    const val MAIN_NAVIGATION_ROUTE = MAIN_NAVIGATION_SCREEN
    const val CARD_READER_ROUTE = CARD_READER_SCREEN
    const val SLOT_AND_TABLE_ROUTE = SLOT_AND_TABLE_SCREEN
}

class AppNavigationActions(private val navController: NavHostController) {

    fun navigateToCardReaderScreen() {
        navController.navigate(AppDestinations.CARD_READER_ROUTE)
    }

    fun navigateToSlotAndTableScreen() {
        navController.navigate(AppDestinations.SLOT_AND_TABLE_ROUTE)
    }

    fun popBackStack() = navController.popBackStack()
}

/**
 * ScanResult{ device=04:CD:15:57:2E:3B, scanRecord=ScanRecord [advertiseFlags=6,
 * serviceUuids=[c83fe52e-0ab5-49d9-9817-98982b4c48a3], manufacturerSpecificData=null,
 * serviceData=null, txPowerLevel=-26, deviceName=BID-intf], rssi=-94,
 * timestampNanos=351090424769011, eventType=17, primaryPhy=1, secondaryPhy=0, advertisingSid=255,
 * txPower=127, periodicAdvertisingInterval=0}
 */
