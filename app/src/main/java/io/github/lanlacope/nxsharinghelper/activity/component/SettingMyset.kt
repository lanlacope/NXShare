package io.github.lanlacope.nxsharinghelper.activity.component

import android.widget.Toast
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.lanlacope.compose.composeable.ui.click.BoxButton
import io.github.lanlacope.compose.ui.animation.DrawUpAnimated
import io.github.lanlacope.compose.ui.button.ColumnButton
import io.github.lanlacope.compose.ui.button.CombinedButton
import io.github.lanlacope.compose.ui.button.CombinedFloatingActionButton
import io.github.lanlacope.compose.ui.lazy.animatedItems
import io.github.lanlacope.compose.ui.lazy.pager.LazyHorizontalPager
import io.github.lanlacope.compose.ui.lazy.pager.animatedPages
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
import io.github.lanlacope.nxsharinghelper.clazz.rememberFileEditor
import io.github.lanlacope.nxsharinghelper.clazz.rememberFileSelector
import io.github.lanlacope.nxsharinghelper.clazz.rememberInfoManager
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun SettingMyset() {

    Column(modifier = Modifier.fillMaxSize()) {

        val BUTTON_PADDING = 20.dp

        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        val fileEditor = rememberFileEditor()
        val fileSelector = rememberFileSelector()

        var mysetAddDialogShown by rememberSaveable { mutableStateOf(false) }
        var mysetImportDialogShown by rememberSaveable { mutableStateOf(false) }
        val listState = rememberLazyListState()

        val files = remember { fileSelector.getMySetFiles().toMutableStateList() }

        CombinedButton(
            onClick = { mysetAddDialogShown = true },
            onLongClick = { mysetImportDialogShown = true },
            modifier = Modifier
                .width(200.dp)
                .wrapContentHeight()
                .align(Alignment.CenterHorizontally)
                .padding(vertical = BUTTON_PADDING)
        ) {
            Text(
                text = stringResource(id = R.string.dialog_positive_add),
                modifier = Modifier.wrapContentSize()
            )
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
        var headText by remember { mutableStateOf(mysetInfo.haedText) }
        var tailText by remember { mutableStateOf(mysetInfo.tailText) }

        BoxButton(
            onClick = { editMysetDialogShown = true },
            onLongClick = { removeMysetDialogShown = true }
        ) {
            GameListHeader(
                title = title,
                headText = headText,
                tailText = tailText
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
    headText: String,
    tailText: String,
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

        Text(
            text = headText,
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

        Text(
            text = tailText,
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

@Composable
private fun GameList(
    file: File,
) {
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
            modifier = Modifier.fillMaxWidth()
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