package io.github.lanlacope.nxsharinghelper.activity.component

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.lanlacope.compose.composeable.ui.click.BoxButton
import io.github.lanlacope.compose.ui.animation.DrawUpAnimated
import io.github.lanlacope.compose.ui.animation.FadeInAnimated
import io.github.lanlacope.compose.ui.busy.manu.BusyManu
import io.github.lanlacope.compose.ui.busy.option.texts
import io.github.lanlacope.compose.ui.button.ColumnButton
import io.github.lanlacope.compose.ui.button.CombinedFloatingActionButton
import io.github.lanlacope.compose.ui.lazy.animatedItems
import io.github.lanlacope.compose.ui.lazy.pager.LazyHorizontalPager
import io.github.lanlacope.compose.ui.lazy.pager.animatedPages
import io.github.lanlacope.compose.ui.lazy.pager.helper.PagerIndexHelper
import io.github.lanlacope.compose.ui.text.manu.OutlinedTextFieldManu
import io.github.lanlacope.nxsharinghelper.R
import io.github.lanlacope.nxsharinghelper.activity.component.dialog.GameAddDialog
import io.github.lanlacope.nxsharinghelper.activity.component.dialog.GameEditDialog
import io.github.lanlacope.nxsharinghelper.activity.component.dialog.GameImportDialog
import io.github.lanlacope.nxsharinghelper.activity.component.dialog.GameRemoveDialog
import io.github.lanlacope.nxsharinghelper.activity.component.dialog.MySetEditDialog
import io.github.lanlacope.nxsharinghelper.activity.component.dialog.MySetImportDialog
import io.github.lanlacope.nxsharinghelper.activity.component.dialog.MysetAddDialog
import io.github.lanlacope.nxsharinghelper.activity.component.dialog.MysetRemoveDialog
import io.github.lanlacope.nxsharinghelper.clazz.InfoManager.GameInfo
import io.github.lanlacope.nxsharinghelper.clazz.propaty.ERROR
import io.github.lanlacope.nxsharinghelper.clazz.rememberFileEditor
import io.github.lanlacope.nxsharinghelper.clazz.rememberFileSelector
import io.github.lanlacope.nxsharinghelper.clazz.rememberInfoManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun SettingMyset() {

    Column(modifier = Modifier.fillMaxSize()) {

        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        val fileEditor = rememberFileEditor()
        val fileSelector = rememberFileSelector()

        var mysetAddDialogShown by rememberSaveable { mutableStateOf(false) }
        var mysetImportDialogShown by rememberSaveable { mutableStateOf(false) }
        val listState = rememberLazyListState()

        val files = remember { fileSelector.getMySetFiles().toMutableStateList() }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            PagerIndexHelper(
                items = files,
                state = listState,
                centerRange = 2
            )

            BoxButton(
                onClick = { mysetAddDialogShown = true },
                onLongClick = { mysetImportDialogShown = true },
                modifier = Modifier
                    .padding(4.dp)
                    .clip(CircleShape)
                    .size(50.dp)
                    .background(MaterialTheme.colorScheme.primary)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = Icons.Default.Add.name,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        var mysetAddDialogError by rememberSaveable { mutableStateOf(false) }

        MysetAddDialog(
            expanded = mysetAddDialogShown,
            onConfirm = { title ->
                val result = fileEditor.addMySet(title)
                if (result.isSuccess) {
                    val newFile = result.getOrNull()!!
                    files.add(newFile)
                    scope.launch { listState.animateScrollToItem(files.size) }
                    mysetAddDialogShown = false
                } else {
                    mysetAddDialogError = true
                }
            },
            onCancel = { mysetAddDialogShown = false },
            isError = mysetAddDialogError
        )

        MySetImportDialog(
            expanded = mysetImportDialogShown,
            onConfirm = { title, jsonObject ->
                val result = fileEditor.importMyset(title, jsonObject)
                if (result.isSuccess) {
                    val newFile = result.getOrNull()!!
                    files.add(newFile)
                    scope.launch { listState.animateScrollToItem(files.size) }
                    mysetImportDialogShown = false
                } else {
                    Toast.makeText(
                        context,
                        context.getString(R.string.failed_import),
                        Toast.LENGTH_LONG
                    ).show()
                }
            },
            onCancel = { mysetImportDialogShown = false },
        )

        LazyHorizontalPager(
            state = listState,
            modifier = Modifier.fillMaxSize()
        ) {
            animatedPages(
                items = files,
                key = { it.name }
            ) { file ->

                // ページを削除するためのコールバック
                val removeMySet: () -> Unit = {
                    fileEditor.removeMySet(file.name)
                    files.remove(file)
                }
                MysetListItem(
                    file = file,
                    removeSelf = removeMySet
                )
            }
        }
    }

}

@Composable
private fun MysetListItem(
    file: File,
    removeSelf: () -> Unit,
) {
    Column {
        val fileEditor = rememberFileEditor()
        val infoManager = rememberInfoManager()

        var editMysetDialogShown by rememberSaveable { mutableStateOf(false) }
        var removeMysetDialogShown by rememberSaveable { mutableStateOf(false) }

        val mysetInfo by remember { mutableStateOf(infoManager.getMysetInfo(file)) }
        var title by remember { mutableStateOf(mysetInfo.title) }
        var headText by remember { mutableStateOf(mysetInfo.prefixText) }
        var tailText by remember { mutableStateOf(mysetInfo.suffixText) }

        BoxButton(
            onClick = { editMysetDialogShown = true },
            onLongClick = { removeMysetDialogShown = true },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 80.dp)
                .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f))
        ) {
            GameListHeader(
                title = title,
                prefixText = headText,
                suffixText = tailText
            )
        }

        MySetEditDialog(
            expanded = editMysetDialogShown,
            title = title,
            headText = headText,
            tailText = tailText,
            onConfirm = { newTitle, newHead, newTail ->
                fileEditor.editMysetInfo(
                    fileName = file.name,
                    title = newTitle,
                    headText = newHead,
                    tailText = newTail
                )
                title = newTitle
                headText = newHead
                tailText = newTail
                editMysetDialogShown = false
            },
            onCancel = { editMysetDialogShown = false },
        )

        MysetRemoveDialog(
            expanded = removeMysetDialogShown,
            onConfirm = {
                // コールバックで自身を削除
                removeSelf()
                removeMysetDialogShown = false
            },
            onCancel = { removeMysetDialogShown = false }
        )

        GameList(file = file)
    }
}

@Composable
private fun GameListHeader(
    title: String,
    prefixText: String,
    suffixText: String,
) {
    Column(modifier = Modifier.fillMaxWidth()) {

        Text(
            text = title,
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

        FadeInAnimated(visible = prefixText.isNotEmpty()) {

            Text(
                text = prefixText,
                fontSize = 12.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .align(Alignment.Start)
                    .padding(
                        start = ComponentValue.DISPLAY_PADDING_START,
                        end = ComponentValue.DISPLAY_PADDING_END
                    )

            )
        }

        FadeInAnimated(visible = suffixText.isNotEmpty()) {

            Text(
                text = suffixText,
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
    }
}

object GameSerchMode {
    const val ID = "Id"
    const val TITLE = "Title"
    const val EITHER = "Either"
}

@Composable
private fun GameList(file: File, ) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val infoManager = rememberInfoManager()
    val fileEditor = rememberFileEditor()

    val games = remember { infoManager.getGameInfo(file).toMutableStateList() }
    var inportEffectKey by remember { mutableIntStateOf(0) }

    LaunchedEffect(inportEffectKey) {
        games.clear()
        games.addAll(infoManager.getGameInfo(file))
    }
    val listState = rememberLazyListState()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {

            var searchText by remember { mutableStateOf("") }
            var selectedSearchMode by remember { mutableStateOf(GameSerchMode.TITLE) }
            var searchManuExpand by remember { mutableStateOf(false) }

            val searchModes = remember {
                mutableStateListOf(
                    GameSerchMode.ID,
                    GameSerchMode.TITLE,
                    GameSerchMode.EITHER
                )
            }

            OutlinedTextFieldManu(
                text = searchText,
                onTextChange = {
                    searchText = it
                    scope.launch(Dispatchers.Default) {
                        val searchList = infoManager.getGameInfo(file).filter { game ->
                            if (it.isNotEmpty()) {
                                when (selectedSearchMode) {
                                    GameSerchMode.ID -> game.id.contains(it)
                                    GameSerchMode.TITLE -> game.title.contains(it)
                                    GameSerchMode.EITHER -> {
                                        game.id.contains(it) || game.title.contains(it)
                                    }
                                    else -> true
                                }
                            } else {
                                true
                            }
                        }
                        games.clear()
                        games.addAll(searchList)
                    }
                },
                hintText = when (selectedSearchMode) {
                    GameSerchMode.ID -> context.getString(R.string.myset_search_hint_id)
                    GameSerchMode.TITLE -> context.getString(R.string.myset_search_hint_title)
                    GameSerchMode.EITHER -> context.getString(R.string.myset_search_hint_either)
                    else -> ERROR
                },
                onClick = { searchManuExpand = true },
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
            ) {
                BusyManu(
                    expanded = searchManuExpand,
                    onDismissRequest = { searchManuExpand = false }
                ) {
                    texts(
                        options = searchModes.associateWith { mode ->
                            when (mode) {
                                GameSerchMode.ID -> context.getString(R.string.myset_search_manu_id)
                                GameSerchMode.TITLE -> context.getString(R.string.myset_search_manu_title)
                                GameSerchMode.EITHER -> context.getString(R.string.myset_search_manu_either)
                                else -> ERROR
                            }
                        },
                        onClick = {
                            selectedSearchMode = it
                            searchManuExpand = false
                        }
                    )
                }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize()
            ) {
                animatedItems(
                    items = games,
                    key = { it.id }
                ) { game ->
                    GameListItem(
                        gameInfo = game,
                        fileName = file.name
                    )
                }
            }
        }

        val FAB_PADDING = 30.dp

        var gameAddDialogShown by rememberSaveable { mutableStateOf(false) }
        var gameImportDialogShown by rememberSaveable { mutableStateOf(false) }

        CombinedFloatingActionButton(
            containerColor = MaterialTheme.colorScheme.secondary,
            onClick = { gameAddDialogShown = true },
            onLongClick = { gameImportDialogShown = true },
            modifier = Modifier
                .wrapContentSize()
                .padding(
                    end = FAB_PADDING,
                    bottom = FAB_PADDING
                )
                .align(Alignment.BottomEnd)

        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add",
                modifier = Modifier
                    .wrapContentSize()
                    .padding(all = 8.dp)

            )
        }

        var gameAddDialogError by rememberSaveable { mutableStateOf(false) }

        GameAddDialog(
            expanded = gameAddDialogShown,
            onConfirm = { id, title, text ->
                val result = fileEditor.addGameInfo(
                    fileName = file.name,
                    id = id,
                    title = title,
                    text = text
                )
                if (result.isSuccess) {
                    val newGame = result.getOrNull()!!
                    games.add(newGame)
                    scope.launch { listState.animateScrollToItem(games.size) }
                    gameAddDialogShown = false
                } else {
                    gameAddDialogError = true
                }
            },
            onCancel = { gameAddDialogShown = false },
            isError = gameAddDialogError
        )

        GameImportDialog(
            expanded = gameImportDialogShown,
            onConfirm = { overwrite, jsonObject ->
                val result = fileEditor.importGameInfo(file.name, jsonObject, overwrite)
                if (result.isSuccess) {
                    if (overwrite) {
                        inportEffectKey++
                    } else {
                        val newGames = result.getOrNull()!!
                        games.addAll(newGames)
                        scope.launch { listState.animateScrollToItem(games.size) }
                    }
                    gameImportDialogShown = false
                } else {
                    Toast.makeText(
                        context,
                        context.getString(R.string.failed_import),
                        Toast.LENGTH_LONG
                    ).show()
                }

            },
            onCancel = { gameImportDialogShown = false }
        )
    }
}

@Composable
private fun GameListItem(
    gameInfo: GameInfo,
    fileName: String,
) {
    var isRemoved by remember { mutableStateOf(false) }

    DrawUpAnimated(visible = !isRemoved) {

        val fileEditor = rememberFileEditor()

        var gameEditDialogShown by rememberSaveable { mutableStateOf(false) }
        var gameRemoveDialogShown by rememberSaveable { mutableStateOf(false) }

        val id = gameInfo.id
        var title by remember { mutableStateOf(gameInfo.title) }
        var text by remember { mutableStateOf(gameInfo.text) }

        ColumnButton(
            onClick = { gameEditDialogShown = true },
            onLongClick = { gameRemoveDialogShown = true },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 60.dp)
        ) {
            Text(
                text = title,
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

            FadeInAnimated(visible = text.isNotEmpty()) {
                Text(
                    text = text,
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

        GameEditDialog(
            expanded = gameEditDialogShown,
            id = id,
            title = title,
            text = text,
            onConfirm = { newTitle, newText ->
                fileEditor.editGameInfo(
                    fileName = fileName,
                    id = id,
                    title = newTitle,
                    text = text
                )
                title = newTitle
                text = newText
                gameEditDialogShown = false
            },
            onCancel = { gameEditDialogShown = false }
        )

        GameRemoveDialog(
            expanded = gameRemoveDialogShown,
            id = id,
            onConfirm = {
                fileEditor.removeGameInfo(fileName, id)
                isRemoved = true
                gameRemoveDialogShown = false
            },
            onCancel = { gameRemoveDialogShown = false }
        )
    }
}