package com.github.mikanichinose.dragAndDrop.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

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

