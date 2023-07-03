package com.github.mikanichinose.dragAndDrop.ui.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.mikanichinose.dragAndDrop.ui.theme.DragAndDropTheme

@Composable
fun TodoListItem(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 5.dp)
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
        TodoText(
            title = title,
            checked = checked,
        )
    }
}

@Composable
fun TodoText(
    title: String,
    checked: Boolean,
) {
    Text(
        text = title,
        style = TextStyle(
            fontSize = 16.sp,
            textDecoration = if (checked) TextDecoration.LineThrough else TextDecoration.None,
        )
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewTodoListItem() {
    DragAndDropTheme {
        TodoListItem(
            title = "Todo 1",
            checked = false,
            onCheckedChange = {},
        )
    }
}
