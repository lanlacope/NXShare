package io.github.lanlacope.nxsharinghelper.activity.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import io.github.lanlacope.nxsharinghelper.R
import io.github.lanlacope.nxsharinghelper.clazz.InfoManager.GameInfo
import io.github.lanlacope.nxsharinghelper.clazz.propaty.AppPropaty.SettingJsonPropaty
import io.github.lanlacope.nxsharinghelper.clazz.rememberFileEditor
import io.github.lanlacope.nxsharinghelper.clazz.rememberSettingManager
import io.github.lanlacope.nxsharinghelper.ui.theme.Gray
import io.github.lanlacope.nxsharinghelper.widgit.Box
import io.github.lanlacope.nxsharinghelper.widgit.Row
import java.io.File

@Composable
fun ChangeAppThemeDialog(
    shown: MutableState<Boolean>,
    selectedTheme: String,
    reflection: (String) -> Unit
) {

    val settingManager = rememberSettingManager()

    val dSelectedTheme = rememberSaveable {
        mutableStateOf(selectedTheme)
    }

    LaunchedEffect(shown.value) {
        if (shown.value) {
            dSelectedTheme.value = selectedTheme
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
                    DialogTitle(text = stringResource(id = R.string.dialog_title_theme))

                    SettingJsonPropaty.APP_THEME_LIST.forEach { theme ->
                        ThemeSelector(
                            theme = theme,
                            selectedTheme = dSelectedTheme
                        )
                    }

                    TextButton(
                        onClick = {
                            settingManager.changeAppTheme(dSelectedTheme.value)
                            reflection(dSelectedTheme.value)
                            shown.value = false
                        },
                        modifier = Modifier
                            .wrapContentSize()
                            .align(Alignment.End)
                    ) {
                        Text(
                            text = stringResource(id = R.string.dialog_positive_apply),
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
fun ThemeSelector(
    theme: String,
    selectedTheme: MutableState<String>
) {
    val onClick = {
        selectedTheme.value = theme
    }

    Row(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()

    ) {
        RadioButton(
            selected = theme == selectedTheme.value,
            onClick = onClick,
            modifier = Modifier
                .wrapContentSize()
                .align(Alignment.CenterVertically)
                .padding(start = 10.dp)

        )

        Text(
            text = when (theme) {
                SettingJsonPropaty.THEME_LIGHT -> stringResource(id = R.string.summary_theme_light)
                SettingJsonPropaty.THEME_DARK -> stringResource(id = R.string.summary_theme_dark)
                else -> stringResource(id = R.string.summary_theme_system)
            },
            maxLines = 1,
            minLines = 1,
            modifier = Modifier
                .wrapContentSize()
                .align(Alignment.CenterVertically)

        )
    }
}

@Composable
fun AddMySetDialog(
    shown: MutableState<Boolean>,
    reflection: (File) -> Unit
) {
    var shownWarning by rememberSaveable {
        mutableStateOf(false)
    }
    val fileEditor = rememberFileEditor()

    val title = rememberSaveable {
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
                                    reflection(result.getOrNull()!!)
                                    shown.value = false
                                } else {
                                    shownWarning = true
                                }
                            },
                            modifier = Modifier
                                .wrapContentSize()
                        ) {
                            Text(
                                text = stringResource(id = R.string.dialog_positive_add),
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
    removeMySet: () -> Unit
) {
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
                            removeMySet()
                            shown.value = false
                        },
                        modifier = Modifier
                            .wrapContentSize()
                            .align(Alignment.End)
                    ) {
                        Text(
                            text = stringResource(id = R.string.dialog_positive_apply),
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
fun ImportMySetDialog(
    shown: MutableState<Boolean>,
    reflection: (File) -> Unit
) {
    val fileEditor = rememberFileEditor()

    val title = rememberSaveable {
        mutableStateOf("")
    }

    LaunchedEffect(shown.value) {
        if (shown.value) {
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
                    DialogTitle(text = stringResource(id = R.string.dialog_title_myset_import))

                    DialogTextField(
                        text = title,
                        hint = stringResource(id = R.string.hint_myset_title)
                    )
                    val failedToast = makeToast(text = stringResource(id = R.string.failed_import))
                    val jsonImportResult = rememberImportJsonResult { jsonObject ->
                        val result = fileEditor.importMyset(title.value, jsonObject)
                        if (result.isSuccess) {
                            reflection(result.getOrNull()!!)
                        } else {
                            failedToast.show()
                        }
                    }

                    TextButton(
                        onClick = {
                            jsonImportResult.launch()
                            shown.value = false
                        },
                        modifier = Modifier
                            .wrapContentSize()
                            .align(Alignment.End)
                    ) {
                        Text(
                            text = stringResource(id = R.string.dialog_positive_import),
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
    title: String,
    headText: String,
    tailText: String,
    editCommonInfo: (String, String, String) -> Unit,
    reflection: (String, String, String) -> Unit,
) {
    val dTitle = rememberSaveable {
        mutableStateOf(title)
    }
    val dHeadText = rememberSaveable {
        mutableStateOf(headText)
    }
    val dTailText = rememberSaveable() {
        mutableStateOf(tailText)
    }

    LaunchedEffect(shown.value) {
        if (shown.value) {
            dTitle.value = title
            dHeadText.value = headText
            dTailText.value = tailText
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
                    DialogTitle(text = stringResource(id = R.string.dialog_title_myset_edit))

                    DialogTextField(
                        text = dTitle,
                        hint = stringResource(id = R.string.hint_myset_title)
                    )

                    DialogTextField(
                        text = dHeadText,
                        hint = stringResource(id = R.string.hint_myset_head),
                        singleLine = false
                    )

                    DialogTextField(
                        text = dTailText,
                        hint = stringResource(id = R.string.hint_myset_tail),
                        singleLine = false
                    )

                    TextButton(
                        onClick = {
                            editCommonInfo(dTitle.value, dHeadText.value, dTailText.value)
                            reflection(dTitle.value, dHeadText.value, dTailText.value)
                            shown.value = false
                        },
                        modifier = Modifier
                            .wrapContentSize()
                            .align(Alignment.End)
                    ) {
                        Text(
                            text = stringResource(id = R.string.dialog_positive_apply),
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
    reflection: (GameInfo) -> Unit
) {
    var shownWarning by rememberSaveable {
        mutableStateOf(true)
    }

    val fileEditor = rememberFileEditor()

    val title = rememberSaveable {
        mutableStateOf("")
    }
    val id = rememberSaveable {
        mutableStateOf("")
    }
    val text = rememberSaveable {
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
                                    reflection(result.getOrNull()!!)
                                    shown.value = false
                                } else {
                                    shownWarning = true
                                }
                            },
                            modifier = Modifier
                                .wrapContentSize()

                        ) {
                            Text(
                                text = stringResource(id = R.string.dialog_positive_add),
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
    title: String,
    id: String,
    text: String,
    reflection: (String, String) -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val fileEditor = rememberFileEditor()

    val dTitle = rememberSaveable {
        mutableStateOf(title)
    }
    val dText = rememberSaveable {
        mutableStateOf(text)
    }

    LaunchedEffect(shown.value) {
        if (shown.value) {
            dTitle.value = title
            dText.value = text
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
                        text = dTitle,
                        hint = stringResource(id = R.string.hint_game_title)
                    )

                    DialogTextField(
                        text = dText,
                        hint = stringResource(id = R.string.hint_game_text),
                        singleLine = false
                    )

                    TextButton(
                        onClick = {
                            fileEditor.editGameInfo(fileName, dTitle.value, id, dText.value)
                            reflection(dTitle.value, dText.value)
                            shown.value = false
                        },
                        modifier = Modifier
                            .wrapContentSize()
                            .align(Alignment.End)
                    ) {
                        Text(
                            text = stringResource(id = R.string.dialog_positive_apply),
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
    reflection: () -> Unit
) {
    val fileEditor = rememberFileEditor()

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
                            reflection()
                        },
                        modifier = Modifier
                            .wrapContentSize()
                            .align(Alignment.End)
                    ) {
                        Text(
                            text = stringResource(id = R.string.dialog_positive_apply),
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
fun ImportGameInfoDialog(
    shown: MutableState<Boolean>,
    fileName: String,
    reflection: (List<GameInfo>, Boolean) -> Unit,
) {
    val fileEditor = rememberFileEditor()

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
                    DialogTitle(text = stringResource(id = R.string.dialog_title_game_import))

                    var overwrite by remember {
                        mutableStateOf(false)
                    }
                    
                    Row(
                        onClick = {
                            overwrite = !overwrite
                        }
                    ) {
                        Checkbox(
                            checked = overwrite,
                            onCheckedChange ={
                                overwrite = !overwrite
                            },
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                        Text(
                            text = stringResource(id = R.string.dialog_checkbox_overwrite),
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                    }

                    val failedToast = makeToast(text = stringResource(id = R.string.failed_import))

                    val jsonImportResult = rememberImportJsonResult { jsonObject ->
                        val result = fileEditor.importGameInfo(fileName, jsonObject, overwrite)
                        if (result.isSuccess) {
                            reflection(result.getOrNull()!!, overwrite)
                            shown.value = false
                        } else {
                            failedToast.show()
                        }
                    }

                    TextButton(
                        onClick = {
                            jsonImportResult.launch()
                        },
                        modifier = Modifier
                            .wrapContentSize()
                            .align(Alignment.End)
                    ) {
                        Text(
                            text = stringResource(id = R.string.dialog_positive_import),
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
        maxLines = if (singleLine) 1 else Int.MAX_VALUE,
        singleLine = singleLine,
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(all = 8.dp)

    )
}

