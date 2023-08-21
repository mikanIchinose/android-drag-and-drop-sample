package com.github.mikanichinose.dragAndDrop

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.github.mikanichinose.dragAndDrop.ui.DraggableScreen
import com.github.mikanichinose.dragAndDrop.ui.theme.DragAndDropTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalFoundationApi::class, ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DragAndDropTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DraggableScreen()
//                    Column {
//                        val tabContents =
//                            listOf("Reorderable", "Reordable2", "DraggableText", "Todo")
//                        val pagerState = rememberPagerState {
//                            tabContents.size
//                        }
//                        val scope = rememberCoroutineScope()
//                        TabRow(selectedTabIndex = pagerState.currentPage) {
//                            tabContents.forEachIndexed { index, title ->
//                                Tab(
//                                    selected = pagerState.currentPage == index,
//                                    onClick = {
//                                        scope.launch {
//                                            pagerState.animateScrollToPage(index)
//                                        }
//                                    },
//                                    text = { Text(text = title) }
//                                )
//                            }
//                        }
//                        HorizontalPager(state = pagerState) {
//                            when (it) {
//                                0 -> VerticalReorderList()
//                                1 -> DraggableScreen()
//                                2 -> DraggableTextScreen()
//                                3 -> ReordableTodoListScreen()
//                            }
//                        }
//                    }
                }
            }
        }
    }
}
