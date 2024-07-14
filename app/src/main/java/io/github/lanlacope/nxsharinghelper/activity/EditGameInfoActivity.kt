package io.github.lanlacope.nxsharinghelper.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.lanlacope.nxsharinghelper.R
import io.github.lanlacope.nxsharinghelper.activity.component.AddGameInfoDialog
import io.github.lanlacope.nxsharinghelper.activity.component.AddMySetDialog
import io.github.lanlacope.nxsharinghelper.activity.component.EditCommonInfoDialog
import io.github.lanlacope.nxsharinghelper.activity.component.EditGameInfoDialog
import io.github.lanlacope.nxsharinghelper.activity.component.RemoveGameInfoDialog
import io.github.lanlacope.nxsharinghelper.activity.component.RemoveMySetDialog
import io.github.lanlacope.nxsharinghelper.clazz.InfoManager.CommonInfo
import io.github.lanlacope.nxsharinghelper.clazz.FileSelector
import io.github.lanlacope.nxsharinghelper.clazz.InfoManager.GameInfo
import io.github.lanlacope.nxsharinghelper.clazz.InfoManager
import io.github.lanlacope.nxsharinghelper.ui.theme.Gray
import io.github.lanlacope.nxsharinghelper.ui.theme.AppTheme
import io.github.lanlacope.nxsharinghelper.widgit.Column
import java.io.File

class EditGameInfoActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MySetList()
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MySetList(
) {
    val fileSelector = FileSelector(LocalContext.current)
    val files = remember {
        mutableStateOf(fileSelector.getMySetFiles())
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        val BUTTON_PADDING = 20.dp
        val shown = remember {
            mutableStateOf(false)
        }
        Button(
            onClick = {
                shown.value = true
            },
            modifier = Modifier
                .width(200.dp)
                .wrapContentHeight()
                .align(Alignment.CenterHorizontally)
                .padding(
                    top = BUTTON_PADDING,
                    bottom = BUTTON_PADDING
                )
        ) {
            Text(
                text = stringResource(id = R.string.dialog_poitive_add),
                modifier = Modifier.wrapContentSize()
            )
        }
        HorizontalPager(
            state = rememberPagerState(
                pageCount = { files.value.size }
            ),
            modifier = Modifier.fillMaxSize()

        ) { page ->
            MySet(
                file = files.value[page]
            )
        }

        AddMySetDialog(
            shown = shown,
            files = files
        )
    }
}

@Composable
private fun MySet(
    file: File
) {
    val isRemoved = remember {
        mutableStateOf(!file.exists())
    }

    if (!isRemoved.value) {

        val shownAddDialog = remember {
            mutableStateOf(false)
        }

        val infoManager = InfoManager(LocalContext.current)
        val common by remember {
            mutableStateOf(infoManager.getCommonInfo(file))
        }
        val games = remember {
            mutableStateOf(infoManager.getGameInfo(file))
        }

        Box(
            modifier = Modifier.fillMaxSize()
        ) {

            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    MySetCommon(
                        common = common,
                        fileName = file.name,
                        isParentRemoved = isRemoved
                    )
                }
                items(games.value) { game ->
                    MySetItem(
                        gameInfo = game,
                        fileName = file.name
                    )
                }
            }

            val FAB_PADDING = 30.dp

            FloatingActionButton(
                containerColor = MaterialTheme.colorScheme.secondary,
                onClick = {
                    shownAddDialog.value = true
                },
                modifier = Modifier
                    .wrapContentSize()
                    .padding(
                        end = FAB_PADDING,
                        bottom = FAB_PADDING
                    )
                    .align(Alignment.BottomEnd)

            ) {
                Image(
                    painter = painterResource(id = R.drawable.baseline_add_24),
                    contentDescription = "Add",
                    modifier = Modifier
                        .wrapContentSize()
                        .padding(all = 8.dp)

                )
            }
        }

        AddGameInfoDialog(
            shown = shownAddDialog,
            fileName = file.name,
            games = games
        )
    } else {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = stringResource(id = R.string.summary_myset_removed),
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                style = TextStyle(
                    color = Gray
                ),
                modifier = Modifier
                    .wrapContentSize()
                    .align(Alignment.Center)

            )
        }
    }
}

@Composable
private fun MySetCommon(
    common: CommonInfo,
    fileName: String,
    isParentRemoved: MutableState<Boolean>
) {
    val shownEditDialog = remember {
        mutableStateOf(false)
    }
    val shownRemoveDialog = remember {
        mutableStateOf(false)
    }

    val title = remember {
        mutableStateOf(common.title)
    }
    val text = remember {
        mutableStateOf(common.text)
    }

    Column(
        onClick = {
            shownEditDialog.value = true
        },
        onLongClick = {
            shownRemoveDialog.value = true
        },
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()

    ) {
        Text(
            text = title.value,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .align(Alignment.Start)
        )
        Text(
            text = text.value,
            fontSize = 12.sp,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .align(Alignment.Start)
        )
    }

    EditCommonInfoDialog(
        shown = shownEditDialog,
        fileName = fileName,
        title = title,
        text = text
    )

    RemoveMySetDialog(
        shown = shownRemoveDialog,
        fileName = fileName,
        isParentRemoved = isParentRemoved
    )
}



@Composable
private fun MySetItem(
    gameInfo: GameInfo,
    fileName: String
) {
    val isRemoved = remember {
        mutableStateOf(false)
    }
    val shownEditDialog = remember {
        mutableStateOf(false)
    }
    val shownRemoveDialog = remember {
        mutableStateOf(false)
    }

    val title = remember {
        mutableStateOf(gameInfo.title)
    }
    val id = gameInfo.id

    val text = remember {
        mutableStateOf(gameInfo.text)
    }

    if (!isRemoved.value) {
        Column(
            onClick = {
                shownEditDialog.value = true
            },
            onLongClick = {
                shownRemoveDialog.value = true
            },
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()


        ) {
            Text(
                text = title.value,
                fontSize = 24.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .align(Alignment.Start)
            )
            Text(
                text = text.value,
                fontSize = 12.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .align(Alignment.Start)

            )
        }
    }

    EditGameInfoDialog(
        shown = shownEditDialog,
        fileName = fileName,
        title = title,
        id = id,
        text = text
    )

    RemoveGameInfoDialog(
        shown = shownRemoveDialog,
        fileName = fileName,
        id = id,
        isParentRemoved = isRemoved
    )
}