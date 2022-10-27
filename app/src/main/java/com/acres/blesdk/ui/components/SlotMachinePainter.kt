/*
 * Copyright © 2022 ACRES. All rights reserved.
 * Created by Acres developer.
 */
package com.acres.blesdk.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toSize
import kotlin.math.roundToInt

class SlotMachinePainter
constructor(
    private val image: ImageBitmap,
    private val srcSize: IntSize = IntSize(image.width, image.height),
    private val paint: Paint =
        Paint().apply {
            alpha = 1f
            color = Color.Black
            strokeWidth = 20f
            blendMode = BlendMode.Color
        }
) : Painter() {
    val size: IntSize = validateSize(srcSize)

    private fun validateSize(srcSize: IntSize): IntSize {
        require(
            srcSize.width >= 0 &&
                srcSize.height >= 0 &&
                srcSize.width <= image.width &&
                srcSize.height <= image.height
        )
        return srcSize
    }

    override val intrinsicSize: Size
        get() = size.toSize()

    override fun DrawScope.onDraw() {
        drawIntoCanvas {
            drawImage(
                image,
                dstSize =
                IntSize(this@onDraw.size.width.roundToInt(), this@onDraw.size.height.roundToInt())
            )
            val rect =
                Rect(
                    offset = Offset(size.width / 4.1f, size.height / 5.5f),
                    size = Size(width = size.width * 0.5f, height = size.height * 0.095f)
                )
            //            drawRect(color = Color.Black, topLeft = Offset(
            //                size.width / 4.1f,
            //                size.height / 5.5f
            //            ), size = Size(width = size.width * 0.5f, height = size.height * 0.095f))

            //            it.nativeCanvas.drawText()
        }
    }
}
