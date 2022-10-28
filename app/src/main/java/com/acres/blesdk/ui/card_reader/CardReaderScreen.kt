/*
 * Copyright © 2022 ACRES. All rights reserved.
 * Created by Acres developer.
 */
package com.acres.blesdk.ui.card_reader

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.navigation.compose.hiltViewModel
import com.acres.blesdk.R
import com.acres.blesdk.ui.components.SlotMachinePainter
import com.acres.blesdk.ui.components.TypewriterText

@Composable
fun CardReaderScreen(
    viewModel: CardReaderViewModel = hiltViewModel(),
) {

    val slotMachineImage = ImageBitmap.imageResource(id = R.drawable.slot_machine)
    val customPainter = remember { SlotMachinePainter(slotMachineImage) }

    val infiniteTransition = rememberInfiniteTransition()

    val scale by
    infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec =
        infiniteRepeatable(animation = tween(1000), repeatMode = RepeatMode.Reverse)
    )
    ConstraintLayout(
        modifier = Modifier.fillMaxSize(),
    ) {
        //        CircularProgressIndicator()
        //        Spacer(modifier = Modifier.height(15.dp))
        //        Text(
        //            text = "Inserting card...\nHold your phone close to the card reader.",
        //            textAlign = TextAlign.Center
        //        )
        val (image, loadingText) = createRefs()

        Image(
            painter = customPainter,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier =
            Modifier.fillMaxSize(0.8f).constrainAs(image) {
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            },
            alignment = Alignment.Center
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier =
            Modifier
                //                .offset(x = (customPainter.size.width * 0.18f).dp,
                //                    y = (customPainter.size.height * 0.12f).dp)
                .fillMaxHeight(0.2f)
                .fillMaxWidth(0.4f)
                .constrainAs(loadingText) {
                    top.linkTo(image.top, (customPainter.size.height * 0.05f).dp)
                    start.linkTo(image.start, (customPainter.size.width * 0.05f).dp)
                    end.linkTo(image.end)
                    //                    bottom.linkTo(image.bottom)
                }
        ) {
            //             Text(text = "Scanning",
            //
            //                 modifier = Modifier
            //                     .wrapContentSize().scale(scale))

            TypewriterText(texts = listOf("Scanning..."), modifier = Modifier.wrapContentSize())
        }

        //        Text(text = "Scanning",
        //
        //            modifier = Modifier
        //                .offset(x = (customPainter.size.width * 0.18f).dp,
        //                    y = (customPainter.size.height * 0.12f).dp)
        //                .fillMaxSize(0.8f))
    }
}
