package io.github.lanlacope.nxsharinghelper.activity

import android.content.res.Configuration
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import io.github.lanlacope.nxsharinghelper.R
import io.github.lanlacope.nxsharinghelper.clazz.CommonInfo
import io.github.lanlacope.nxsharinghelper.clazz.FileEditor
import io.github.lanlacope.nxsharinghelper.clazz.FileSelector
import io.github.lanlacope.nxsharinghelper.clazz.GameInfo
import io.github.lanlacope.nxsharinghelper.clazz.InfoManager
import io.github.lanlacope.nxsharinghelper.ui.theme.Gray
import io.github.lanlacope.nxsharinghelper.ui.theme.NXSharingHelperTheme
import io.github.lanlacope.nxsharinghelper.widgit.Column
import java.io.File

class EditGameInfoActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            NXSharingHelperTheme {
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
        mutableStateOf(fileSelector.getTypeFiles())
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
                text = stringResource(id = R.string.button_add),
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
        val infoManager = InfoManager(LocalContext.current)
        val common by remember {
            mutableStateOf(infoManager.getCommonInfo(file))
        }
        val games = remember {
            mutableStateOf(infoManager.getGameInfo(file))
        }

        val shownAddDialog = remember {
            mutableStateOf(false)
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
                text = "Removed",
                fontSize = 32.sp,
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
    val title = remember {
        mutableStateOf(gameInfo.title)
    }
    val id = gameInfo.id

    val text = remember {
        mutableStateOf(gameInfo.text)
    }

    val isRemoved = remember {
        mutableStateOf(false)
    }

    val shownEditDialog = remember {
        mutableStateOf(false)
    }

    val shownRemoveDialog = remember {
        mutableStateOf(false)
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

@Composable
private fun AddMySetDialog(
    shown: MutableState<Boolean>,
    files: MutableState<List<File>>
) {
    val fileEditor = FileEditor(LocalContext.current)

    val title = remember {
        mutableStateOf("")
    }

    if (shown.value) {
        Dialog(
            onDismissRequest = {
                shown.value = false
            },
        ) {
            Surface(
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier
                    .wrapContentSize()

            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                ) {
                    DialogTextField(
                        text = title,
                        hint = stringResource(id = R.string.hint_title)
                    )

                    TextButton(
                        onClick = {
                            val result = fileEditor.addMySet(title.value)
                            if (result.isSuccess) {
                                files.value += result.getOrNull()!!
                                shown.value = false
                            }
                        },
                        modifier = Modifier
                            .wrapContentSize()
                            .align(Alignment.End)
                    ) {
                        Text(
                            text = stringResource(id = R.string.button_add),
                            modifier = Modifier
                                .wrapContentSize()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RemoveMySetDialog(
    shown: MutableState<Boolean>,
    fileName: String,
    isParentRemoved: MutableState<Boolean>
) {
    val fileEditor = FileEditor(LocalContext.current)

    if (shown.value) {
        Dialog(
            onDismissRequest = {
                shown.value = false
            },
        ) {
            Surface(
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier
                    .wrapContentSize()

            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                ) {
                    Text(
                        text = stringResource(id = R.string.confirm_title),
                        fontSize = 32.sp,
                        minLines = 1,
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .align(Alignment.Start)
                            .padding(all = 10.dp)

                    )

                    Text(
                        text = stringResource(id = R.string.confirm_text_myset),
                        fontSize = 16.sp,
                        minLines = 1,
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .align(Alignment.Start)
                            .padding(start = 16.dp)

                    )

                    TextButton(
                        onClick = {
                            shown.value = false
                            fileEditor.removeMySet(fileName)
                            isParentRemoved.value = true
                        },
                        modifier = Modifier
                            .wrapContentSize()
                            .align(Alignment.End)
                    ) {
                        Text(
                            text = stringResource(id = R.string.button_apply),
                            modifier = Modifier
                                .wrapContentSize()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EditCommonInfoDialog(
    shown: MutableState<Boolean>,
    fileName: String,
    title: MutableState<String>,
    text: MutableState<String>
) {
    val fileEditor = FileEditor(LocalContext.current)

    var _title by remember {
        mutableStateOf(title.value)
    }

    var _text by remember {
        mutableStateOf(text.value)
    }

    LaunchedEffect(shown.value) {
        if (shown.value) {
            _title = title.value
            _text = text.value
        }
    }

    if (shown.value) {
        Dialog(
            onDismissRequest = {
                shown.value = false
                title.value = _title
                text.value = _text
            },
        ) {
            Surface(
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier
                    .wrapContentSize()

            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                ) {
                    DialogTextField(
                        text = title,
                        hint = stringResource(id = R.string.hint_title)
                    )

                    DialogTextField(
                        text = text,
                        hint = stringResource(id = R.string.hint_text),
                        singleLine = false
                    )

                    TextButton(
                        onClick = {
                            shown.value = false
                            fileEditor.editCommonInfo(fileName, title.value, text.value)
                        },
                        modifier = Modifier
                            .wrapContentSize()
                            .align(Alignment.End)
                    ) {
                        Text(
                            text = stringResource(id = R.string.button_apply),
                            modifier = Modifier
                                .wrapContentSize()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AddGameInfoDialog(
    shown: MutableState<Boolean>,
    fileName: String,
    games:  MutableState<List<GameInfo>>
) {
    val fileEditor = FileEditor(LocalContext.current)

    val title = remember {
        mutableStateOf("")
    }
    val id = remember {
        mutableStateOf("")
    }
    val text = remember {
        mutableStateOf("")
    }

    if (shown.value) {
        Dialog(
            onDismissRequest = {
                shown.value = false
            },
        ) {
            Surface(
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier
                    .wrapContentSize()

            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                ) {
                    DialogTextField(
                        text = id,
                        hint = stringResource(id = R.string.hint_id)
                    )

                    DialogTextField(
                        text = title,
                        hint = stringResource(id = R.string.hint_title)
                    )

                    DialogTextField(
                        text = text,
                        hint = stringResource(id = R.string.hint_text),
                        singleLine = false
                    )

                    TextButton(
                        onClick = {
                            val result = fileEditor.addGameInfo(fileName, title.value, id.value, text.value)
                            if (result.isSuccess) {
                                games.value += result.getOrNull()!!
                                shown.value = false
                            }
                        },
                        modifier = Modifier
                            .wrapContentSize()
                            .align(Alignment.End)

                    ) {
                        Text(
                            text = stringResource(id = R.string.button_add),
                            modifier = Modifier
                                .wrapContentSize()

                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EditGameInfoDialog(
    shown: MutableState<Boolean>,
    fileName: String,
    title: MutableState<String>,
    id: String,
    text: MutableState<String>
) {
    val fileEditor = FileEditor(LocalContext.current)

    var _title by remember {
        mutableStateOf(title.value)
    }
    var _text by remember {
        mutableStateOf(text.value)
    }

    LaunchedEffect(shown.value) {
        if (shown.value) {
            _title = title.value
            _text = text.value
        }
    }

    if (shown.value) {
        Dialog(
            onDismissRequest = {
                shown.value = false
                title.value = _title
                text.value = _text
            },
        ) {
            Surface(
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier
                    .wrapContentSize()

            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                ) {
                    Text(
                        text = id,
                        minLines = 1,
                        maxLines = 1,
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .align(Alignment.Start)
                            .padding(all = 8.dp)

                    )

                    DialogTextField(
                        text = title,
                        hint = stringResource(id = R.string.hint_title)
                    )

                    DialogTextField(
                        text = text,
                        hint = stringResource(id = R.string.hint_text),
                        singleLine = false
                    )

                    TextButton(
                        onClick = {
                            shown.value = false
                            fileEditor.editGameInfo(fileName, title.value, id, text.value)
                        },
                        modifier = Modifier
                            .wrapContentSize()
                            .align(Alignment.End)
                    ) {
                        Text(
                            text = stringResource(id = R.string.button_apply),
                            modifier = Modifier
                                .wrapContentSize()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RemoveGameInfoDialog(
    shown: MutableState<Boolean>,
    fileName: String,
    id: String,
    isParentRemoved: MutableState<Boolean>
) {
    val fileEditor = FileEditor(LocalContext.current)

    if (shown.value) {
        Dialog(
            onDismissRequest = {
                shown.value = false
            },
        ) {
            Surface(
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier
                    .wrapContentSize()

            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                ) {
                    Text(
                        text = stringResource(id = R.string.confirm_title),
                        fontSize = 32.sp,
                        minLines = 1,
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .align(Alignment.Start)
                            .padding(all = 10.dp)

                    )

                    Text(
                        text = stringResource(id = R.string.confirm_text_game),
                        fontSize = 16.sp,
                        minLines = 1,
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .align(Alignment.Start)
                            .padding(start = 16.dp)

                    )

                    TextButton(
                        onClick = {
                            shown.value = false
                            fileEditor.removeGameInfo(fileName, id)
                            isParentRemoved.value = true
                        },
                        modifier = Modifier
                            .wrapContentSize()
                            .align(Alignment.End)
                    ) {
                        Text(
                            text = stringResource(id = R.string.button_apply),
                            modifier = Modifier
                                .wrapContentSize()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DialogTextField(
    text: MutableState<String>,
    hint: String,
    singleLine: Boolean = true
) {
    val maxLines = if (singleLine) 1 else Int.MAX_VALUE

    OutlinedTextField(
        value = text.value,
        onValueChange = { text.value = it },
        placeholder = {
            Text(
                text = hint,
                style = TextStyle(
                    color = Gray
                ),
                modifier = Modifier.wrapContentSize()
            )
        },
        minLines = 1,
        maxLines = maxLines,
        singleLine = singleLine,
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(all = 8.dp)

    )
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
private fun LicensePreViewLight() {
    NXSharingHelperTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            val shown = remember {
                mutableStateOf(true)
            }
            val title = remember {
                mutableStateOf("")
            }
            val text = remember {
                mutableStateOf("")
            }

            EditGameInfoDialog(
                shown = shown,
                fileName = "name",
                title = title,
                id = "hash",
                text = text
            )
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
private fun LicensePreViewLight2() {
    NXSharingHelperTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            val shown = remember {
                mutableStateOf(true)
            }

            RemoveGameInfoDialog(
                shown = shown,
                fileName = "name",
                id = "hash",
                isParentRemoved = shown
            )
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
private fun LicensePreViewLight3() {
    NXSharingHelperTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            val shown = remember {
                mutableStateOf(true)
            }

            RemoveMySetDialog(
                shown = shown,
                fileName = "name",
                isParentRemoved = shown
            )
        }
    }
}