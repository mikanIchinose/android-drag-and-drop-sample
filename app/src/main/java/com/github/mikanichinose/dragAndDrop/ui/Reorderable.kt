package com.github.mikanichinose.dragAndDrop.ui

import android.util.Log
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.CoroutineScope
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
    val list = remember { List(10) { "Item $it" }.toMutableStateList() }
    DraggableLazyColumn(
        items = list,
        onMove = { from, to ->
            list.apply {
                add(to, removeAt(from))
            }
        },
    )
}

// 多分ここを参考にしてる https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:compose/foundation/foundation/integration-tests/foundation-demos/src/main/java/androidx/compose/foundation/demos/LazyColumnDragAndDropDemo.kt
// https://gist.github.com/surajsau/f5342f443352195208029e98b0ee39f3
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun <T> DraggableLazyColumn(
    items: List<T>,
    onMove: (from: Int, to: Int) -> Unit,
) {
    val scope = rememberCoroutineScope()
    var overscrollJob by remember { mutableStateOf<Job?>(null) }
    val lazyListState = rememberLazyListState()
    val draggableLazyListState = rememberDraggableLazyListState(
        lazyListState = lazyListState,
        onMove = onMove,
        scope = scope
    )
    LazyColumn(
        modifier = Modifier
            .padding(16.dp)
            .pointerInput(Unit) {
                detectDragGesturesAfterLongPress(
                    // この部分は、DraggableLazyListStateのonDragに移せないのか?
                    onDrag = { change, dragAmount ->
                        change.consume()
                        draggableLazyListState.onDrag(dragAmount)
                        if (overscrollJob?.isActive == true)
                            return@detectDragGesturesAfterLongPress

                        // jobが死んでいるとき
                        draggableLazyListState
                            .checkForOverScroll()
                            .takeIf { it != 0f }
                            ?.let {
                                // リストの先頭または末尾に来たときに動く
                                overscrollJob = scope.launch {
                                    draggableLazyListState.lazyListState.scrollBy(it)
                                }
                            }
                            ?: run {
                                overscrollJob?.cancel()
                            }
                    },
                    onDragStart = draggableLazyListState::onDragStart,
                    onDragEnd = draggableLazyListState::onDragInterrupted,
                    onDragCancel = draggableLazyListState::onDragInterrupted,
                )
            },
        state = draggableLazyListState.lazyListState,
    ) {
        itemsIndexed(items, { _, item -> "$item" }) { index, item ->
            val isDragging = draggableLazyListState.currentDraggedItemIndex == index
            val elevation = animateDpAsState(if (isDragging) 16.dp else 0.dp, label = "")
            val draggingModifier = if (isDragging) {
                Modifier
                    .zIndex(1f)
                    .graphicsLayer {
                        // ドラッグしているときの動きを描画
                        val offsetOrNull = draggableLazyListState.itemDisplacement
                        translationY = offsetOrNull ?: 0f
                    }
            } else {
                Modifier.animateItemPlacement()
            }
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .then(draggingModifier)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(elevation.value, RoundedCornerShape(100.dp))
                        .background(Color.White, RoundedCornerShape(100.dp))
                        .border(2.dp, Color.Red, RoundedCornerShape(100.dp))
                ) {
                    Text(
                        text = "$item", modifier = Modifier.padding(30.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun rememberDraggableLazyListState(
    lazyListState: LazyListState,
    scope: CoroutineScope,
    onMove: (Int, Int) -> Unit,
): DraggableLazyListState {
    return remember {
        DraggableLazyListState(
            lazyListState = lazyListState,
            scope = scope,
            onMove = onMove,
        )
    }
}

class DraggableLazyListState(
    val lazyListState: LazyListState,
    private val scope: CoroutineScope,
    private val onMove: (from: Int, to: Int) -> Unit,
) {
    // 要素の移動距離
    private var draggedDistance by mutableFloatStateOf(0f)

    // ドラッグを検知したときに更新される
    // DragInterruptedのときに解放される
    private var initialDraggedItem by mutableStateOf<LazyListItemInfo?>(null)

    // 移動前の上庭と下底のoffsetのペア
    private val initialDraggedItemOffsets: Pair<Int, Int>?
        get() = initialDraggedItem?.let { Pair(it.offset, it.offsetEnd) }

    private val currentDraggedItem: LazyListItemInfo?
        get() = currentDraggedItemIndex?.let { lazyListState.getVisibleItemInfoFor(it) }

    // なんのために必要なの？
    private var overscrollJob by mutableStateOf<Job?>(null)

    // initialDraggedItemとの違いは?
    // 更新タイミングが違ってそう
    var currentDraggedItemIndex by mutableStateOf<Int?>(null)
    val itemDisplacement: Float?
        get() = currentDraggedItemIndex
            ?.let { lazyListState.getVisibleItemInfoFor(it) }
            ?.let { item ->
                (initialDraggedItem?.offset ?: 0f).toFloat() + draggedDistance - item.offset
            }

    fun onDragStart(offset: Offset) {
        lazyListState.layoutInfo.visibleItemsInfo
            // 指の位置から、どの要素をドラッグしているかを判定する
            .firstOrNull { item -> offset.y.toInt() in item.offset..item.offsetEnd }
            ?.also {
                currentDraggedItemIndex = it.index
                initialDraggedItem = it
            }
        log("pointer: $offset")
    }

    fun onDragInterrupted() {
        draggedDistance = 0f
        currentDraggedItemIndex = null
        initialDraggedItem = null
        overscrollJob?.cancel()
    }

    fun onDrag(offset: Offset) {
        // 移動距離を足す
        draggedDistance += offset.y
        initialDraggedItemOffsets?.let { (topOffset, bottomOffset) ->
            // 移動距離を足す
            val startOffset = topOffset + draggedDistance
            val endOffset = bottomOffset + draggedDistance
            currentDraggedItem?.let { currentDraggedItem ->
                lazyListState.layoutInfo.visibleItemsInfo
                    // なにやってるんだ？
                    // この条件を突破できる要素は排除している
                    // item.offsetEnd < startOffset: ドラッグ対象よりも全体が上にある要素
                    // item.offset > endOffset: ドラッグ対象よりも全体が下にある要素
                    // currentDraggedItem.index == item.index: ドラッグ対象と同じ要素, 1mmも動かしていない状態
                    // つまりなに？
                    // ドラッグ中の要素と重なっている要素
                    .filterNot { item -> item.offsetEnd < startOffset || item.offset > endOffset || currentDraggedItem.index == item.index }
                    // なにやってるんだ？
                    .firstOrNull { item ->
                        val delta = startOffset - currentDraggedItem.offset
                        when {
                            delta > 0 -> (endOffset > item.offsetEnd)
                            else -> (startOffset < item.offset)
                        }
                    }
                    ?.also { item ->
                        currentDraggedItemIndex?.let { current ->
                            if (item.index == lazyListState.firstVisibleItemIndex || currentDraggedItemIndex == lazyListState.firstVisibleItemIndex) {
                                scope.launch {
                                    onMove(
                                        current,
                                        item.index,
                                    )
                                    lazyListState.scrollToItem(
                                        lazyListState.firstVisibleItemIndex,
                                        lazyListState.firstVisibleItemScrollOffset
                                    )
                                }
                            } else {
                                onMove(
                                    current,
                                    item.index,
                                )
                            }
                        }
                        currentDraggedItemIndex = item.index
                        log("onMove ${lazyListState.layoutInfo.visibleItemsInfo.map { it.key }}")
                    }
            }
        }
    }

    fun checkForOverScroll(): Float {
        return initialDraggedItem?.let {
            val startOffset = it.offset + draggedDistance
            val endOffset = it.offsetEnd + draggedDistance

            when {
                // note(mikan): 下方向
                draggedDistance > 0 -> {
                    (endOffset - lazyListState.layoutInfo.viewportEndOffset)
                        .takeIf { diff -> diff > 0 }
                }

                // note(mikan): 上方向
                draggedDistance < 0 -> {
                    (startOffset - lazyListState.layoutInfo.viewportStartOffset)
                        .takeIf { diff -> diff < 0 }
                }

                else -> null
            }
        } ?: 0f
    }

    private fun log(message: String = "") {
        Log.d(
            this.javaClass.simpleName,
            """
            $message
            draggedDistance: $draggedDistance
            initiallyDraggedElement: ${initialDraggedItem?.prettyPrint()}
            initialOffsets: $initialDraggedItemOffsets
            currentItemInfo: ${currentDraggedItem?.prettyPrint()} 
            """.trimIndent(),
        )
    }

    private fun LazyListItemInfo.prettyPrint(): String =
        "index: $index key: $key size: $size contentType: $contentType offset: $offset offsetEnd: $offsetEnd"
}

private fun LazyListState.getVisibleItemInfoFor(absoluteIndex: Int): LazyListItemInfo? {
    return layoutInfo.visibleItemsInfo.getOrNull((absoluteIndex - layoutInfo.visibleItemsInfo.first().index))
}

private val LazyListItemInfo.offsetEnd: Int
    get() = offset + size
