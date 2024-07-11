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

        MySetListDialog(
            shown = shown,
            files = files
        )
    }
}

@Composable
private fun MySetListDialog(
    shown: MutableState<Boolean>,
    files: MutableState<List<File>>
) {
    val fileEditor = FileEditor(LocalContext.current)

    var name by remember {
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
                    val TEXT_PADDING = 8.dp

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        placeholder = {
                            Text(
                                text = stringResource(id = R.string.hint_title),
                                modifier = Modifier.wrapContentSize()
                            )
                        },
                        minLines = 1,
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .align(Alignment.Start)
                            .padding(all = TEXT_PADDING)
                    )

                    TextButton(
                        onClick = {
                            val result = fileEditor.addMySet(name)
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
private fun MySet(
    file: File
) {
    val infoManager = InfoManager(LocalContext.current)
    val common = infoManager.getCommonInfo(file)
    val games = remember {
        mutableStateOf(infoManager.getGameInfo(file))
    }

    val shown = remember {
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
                    fileName = file.name
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
                shown.value = true
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
    
    MySetDialog(
        shown = shown, 
        fileName = file.name, 
        games = games
    ) 
}

@Composable
private fun MySetDialog(
    shown: MutableState<Boolean>,
    fileName: String,
    games:  MutableState<List<GameInfo>>
) {
    val fileEditor = FileEditor(LocalContext.current)

    var title by remember {
        mutableStateOf("")
    }
    var hash by remember {
        mutableStateOf("")
    }
    var text by remember {
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
                    val TEXT_PADDING = 8.dp

                    OutlinedTextField(
                        value = hash,
                        onValueChange = { hash = it },
                        placeholder = {
                            Text(
                                text = stringResource(id = R.string.hint_hash),
                                modifier = Modifier.wrapContentSize()
                            )
                        },
                        minLines = 1,
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .align(Alignment.Start)
                            .padding(all = TEXT_PADDING)

                    )

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        placeholder = {
                            Text(
                                text = stringResource(id = R.string.hint_title),
                                modifier = Modifier.wrapContentSize()
                            )
                        },
                        minLines = 1,
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .align(Alignment.Start)
                            .padding(all = TEXT_PADDING)

                    )

                    OutlinedTextField(
                        value = text,
                        onValueChange = { text = it },
                        placeholder = {
                            Text(
                                text = stringResource(id = R.string.hint_text),
                                modifier = Modifier.wrapContentSize()
                            )
                        },
                        minLines = 1,
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .align(Alignment.Start)
                            .padding(all = TEXT_PADDING)
                    )

                    TextButton(
                        onClick = {
                            val result = fileEditor.addGameInfo(fileName, title, hash, text)
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
                            text = "追加",
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
private fun MySetCommon(
    common: CommonInfo,
    fileName: String
) {
    val shown = remember {
        mutableStateOf(false)
    }
    val text = remember {
        mutableStateOf(common.text)
    }

    Column(
        onClick = {
            shown.value = true
        },
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()

    ) {
        Text(
            text = common.name,
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

    MySetCommonDialog(
        shown = shown,
        fileName = fileName,
        text = text
    )
}

@Composable
private fun MySetCommonDialog(
    shown: MutableState<Boolean>,
    fileName: String,
    text: MutableState<String>
) {
    val fileEditor = FileEditor(LocalContext.current)

    /*
    var _title by remember {
        mutableStateOf(title.value)
    }

     */
    var _text by remember {
        mutableStateOf(text.value)
    }

    LaunchedEffect(shown.value) {
        if (shown.value) {
            // _title = title.value
            _text = text.value
        }
    }

    if (shown.value) {
        Dialog(
            onDismissRequest = {
                shown.value = false
                //title.value = _title
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
                    val TEXT_PADDING = 8.dp
                    /*
                    OutlinedTextField(
                        value = title.value,
                        onValueChange = { title.value = it },
                        placeholder = {
                            Text(
                                text = stringResource(id = R.string.summary_title),
                                modifier = Modifier.wrapContentSize()
                            )
                        },
                        minLines = 1,
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .align(Alignment.Start)
                            .padding(all = TEXT_PADDING)

                    )

                     */
                    OutlinedTextField(
                        value = text.value,
                        onValueChange = { text.value = it },
                        placeholder = {
                            Text(
                                text = stringResource(id = R.string.hint_text),
                                modifier = Modifier.wrapContentSize()
                            )
                        },
                        minLines = 1,
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .align(Alignment.Start)
                            .padding(all = TEXT_PADDING)
                    )

                    TextButton(
                        onClick = {
                            shown.value = false
                            fileEditor.editCommonInfo(fileName, text.value)
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
private fun MySetItem(
    gameInfo: GameInfo,
    fileName: String
) {
    val title = remember {
        mutableStateOf(gameInfo.title)
    }
    val hash = gameInfo.hash

    val text = remember {
        mutableStateOf(gameInfo.text)
    }

    val shown = remember {
        mutableStateOf(false)
    }

    Column(
        onClick = {
            shown.value = true
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

    MySetItemDialog(
        shown = shown,
        fileName = fileName,
        title = title,
        hash = hash,
        text = text
    )
}

@Composable
private fun MySetItemDialog(
    shown: MutableState<Boolean>,
    fileName: String,
    title: MutableState<String>,
    hash: String,
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
                    val TEXT_PADDING = 8.dp
                    Text(
                        text = hash,
                        minLines = 1,
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .align(Alignment.Start)
                            .padding(all = TEXT_PADDING)

                    )

                    OutlinedTextField(
                        value = title.value,
                        onValueChange = { title.value = it },
                        placeholder = {
                            Text(
                                text = stringResource(id = R.string.hint_title),
                                modifier = Modifier.wrapContentSize()
                            )
                        },
                        minLines = 1,
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .align(Alignment.Start)
                            .padding(all = TEXT_PADDING)

                    )
                    OutlinedTextField(
                        value = text.value,
                        onValueChange = { text.value = it },
                        placeholder = {
                            Text(
                                text = stringResource(id = R.string.hint_text),
                                modifier = Modifier.wrapContentSize()
                            )
                        },
                        minLines = 1,
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .align(Alignment.Start)
                            .padding(all = TEXT_PADDING)
                    )

                    TextButton(
                        onClick = {
                            shown.value = false
                            fileEditor.editGameInfo(fileName, title.value, hash, text.value)
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

            MySetItemDialog(
                shown = shown,
                fileName = "name",
                title = title,
                hash = "hash",
                text = text
            )
        }
    }
}