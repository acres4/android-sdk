/*
 * Copyright © 2022 ACRES. All rights reserved.
 * Created by Acres developer.
 */
package com.acres

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.acres.blesdk.AppDestinations
import com.acres.blesdk.AppNavigationActions
import com.acres.blesdk.ui.card_reader.CardReaderScreen
import com.acres.blesdk.ui.main_navigation.MainNavigationScreen
import com.acres.blesdk.ui.slot_and_table.CashOutScreen
import com.acres.blesdk.ui.slot_and_table.FundTableScreen
import com.acres.blesdk.ui.slot_and_table.SlotAndTableScreen

@Composable
fun AppNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = AppDestinations.MAIN_NAVIGATION_ROUTE,
    navActions: AppNavigationActions =
        remember(navController) { AppNavigationActions(navController) }
) {

    NavHost(navController = navController, startDestination = startDestination, modifier = modifier) {
        composable(
            AppDestinations.MAIN_NAVIGATION_ROUTE,
        ) {
            MainNavigationScreen(
                onCardReaderClicked = { navActions.navigateToCardReaderScreen() },
                onSlotAndTableClicked = { navActions.navigateToSlotAndTableScreen() }
            )
        }
        composable(
            AppDestinations.CARD_READER_ROUTE,
        ) { CardReaderScreen() }

        composable(
            AppDestinations.SLOT_AND_TABLE_ROUTE,
        ) {
            SlotAndTableScreen(
                onFundClicked = { navActions.navigateToFundTableScreen() },
                onCashOutClicked = { navActions.navigateToCashOutScreen() }
            )
        }

        composable(
            AppDestinations.FUND_TABLE_ROUTE,
        ) { FundTableScreen() }

        composable(
            AppDestinations.CASH_OUT_ROUTE,
        ) { CashOutScreen() }
    }
}
