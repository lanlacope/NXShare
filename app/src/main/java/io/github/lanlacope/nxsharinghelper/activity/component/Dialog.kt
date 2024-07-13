package io.github.lanlacope.nxsharinghelper.activity.component

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import io.github.lanlacope.nxsharinghelper.R
import io.github.lanlacope.nxsharinghelper.clazz.FileEditor
import io.github.lanlacope.nxsharinghelper.clazz.InfoManager.GameInfo
import io.github.lanlacope.nxsharinghelper.ui.theme.Gray
import io.github.lanlacope.nxsharinghelper.ui.theme.NXSharingHelperTheme
import io.github.lanlacope.nxsharinghelper.widgit.Box
import java.io.File

@Composable
fun AddMySetDialog(
    shown: MutableState<Boolean>,
    files: MutableState<List<File>>
) {
    var shownWarning by remember {
        mutableStateOf(false)
    }
    val fileEditor = FileEditor(LocalContext.current)

    val title = remember {
        mutableStateOf("")
    }

    LaunchedEffect(shown.value) {
        if (shown.value) {
            shownWarning = false
            title.value = ""
        }
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
                    DialogTitle(text = stringResource(id = R.string.dialog_title_myset_add))

                    DialogTextField(
                        text = title,
                        hint = stringResource(id = R.string.hint_myset_title)
                    )

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()

                    ) {

                        DialogWarning(
                            text = stringResource(id = R.string.dialog_warning_exists),
                            shown = shownWarning
                        )

                        TextButton(
                            onClick = {
                                val result = fileEditor.addMySet(title.value)
                                if (result.isSuccess) {
                                    files.value += result.getOrNull()!!
                                    shown.value = false
                                } else {
                                    shownWarning = true
                                }
                            },
                            modifier = Modifier
                                .wrapContentSize()
                        ) {
                            Text(
                                text = stringResource(id = R.string.dialog_poitive_add),
                                modifier = Modifier
                                    .wrapContentSize()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RemoveMySetDialog(
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
                    DialogTitle(text = stringResource(id = R.string.dialog_title_comfirm))

                    DialogMessage(text = stringResource(id = R.string.confirm_text_myset_remove))

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
                            text = stringResource(id = R.string.dialog_poitive_apply),
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
fun EditCommonInfoDialog(
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
                    DialogTitle(text = stringResource(id = R.string.dialog_title_myset_edit))

                    DialogTextField(
                        text = title,
                        hint = stringResource(id = R.string.hint_myset_title)
                    )

                    DialogTextField(
                        text = text,
                        hint = stringResource(id = R.string.hint_myset_text),
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
                            text = stringResource(id = R.string.dialog_poitive_apply),
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
fun AddGameInfoDialog(
    shown: MutableState<Boolean>,
    fileName: String,
    games: MutableState<List<GameInfo>>
) {
    var shownWarning by remember {
        mutableStateOf(true)
    }

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

    LaunchedEffect(shown.value) {
        if (shown.value) {
            shownWarning = false
            title.value = ""
            id.value = ""
            text.value = ""
        }
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
                    DialogTitle(text = stringResource(id = R.string.dialog_title_game_add))
                    DialogTextField(
                        text = id,
                        hint = stringResource(id = R.string.hint_game_id)
                    )

                    DialogTextField(
                        text = title,
                        hint = stringResource(id = R.string.hint_game_title)
                    )

                    DialogTextField(
                        text = text,
                        hint = stringResource(id = R.string.hint_game_text),
                        singleLine = false
                    )

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()

                    ) {
                        DialogWarning(
                            text = stringResource(id = R.string.dialog_warning_exists),
                            shown = shownWarning
                        )

                        TextButton(
                            onClick = {
                                val result =
                                    fileEditor.addGameInfo(
                                        fileName,
                                        title.value,
                                        id.value,
                                        text.value
                                    )
                                if (result.isSuccess) {
                                    games.value += result.getOrNull()!!
                                    shown.value = false
                                } else {
                                    shownWarning = true
                                }
                            },
                            modifier = Modifier
                                .wrapContentSize()

                        ) {
                            Text(
                                text = stringResource(id = R.string.dialog_poitive_add),
                                modifier = Modifier
                                    .wrapContentSize()

                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EditGameInfoDialog(
    shown: MutableState<Boolean>,
    fileName: String,
    title: MutableState<String>,
    id: String,
    text: MutableState<String>
) {
    val fileEditor = FileEditor(LocalContext.current)
    val clipboardManager = LocalClipboardManager.current

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
                    DialogTitle(text = stringResource(id = R.string.hint_game_id))

                    Box(
                        onLongClick = {
                            clipboardManager.setText(AnnotatedString(id))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .align(Alignment.Start)
                            .padding(
                                start = 12.dp
                            )
                    ) {
                        Text(
                            text = id,
                            maxLines = 1,
                            modifier = Modifier
                                .wrapContentSize()

                        )
                    }

                    DialogTextField(
                        text = title,
                        hint = stringResource(id = R.string.hint_game_title)
                    )

                    DialogTextField(
                        text = text,
                        hint = stringResource(id = R.string.hint_game_text),
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
                            text = stringResource(id = R.string.dialog_poitive_apply),
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
fun RemoveGameInfoDialog(
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
                    DialogTitle(text = stringResource(id = R.string.dialog_title_comfirm))

                    DialogMessage(text = stringResource(id = R.string.confirm_text_game_remove))

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
                            text = stringResource(id = R.string.dialog_poitive_apply),
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
private fun DialogTitle(text: String) {
    Text(
        text = text,
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(all = 10.dp)

    )
}

@Composable
private fun DialogMessage(text: String) {
    Text(
        text = text,
        fontSize = 16.sp,
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(start = 16.dp)

    )
}

@Composable
private fun DialogWarning(
    text: String,
    shown: Boolean
) {
    Text(
        text = if (shown) text else "",
        fontSize = 12.sp,
        fontStyle = FontStyle.Italic,
        style = TextStyle(
            color = MaterialTheme.colorScheme.error
        ),
        modifier = Modifier
            .wrapContentSize()
            .padding(start = 16.dp)

    )
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
                fontWeight = FontWeight.Bold,
                style = TextStyle(
                    color = Gray
                ),
                modifier = Modifier.wrapContentSize()
            )
        },
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
                id = "XXXXXXXXXXXXXXXXXXXXXX",
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
                id = "XXXXXXXXXXXXXXXXXXXXXX",
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

            val list = remember {
                mutableStateOf(listOf(File(""), File("")))
            }

            AddMySetDialog(
                shown = shown,
                files = list
            )
        }
    }
}

