package com.github.mikanichinose.dragAndDrop

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.github.mikanichinose.dragAndDrop.ui.DraggableScreen
import com.github.mikanichinose.dragAndDrop.ui.DraggableTextScreen
import com.github.mikanichinose.dragAndDrop.ui.ReordableTodoListScreen
import com.github.mikanichinose.dragAndDrop.ui.VerticalReorderList
import com.github.mikanichinose.dragAndDrop.ui.theme.DragAndDropTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DragAndDropTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column {
                        val pagerState = rememberPagerState()
                        val scope = rememberCoroutineScope()
                        val tabContents =
                            listOf("Reorderable", "Reordable2", "DraggableText", "Todo")
                        TabRow(selectedTabIndex = pagerState.currentPage) {
                            tabContents.forEachIndexed { index, title ->
                                Tab(
                                    selected = pagerState.currentPage == index,
                                    onClick = {
                                        scope.launch {
                                            pagerState.animateScrollToPage(index)
                                        }
                                    },
                                    text = { Text(text = title) }
                                )
                            }
                        }
                        HorizontalPager(pageCount = tabContents.size, state = pagerState) {
                            when (it) {
                                0 -> VerticalReorderList()
                                1 -> DraggableScreen()
                                2 -> DraggableTextScreen()
                                3 -> ReordableTodoListScreen()
                            }
                        }
                    }
                }
            }
        }
    }
}
