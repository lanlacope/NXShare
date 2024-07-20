package io.github.lanlacope.nxsharinghelper.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.lanlacope.nxsharinghelper.R
import io.github.lanlacope.nxsharinghelper.activity.component.AddGameInfoDialog
import io.github.lanlacope.nxsharinghelper.activity.component.AddMySetDialog
import io.github.lanlacope.nxsharinghelper.activity.component.DrawDownAnimated
import io.github.lanlacope.nxsharinghelper.activity.component.EditCommonInfoDialog
import io.github.lanlacope.nxsharinghelper.activity.component.EditGameInfoDialog
import io.github.lanlacope.nxsharinghelper.activity.component.RemoveGameInfoDialog
import io.github.lanlacope.nxsharinghelper.activity.component.RemoveMySetDialog
import io.github.lanlacope.nxsharinghelper.activity.component.ComponentValue
import io.github.lanlacope.nxsharinghelper.clazz.InfoManager.CommonInfo
import io.github.lanlacope.nxsharinghelper.clazz.InfoManager.GameInfo
import io.github.lanlacope.nxsharinghelper.clazz.rememberFileEditor
import io.github.lanlacope.nxsharinghelper.clazz.rememberFileSelector
import io.github.lanlacope.nxsharinghelper.clazz.rememberInfoManager
import io.github.lanlacope.nxsharinghelper.ui.theme.AppTheme
import io.github.lanlacope.nxsharinghelper.widgit.Column
import io.github.lanlacope.nxsharinghelper.widgit.LazyHorizontalPager
import io.github.lanlacope.nxsharinghelper.widgit.animatedItems
import io.github.lanlacope.nxsharinghelper.widgit.animatedPagerItems
import io.github.lanlacope.nxsharinghelper.widgit.Button
import kotlinx.coroutines.launch
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

@Composable
private fun MySetList(
) {
    val scope = rememberCoroutineScope()
    val fileSelector = rememberFileSelector()
    val files = remember {
        fileSelector.getMySetFiles().toMutableStateList()
    }

    val listState = rememberLazyListState()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        val BUTTON_PADDING = 20.dp
        val shownAddMysetDialog = remember {
            mutableStateOf(false)
        }
        val shownImportMysetDialog = remember {
            mutableStateOf(false)
        }
        Button(
            onClick = {
                shownAddMysetDialog.value = true
            },
            onLongClick = {
                shownImportMysetDialog.value = true
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

        LazyHorizontalPager(
            state = listState,
            modifier = Modifier.fillMaxSize()
        ) {
            animatedPagerItems(
                items = files,
                key = { it.name }
            ) { file ->
                val infoManager = rememberInfoManager()
                val fileEditor = rememberFileEditor()
                val common by remember {
                    mutableStateOf(infoManager.getCommonInfo(file))
                }

                // MySetCommonのコールバックを定義しておく
                val editCommonInfo: (String, String) -> Unit = { title, text ->
                    fileEditor.editCommonInfo(file.name, title, text)
                }
                val removeMySet: () -> Unit = {
                    fileEditor.removeMySet(file.name)
                    files.remove(file)
                }
                Column {
                    MySetCommon(
                        common = common,
                        editCommonInfo = editCommonInfo,
                        removeMySet = removeMySet
                    )
                    MySet(
                        file = file
                    )
                }
            }
        }

        val scrollLastItem: () -> Unit = {
            scope.launch {
                listState.animateScrollToItem(files.size)
            }
        }
        AddMySetDialog(
            shown = shownAddMysetDialog,
            files = files,
            onSucceseful = scrollLastItem
        )

        // TODO: ImportMySetDialog
    }
}

@Composable
private fun MySetCommon(
    common: CommonInfo,
    editCommonInfo: (String, String) -> Unit,
    removeMySet: () -> Unit
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
                .padding(
                    start = ComponentValue.DISPLAY_PADDING_START,
                    end = ComponentValue.DISPLAY_PADDING_END
                )
        )
        Text(
            text = text.value,
            fontSize = 12.sp,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .align(Alignment.Start)
                .padding(
                    start = ComponentValue.DISPLAY_PADDING_START,
                    end = ComponentValue.DISPLAY_PADDING_END,
                    bottom = 20.dp
                )
        )
    }

    EditCommonInfoDialog(
        shown = shownEditDialog,
        title = title,
        text = text,
        editCommonInfo = editCommonInfo
    )

    RemoveMySetDialog(
        shown = shownRemoveDialog,
        removeMySet = removeMySet
    )
}

@Composable
private fun MySet(
    file: File
) {
    val shownAddDialog = remember {
        mutableStateOf(false)
    }

    val infoManager = rememberInfoManager()
    val games = remember {
        infoManager.getGameInfo(file).toMutableStateList()
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
        ) {
            animatedItems(
                items = games,
                key = { it.id }
            ) { game ->
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

    DrawDownAnimated(visible = !isRemoved.value) {
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
                    .padding(
                        start = ComponentValue.DISPLAY_PADDING_START,
                        end = ComponentValue.DISPLAY_PADDING_END
                    )
            )
            Text(
                text = text.value,
                fontSize = 12.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .align(Alignment.Start)
                    .padding(
                        start = ComponentValue.DISPLAY_PADDING_START,
                        end = ComponentValue.DISPLAY_PADDING_END,
                        bottom = 10.dp
                    )
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