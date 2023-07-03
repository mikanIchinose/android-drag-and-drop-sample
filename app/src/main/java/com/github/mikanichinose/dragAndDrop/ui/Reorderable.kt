package com.github.mikanichinose.dragAndDrop.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable

@Composable
fun VerticalReorderList() {
    val list = remember { List(100) { "Item $it" }.toMutableStateList() }
    val reorderableLazyListState = rememberReorderableLazyListState(
        onMove = { from, to ->
            list.apply {
                add(to.index, removeAt(from.index))
            }
        },
    )
    LazyColumn(
        state = reorderableLazyListState.listState,
        modifier = Modifier
            .reorderable(reorderableLazyListState)
    ) {
        items(list, { it }) { item ->
            ReorderableItem(reorderableLazyListState, key = item) { isDragging ->
                val elevation = animateDpAsState(if (isDragging) 16.dp else 0.dp, label = "")
                Column(
                    modifier = Modifier
                        .detectReorderAfterLongPress(reorderableLazyListState)
                        .shadow(elevation.value)
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    Text(text = item, modifier = Modifier.padding(16.dp))
                    Divider()
                }
            }
        }
    }
}

// https://stackoverflow.com/a/65076085
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AnimatedShufflableList() {
    var list by remember { mutableStateOf(listOf("A", "B", "C")) }
    LazyColumn {
        item {
            Button(onClick = { list = list.shuffled() }) {
                Text(text = "Shuffle")
            }
        }
        items(list, { it }) {
            Text(text = it, Modifier.animateItemPlacement())
        }
    }
}

@Composable
fun DraggableScreen() {
    val list by remember { mutableStateOf(List(100) { "Item $it" }) }
    DraggableLazyColumn(
        items = list,
        onMove = { from, to ->
            list.toMutableList().apply {
                add(to, removeAt(from))
            }
        }
    )
}

// https://gist.github.com/surajsau/f5342f443352195208029e98b0ee39f3
@Composable
fun <T> DraggableLazyColumn(
    items: List<T>,
    onMove: (Int, Int) -> Unit,
) {
    val scope = rememberCoroutineScope()
    var overscrollJob by remember { mutableStateOf<Job?>(null) }
    val dragAndDropState = rememberDraggableLazyListState(onMove = onMove)
    LazyColumn(
        modifier = Modifier
            .pointerInput(Unit) {
                detectDragGesturesAfterLongPress(
                    onDrag = { change, dragAmount ->
                        change.consume()
                        dragAndDropState.onDrag(dragAmount)
                        if (overscrollJob?.isActive == true)
                            return@detectDragGesturesAfterLongPress

                        dragAndDropState.checkForOverScroll()
                            .takeIf { it != 0f }
                            ?.let {
                                overscrollJob = scope.launch {
                                    dragAndDropState.lazyListState.scrollBy(it)
                                }
                            }
                            ?: run { overscrollJob?.cancel() }
                    },
                    onDragStart = { offset -> dragAndDropState.onDragStart(offset) },
                    onDragEnd = { dragAndDropState.onDragInterrupted() },
                    onDragCancel = { dragAndDropState.onDragInterrupted() },
                )
            },
        state = dragAndDropState.lazyListState,
    ) {
        itemsIndexed(items) { index, item ->
            Column(
                modifier = Modifier
                    .composed {
                        val offsetOrNull = dragAndDropState.elementDisplacement.takeIf {
                            index == dragAndDropState.currentIndexOfDraggedItem
                        }
                        graphicsLayer {
                            translationY = offsetOrNull ?: 0f
                        }
                    }
                    .background(Color.White, shape = RoundedCornerShape(4.dp))
                    .fillMaxWidth()
            ) {
                Text(text = "$item", modifier = Modifier.padding(16.dp))
                Divider()
            }
        }
    }
}

@Composable
fun rememberDraggableLazyListState(
    lazyListState: LazyListState = rememberLazyListState(),
    onMove: (Int, Int) -> Unit,
): DraggableLazyListState {
    return remember {
        DraggableLazyListState(
            lazyListState = lazyListState,
            onMove = onMove
        )
    }
}

class DraggableLazyListState(
    val lazyListState: LazyListState,
    private val onMove: (Int, Int) -> Unit
) {
    var draggedDistance by mutableStateOf(0f)
    var initiallyDraggedElement by mutableStateOf<LazyListItemInfo?>(null)
    var currentIndexOfDraggedItem by mutableStateOf<Int?>(null)
    val initialOffsets: Pair<Int, Int>?
        get() = initiallyDraggedElement?.let { Pair(it.offset, it.offsetEnd) }
    val elementDisplacement: Float?
        get() = currentIndexOfDraggedItem
            ?.let { lazyListState.getVisibleItemInfoFor(it) }
            ?.let { item ->
                (initiallyDraggedElement?.offset ?: 0f).toFloat() + draggedDistance - item.offset
            }
    val currentElement: LazyListItemInfo?
        get() = currentIndexOfDraggedItem?.let { lazyListState.getVisibleItemInfoFor(it) }
    var overscrollJob by mutableStateOf<Job?>(null)

    fun onDragStart(offset: Offset) {
        lazyListState.layoutInfo.visibleItemsInfo
            .firstOrNull { item -> offset.y.toInt() in item.offset..(item.offsetEnd) }
            ?.also {
                currentIndexOfDraggedItem = it.index
                initiallyDraggedElement = it
            }
    }

    fun onDragInterrupted() {
        draggedDistance = 0f
        currentIndexOfDraggedItem = null
        initiallyDraggedElement = null
        overscrollJob?.cancel()
    }

    fun onDrag(offset: Offset) {
        draggedDistance += offset.y
        initialOffsets?.let { (topOffset, bottomOffset) ->
            val startOffset = topOffset + draggedDistance
            val endOffset = bottomOffset + draggedDistance
            currentElement?.let { hovered ->
                lazyListState.layoutInfo.visibleItemsInfo
                    .filterNot { item -> item.offsetEnd < startOffset || item.offset > endOffset || hovered.index == item.index }
                    .firstOrNull { item ->
                        val delta = startOffset - hovered.offsetEnd
                        when {
                            delta > 0 -> (endOffset > item.offsetEnd)
                            else -> (startOffset < item.offset)
                        }
                    }
                    ?.also { item ->
                        currentIndexOfDraggedItem?.let { current ->
                            onMove.invoke(
                                current,
                                item.index
                            )
                        }
                        currentIndexOfDraggedItem = item.index
                    }
            }
        }
    }

    fun checkForOverScroll(): Float {
        return initiallyDraggedElement?.let {
            val startOffset = it.offset + draggedDistance
            val endOffset = it.offsetEnd + draggedDistance

            when {
                draggedDistance > 0 -> {
                    (endOffset - lazyListState.layoutInfo.viewportEndOffset)
                        .takeIf { diff -> diff > 0 }
                }

                draggedDistance < 0 -> {
                    (startOffset - lazyListState.layoutInfo.viewportStartOffset)
                        .takeIf { diff -> diff < 0 }
                }

                else -> null
            }
        } ?: 0f
    }
}

private fun LazyListState.getVisibleItemInfoFor(absoluteIndex: Int): LazyListItemInfo? {
    return layoutInfo.visibleItemsInfo.getOrNull((absoluteIndex - layoutInfo.visibleItemsInfo.first().index))
}

private val LazyListItemInfo.offsetEnd: Int
    get() = offset + size

private fun <T> MutableList<T>.move(from: Int, to: Int) {
    if (from == to) return
    val element = removeAt(from) ?: return
    add(to, element)
}
