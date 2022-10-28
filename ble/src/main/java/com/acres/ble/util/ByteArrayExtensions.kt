/*
 * Copyright © 2020 ACRES. All rights reserved.
 * Created by Acres developer.
 */
package com.acres.ble.util

import java.nio.ByteBuffer
import java.nio.ByteOrder

fun ByteArray.toHexString() =
    "[${
    this.joinToString(" ") {
        String.format("%02X", it)
    }
    }] len=$size"

fun ByteArray.toHex(): String = joinToString(separator = "") { eachByte -> "%02x".format(eachByte) }

fun ByteArray?.toInt(): Int? {
    val data = this ?: return null

    return when (data.size) {
        1 -> data[0].toInt()
        2 -> ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).short.toInt()
        3 -> ByteBuffer.wrap(data).put(0).order(ByteOrder.LITTLE_ENDIAN).int
        4 -> ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).int
        else -> null
    }
}

fun ByteArray.chunkedSequence(chunk: Int): Sequence<ByteArray> {
    val input = this.inputStream().buffered()
    val buffer = ByteArray(chunk)
    return generateSequence {
        val red = input.read(buffer)
        if (red >= 0) buffer.copyOf(red)
        else {
            input.close()
            null
        }
    }
}

fun Byte.toBoolean(): Boolean =
    when (this) {
        0.toByte() -> true
        else -> false
    }
