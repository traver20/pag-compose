package com.example.toy

import android.graphics.Matrix
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.ScaleFactor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.viewinterop.AndroidView
import org.libpag.PAGComposition
import org.libpag.PAGFile
import org.libpag.PAGView

private operator fun Size.times(scale: ScaleFactor): IntSize {
    return IntSize((width * scale.scaleX).toInt(), (height * scale.scaleY).toInt())
}

private fun createTransformationMatrix(
    alignment: Alignment,
    contentScale: ContentScale,
    containerSize: IntSize,
    contentSize: Size,
    layoutDirection: LayoutDirection,
): Matrix {
    val matrix = Matrix()

    // Calculate scale factors using ContentScale's computeScaleFactor
    val scaleFactor = contentScale.computeScaleFactor(
        srcSize = contentSize,
        dstSize = containerSize.toSize()
    )

    // Apply content scale
    matrix.setScale(scaleFactor.scaleX, scaleFactor.scaleY)

    val translation = alignment.align(
        contentSize * scaleFactor,
        containerSize,
        layoutDirection
    )

    matrix.reset()
    matrix.preTranslate(translation.x.toFloat(), translation.y.toFloat())
    matrix.preScale(scaleFactor.scaleX, scaleFactor.scaleY)

    return matrix
}

@Composable
fun PagAnimation(
    modifier: Modifier = Modifier,
    assetPath: String,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Crop
) {
    val context = LocalContext.current
    val pagFile = remember(assetPath) {
        PAGFile.Load(context.assets, assetPath)
    }

    PagAnimation(modifier, pagFile, alignment, contentScale)
}

@Composable
fun PagAnimation(
    modifier: Modifier = Modifier,
    composition: PAGComposition,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Crop
) {
    val layoutDirection = LocalLayoutDirection.current

    BoxWithConstraints(
        modifier = modifier.fillMaxSize()
    ) {
        val containerSize = IntSize(
            width = constraints.maxWidth,
            height = constraints.maxHeight
        )

        val contentSize = remember(composition) {
            IntSize(
                width = composition.width(),
                height = composition.height()
            )
        }

        val matrix = remember(alignment, contentScale, containerSize, contentSize) {
            createTransformationMatrix(
                alignment,
                contentScale,
                containerSize,
                contentSize.toSize(),
                layoutDirection
            )
        }

        AndroidView(
            factory = { context ->
                PAGView(context).apply {
                    this.composition = composition
                    this.matrix = matrix

                    setRepeatCount(-1)
                    play()
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
} 