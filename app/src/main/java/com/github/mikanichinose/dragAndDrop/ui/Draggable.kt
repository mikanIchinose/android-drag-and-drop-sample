package com.github.mikanichinose.dragAndDrop.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import kotlin.math.roundToInt

// https://developer.android.com/jetpack/compose/touch-input/pointer-input/drag-swipe-fling

@Composable
fun DraggableTextScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .border(1.dp, MaterialTheme.colorScheme.primary)
    ) {
        var containerSize by remember { mutableStateOf(Size.Zero) }
        var offsetX by remember { mutableStateOf(0f) }
        var offsetY by remember { mutableStateOf(0f) }

        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = "parent (width: ${containerSize.width}, height: ${containerSize.height})")
            Text(text = "draggable element (x: ${offsetX.roundToInt()}, y: ${offsetY.roundToInt()})")
        }
        Spacer(modifier = Modifier.padding(16.dp))
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .background(Color.LightGray)
                .onGloballyPositioned { constrains ->
                    containerSize = constrains.size.toSize()
                }
        ) {
            DragHorizontally(
                offsetX = offsetX,
                onDragText = { endX, delta ->
                    if (offsetX + delta >= 0 && endX + delta <= containerSize.width) {
                        offsetX += delta
                    }
                }
            ) {
                Text(
                    modifier = Modifier
                        .background(Color.White)
                        .border(width = 2.dp, color = Color.Blue)
                        .padding(16.dp),
                    text = "Drag me!"
                )
            }
            DragVertically(
                offsetY = offsetY,
                onMove = { bottomY, delta ->
                    if (offsetY + delta >= 0 && bottomY <= containerSize.height) {
                        offsetY += delta
                    }
                }
            ) {
                Text(
                    modifier = Modifier
                        .background(Color.White)
                        .border(width = 2.dp, color = Color.Yellow)
                        .padding(16.dp),
                    text = "Drag me!"
                )
            }
            DragTwoDimension(
                offsetX = offsetX,
                offsetY = offsetY,
                onDragText = { endOffset, dragAmount ->
                    if (offsetX + dragAmount.x >= 0 && endOffset.x + dragAmount.x <= containerSize.width) {
                        offsetX += dragAmount.x
                    }
                    if (offsetY + dragAmount.y >= 0 && endOffset.y + dragAmount.y <= containerSize.height) {
                        offsetY += dragAmount.y
                    }
                },
            ) {
                Text(
                    modifier = Modifier
                        .background(Color.White)
                        .border(width = 2.dp, color = Color.Red)
                        .padding(16.dp),
                    text = "Drag me!"
                )
            }
        }
    }
}

@Composable
fun DragHorizontally(
    offsetX: Float,
    onDragText: (width: Float, delta: Float) -> Unit,
    content: @Composable () -> Unit,
) {
    var endX by remember { mutableStateOf(0f) }
    Box(
        modifier = Modifier
            .offset { IntOffset(offsetX.roundToInt(), 0) }
            .draggable(
                orientation = Orientation.Horizontal,
                state = rememberDraggableState { delta ->
                    onDragText(endX, delta)
                }
            )
            .onGloballyPositioned { constrains ->
                endX = constrains.size.width + constrains.positionInParent().x
            },
    ) {
        content()
    }
}

@Preview
@Composable
fun PreviewDragHorizontally() {
    var containerSize by remember { mutableStateOf(Size.Zero) }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .onGloballyPositioned { constrains ->
                containerSize = constrains.size.toSize()
            }
    ) {
        Box {
            var offsetX by remember { mutableStateOf(0f) }
            DragHorizontally(
                offsetX = offsetX,
                onDragText = { endX, delta ->
                    if (offsetX + delta >= 0 && endX + delta <= containerSize.width) {
                        offsetX += delta
                    }
                }
            ) {
                Text(
                    modifier = Modifier
                        .border(width = 1.dp, color = MaterialTheme.colorScheme.primary)
                        .padding(16.dp),
                    text = "Drag me horizontally!"
                )
            }
        }
    }
}

@Composable
fun DragVertically(
    offsetY: Float,
    onMove: (bottomY: Float, delta: Float) -> Unit,
    content: @Composable () -> Unit = {},
) {
    var bottomY by remember { mutableStateOf(0f) }
    Box(
        modifier = Modifier
            .offset { IntOffset(0, offsetY.roundToInt()) }
            .draggable(
                orientation = Orientation.Vertical,
                state = rememberDraggableState { delta ->
                    onMove(bottomY, delta)
                }
            )
            .onGloballyPositioned { constrains ->
                bottomY = constrains.size.height + constrains.positionInParent().y
            },
    ) {
        content()
    }
}

@Preview
@Composable
fun PreviewDragVertically() {
    var containerSize by remember { mutableStateOf(Size.Zero) }
    Surface(
        modifier = Modifier
            .fillMaxHeight()
            .onGloballyPositioned { constrains ->
                containerSize = constrains.size.toSize()
            }
    ) {
        Box {
            var offsetY by remember { mutableStateOf(0f) }
            DragVertically(
                offsetY = offsetY,
                onMove = { bottomY, delta ->
                    if (offsetY + delta >= 0 && bottomY + delta <= containerSize.height) {
                        offsetY += delta
                    }
                }
            ) {
                Text(
                    modifier = Modifier
                        .border(width = 1.dp, color = MaterialTheme.colorScheme.primary)
                        .padding(16.dp),
                    text = "Drag me vertically!"
                )
            }
        }
    }
}

@Composable
fun DragTwoDimension(
    offsetX: Float,
    offsetY: Float,
    onDragText: (endOffset: Offset, dragAmount: Offset) -> Unit,
    content: @Composable () -> Unit,
) {
    var endOffset by remember { mutableStateOf(Offset.Zero) }
    Box(
        modifier = Modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onDragText(endOffset, dragAmount)
                }
            }
            .onGloballyPositioned { layoutCoordinates ->
                endOffset = Offset(
                    x = layoutCoordinates.size.width + layoutCoordinates.positionInParent().x,
                    y = layoutCoordinates.size.height + layoutCoordinates.positionInParent().y
                )
            }
    ) {
        content()
    }
}

@Preview
@Composable
fun PreviewDragTwoDimension() {
    var containerSize by remember { mutableStateOf(Size.Zero) }
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { constrains ->
                containerSize = constrains.size.toSize()
            }
    ) {
        Box {
            var offsetX by remember { mutableStateOf(0f) }
            var offsetY by remember { mutableStateOf(0f) }
            DragTwoDimension(
                offsetX = offsetX,
                offsetY = offsetY,
                onDragText = { endOffset, dragAmount ->
                    if (offsetX + dragAmount.x >= 0 && endOffset.x + dragAmount.x <= containerSize.width) {
                        offsetX += dragAmount.x
                    }
                    if (offsetY + dragAmount.y >= 0 && endOffset.y + dragAmount.y <= containerSize.height) {
                        offsetY += dragAmount.y
                    }
                },
            ) {
                Text(
                    modifier = Modifier
                        .border(width = 1.dp, color = MaterialTheme.colorScheme.primary)
                        .padding(16.dp),
                    text = "Drag me!"
                )
            }
        }

    }
}