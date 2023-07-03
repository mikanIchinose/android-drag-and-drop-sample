package com.github.mikanichinose.dragAndDrop.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import com.github.mikanichinose.dragAndDrop.ui.component.TodoListItem
import com.github.mikanichinose.dragAndDrop.ui.model.Todo
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.ReorderableLazyListState
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable

private val todos = List(100) {
    Todo(
        id = it,
        title = "Todo $it",
        description = "Todo $it description",
        isDone = false,
    )
}

@Composable
fun TodoListScreen() {
    val todos = remember { todos.toMutableStateList() }
    TodoList(
        todos = todos,
        onCheckedChange = { index, todo ->
            todos[index] = todo
        },
    )
}

@Composable
fun TodoList(
    todos: List<Todo>,
    onCheckedChange: (Int, Todo) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(5.dp)
    ) {
        itemsIndexed(todos) { index, todo ->
            TodoListItem(
                title = todo.title,
                checked = todo.isDone,
                onCheckedChange = {
                    onCheckedChange(index, todo.copy(isDone = it))
                },
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface)
            )
        }
    }
}

@Composable
fun ReordableTodoListScreen() {
    val todos = remember { todos.toMutableStateList() }
    val reorderableLazyListState = rememberReorderableLazyListState(
        onMove = { from, to ->
            todos.apply {
                add(to.index, removeAt(from.index))
            }
        },
    )
    ReordableTodoList(
        todos = todos,
        onCheckedChange = { index, todo ->
            todos[index] = todo
        },
        state = reorderableLazyListState,
    )
}

@Composable
fun ReordableTodoList(
    todos: List<Todo>,
    onCheckedChange: (Int, Todo) -> Unit,
    state: ReorderableLazyListState,
) {
    LazyColumn(
        state = state.listState,
        modifier = Modifier
            .fillMaxSize()
            .padding(5.dp)
            .reorderable(state)
    ) {
        itemsIndexed(todos, { _, todo -> todo.title }) { index, todo ->
            ReorderableItem(reorderableState = state, key = todo.title) { isDragging ->
                val elevation = animateDpAsState(if (isDragging) 16.dp else 0.dp, label = "")
                Box(
                    modifier = Modifier
                        .detectReorderAfterLongPress(state)
                        .shadow(elevation.value)
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    TodoListItem(
                        title = todo.title,
                        checked = todo.isDone,
                        onCheckedChange = {
                            onCheckedChange(index, todo.copy(isDone = it))
                        },
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surface)
                    )
                }
            }
        }
    }
}
