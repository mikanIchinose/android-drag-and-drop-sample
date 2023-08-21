package com.github.mikanichinose.dragAndDrop.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable

@Composable
fun ReorderableScreen() {
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

