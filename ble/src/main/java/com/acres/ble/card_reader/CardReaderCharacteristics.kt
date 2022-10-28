/*
 * Copyright © 2022 ACRES. All rights reserved.
 * Created by Acres developer.
 */
package com.acres.ble.card_reader

import java.util.*

object CardReaderCharacteristics {
    val PLAYER_CARD_STATUS_UUID: UUID = UUID.fromString("1179974A-EE22-4DED-A842-78750E977BCF")
    val PLAYER_CARD_TRACK1_UUID: UUID = UUID.fromString("C3F67C88-5D44-4F5B-83AF-896F377AB6E7")
    val PLAYER_CARD_TRACK2_UUID: UUID = UUID.fromString("02F2C17B-751F-4F2E-816C-8D67622614DA")
    val PLAYER_CARD_INSERT_UUID: UUID = UUID.fromString("7147C04E-F8E5-419D-B072-F50C45A5A431")
}
