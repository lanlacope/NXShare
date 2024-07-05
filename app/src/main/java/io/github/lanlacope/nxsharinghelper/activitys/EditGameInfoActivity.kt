package io.github.lanlacope.nxsharinghelper.activitys

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import io.github.lanlacope.nxsharinghelper.R
import io.github.lanlacope.nxsharinghelper.classes.CommonInfo
import io.github.lanlacope.nxsharinghelper.classes.FileEditer
import io.github.lanlacope.nxsharinghelper.classes.GameInfo
import io.github.lanlacope.nxsharinghelper.classes.SHARE_JSON_PROPATY
import io.github.lanlacope.nxsharinghelper.classes.TypeInfo
import io.github.lanlacope.nxsharinghelper.classes.removeStringsForFile
import io.github.lanlacope.nxsharinghelper.ui.theme.NXSharingHelperTheme
import org.json.JSONArray
import org.json.JSONObject
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
                    MySetList(
                        addMyset = addMySet,
                        onApplyCommon = onApplyCommon,
                        onApplyGame = onApplyGame
                    )
                }

            }
        }
    }
    
    private val addMySet: (String) -> Unit = { name ->
        val fileEditer = FileEditer(this)
        val folder = fileEditer.getTypeFolder()
        val file = File(folder, "${removeStringsForFile(name)}.json")
        if (!file.exists()) {
            file.mkdirs()
            val jsonObject = JSONObject().apply {
                put(SHARE_JSON_PROPATY.DATA_NAME, name)
                put(SHARE_JSON_PROPATY.COMMON_TEXT, "")
                put(SHARE_JSON_PROPATY.GAME_DATA, JSONArray())
            }
            file.writeText(jsonObject.toString())
        }
    }

    private val onApplyCommon: (String, String) -> Unit = { fileName, text ->
        val fileEditer = FileEditer(this)
        val file = fileEditer.getTypeFile(fileName)
        val jsonObject = JSONObject(file.readText())
        jsonObject.put(SHARE_JSON_PROPATY.COMMON_TEXT, text)
        file.writeText(jsonObject.toString())
    }

    private val onApplyGame: (String, String, String, String) -> Unit = { fileName, title, hash, text ->
        val fileEditer = FileEditer(this)
        val file = fileEditer.getTypeFile(fileName)
        val jsonObject = JSONObject(file.readText())
        val jsonArray = jsonObject.getJSONArray(SHARE_JSON_PROPATY.DATA_NAME)

        val gameData = JSONObject().apply {
            put(SHARE_JSON_PROPATY.GAME_TITLE, title)
            put(SHARE_JSON_PROPATY.GAME_HASH, hash)
            put(SHARE_JSON_PROPATY.GAME_TEXT, text)
        }
        jsonArray.put(gameData)
        jsonObject.put(SHARE_JSON_PROPATY.DATA_NAME, jsonArray)
        file.writeText(jsonObject.toString())
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MySetList(
    addMyset: (String) -> Unit,
    onApplyCommon: (String, String) -> Unit,
    onApplyGame: (String, String, String, String) -> Unit
) {
    val context = LocalContext.current
    val typeInfo = TypeInfo(context)

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        val BUTTON_PADDING = 20.dp
        var shown by remember {
            mutableStateOf(false)
        }
        Button(
            onClick = {
                shown = true
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
            Text(text = "追加")
        }
        HorizontalPager(
            state = rememberPagerState(
                pageCount = { typeInfo.fileNames.size }
            ),
            modifier = Modifier.fillMaxSize()

        ) { page ->
            MySet(
                file = typeInfo.typeFiles[page],
                onApplyCommon = onApplyCommon,
                onApplyGame = onApplyGame
            )
        }

        if (shown) {
            Dialog(
                onDismissRequest = {
                    shown = false
                }
            ) {
                var name by remember {
                    mutableStateOf("")
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        minLines = 1,
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .align(Alignment.Start)
                    )
                    TextButton(
                        onClick = {
                            shown = false
                            addMyset(name)
                        },
                        modifier = Modifier
                            .wrapContentSize()
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
private fun MySet(
    file: File,
    onApplyCommon: (String, String) -> Unit,
    onApplyGame: (String, String, String, String) -> Unit
) {
    val fileEditer = FileEditer(LocalContext.current)
    val common = fileEditer.getCommonInfo(file)
    var games by remember {
        mutableStateOf(fileEditer.getGameInfo(file))
    }

    var shown by remember {
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
                    onApply = onApplyCommon
                )
            }
            items(games) { game ->
                MySetItem(
                    gameInfo = game,
                    fileName = file.name,
                    onApply = onApplyGame
                )
            }
        }

        FloatingActionButton(
            onClick = {
                shown = true
            },
            modifier = Modifier
                .size(80.dp)
                .padding(30.dp)
                .align(Alignment.BottomEnd)

        ) {
            Image(
                painter = painterResource(id = R.drawable.baseline_add_24),
                contentDescription = "Add",
                modifier = Modifier.fillMaxSize()
            )
        }

        if (shown) {
            Dialog(
                onDismissRequest = {
                    shown = false
                }
            ) {
                var title by remember {
                    mutableStateOf("")
                }
                var hash by remember {
                    mutableStateOf("")
                }
                var text by remember {
                    mutableStateOf("")
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                ) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        minLines = 1,
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .align(Alignment.Start)

                    )
                    OutlinedTextField(
                        value = hash,
                        onValueChange = { hash = it },
                        minLines = 1,
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .align(Alignment.Start)

                    )
                    OutlinedTextField(
                        value = text,
                        onValueChange = { text = it },
                        minLines = 1,
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .align(Alignment.Start)
                    )

                    TextButton(
                        onClick = {
                            shown = false
                            onApplyGame(file.name, title, hash, text)
                            val gameData = JSONObject().apply {
                                put(SHARE_JSON_PROPATY.GAME_TITLE, title)
                                put(SHARE_JSON_PROPATY.GAME_HASH, hash)
                                put(SHARE_JSON_PROPATY.GAME_TEXT, text)
                            }
                            games = games + GameInfo(gameData)
                        },
                        modifier = Modifier
                            .wrapContentSize()
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
    fileName: String,
    onApply: (String, String) -> Unit
) {
    var shown by remember {
        mutableStateOf(false)
    }
    var text by remember {
        mutableStateOf(common.text)
    }

    var _text by remember {
        mutableStateOf(text)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .clickable {
                _text = text
                shown = true
            }
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
            text = text,
            fontSize = 12.sp,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .align(Alignment.Start)
        )
    }

    if (shown) {
        Dialog(
            onDismissRequest = {
                shown = false
                text = _text
            }
        ) {
            Column {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    minLines = 1,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .align(Alignment.Start)
                )

                TextButton(
                    onClick = {
                        onApply(fileName, text)
                    },
                    modifier = Modifier
                        .wrapContentSize()
                ) {
                    Text(
                        text = "適用",
                        modifier = Modifier
                            .wrapContentSize()
                    )
                }
            }
        }
    }
}

@Composable
private fun MySetItem(
    gameInfo: GameInfo,
    fileName: String,
    onApply: (String, String, String, String) -> Unit
) {
    var title by remember {
        mutableStateOf(gameInfo.title)
    }
    var hash by remember {
        mutableStateOf(gameInfo.hash)
    }
    var text by remember {
        mutableStateOf(gameInfo.text)
    }

    var shown by remember {
        mutableStateOf(false)
    }
    var _title by remember {
        mutableStateOf(title)
    }
    var _hash by remember {
        mutableStateOf(hash)
    }
    var _text by remember {
        mutableStateOf(text)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clickable {
                _title = title
                _hash = hash
                _text = text
                shown = true
            }
    ) {
        Text(
            text = title,
            fontSize = 24.sp,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .align(Alignment.Start)
        )
        Text(
            text = text,
            fontSize = 12.sp,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .align(Alignment.Start)

        )
    }

    if (shown) {
        Dialog(
            onDismissRequest = {
                shown = false
                title = _title
                hash = _hash
                text = _text
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    minLines = 1,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .align(Alignment.Start)

                )
                OutlinedTextField(
                    value = hash,
                    onValueChange = { hash = it },
                    minLines = 1,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .align(Alignment.Start)

                )
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    minLines = 1,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .align(Alignment.Start)
                )

                TextButton(
                    onClick = {
                    onApply(fileName, title, hash, text)
                    },
                    modifier = Modifier
                        .wrapContentSize()
                ) {
                    Text(
                        text = "適用",
                        modifier = Modifier
                            .wrapContentSize()
                    )
                }
            }
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
private fun MySetListPreViewLight() {
    NXSharingHelperTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            MySetList(
                addMyset = { _ ->

                },
                onApplyCommon = { _,_, ->

                },
                onApplyGame =  { _,_,_,_, ->

                }
            )
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun MySetListPreViewDark() {
    val onApply: (String, String, String, String) -> Unit = { _, _, _, _ -> }
    NXSharingHelperTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            MySetList(
                addMyset = { _ ->

                },
                onApplyCommon = { _,_, ->

                },
                onApplyGame =  { _,_,_,_, ->

                }
            )
        }
    }
}