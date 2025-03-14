package io.github.lanlacope.nxsharinghelper.activity.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.core.graphics.drawable.toBitmap
import io.github.lanlacope.rewheel.composeable.ui.click.BoxButton
import io.github.lanlacope.rewheel.ui.action.setting.SettingSwitch
import io.github.lanlacope.rewheel.ui.animation.DrawUpAnimated
import io.github.lanlacope.rewheel.ui.busy.option.BusyOption
import io.github.lanlacope.rewheel.ui.busy.option.radioButtons
import io.github.lanlacope.rewheel.ui.button.RowButton
import io.github.lanlacope.rewheel.ui.lazy.animatedItems
import io.github.lanlacope.rewheel.ui.text.search.SearchTextField
import io.github.lanlacope.nxsharinghelper.R
import io.github.lanlacope.nxsharinghelper.clazz.InfoManager.AppInfo
import io.github.lanlacope.nxsharinghelper.clazz.InfoManager.ShareInfo
import io.github.lanlacope.nxsharinghelper.clazz.propaty.AppJsonPropaty
import io.github.lanlacope.nxsharinghelper.clazz.rememberFileEditor
import io.github.lanlacope.nxsharinghelper.clazz.rememberInfoManager
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch

/*
 * 共有可能なアプリの一覧を表示
 * それぞれの個別設定を提供する
 */

@Composable
fun SettingPackage() {

    val infoManager = rememberInfoManager()
    var apps by remember { mutableStateOf<ImmutableList<AppInfo>?>(null) }

    LaunchedEffect(Unit) {
        apps = infoManager.getAppInfo().toImmutableList()
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {

        AnimatedContent(targetState = (apps != null), label = "package") { loaded ->
            if (loaded) {
                Column(modifier = Modifier.fillMaxSize()) {

                    val searchedApps = remember { apps!!.toMutableStateList() }
                    var searchText by remember { mutableStateOf("") }

                    SearchTextField(
                        text = searchText,
                        onTextChange = {
                            searchText = it
                            val searchList = apps!!.toMutableList().filter { app ->
                                if (it.isNotEmpty()) {
                                    app.name.contains(it)
                                } else {
                                    true
                                }
                            }
                            searchedApps.clear()
                            searchedApps.addAll(searchList)
                        },
                        hintText = stringResource(id = R.string.package_search_hint),
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth()
                    )


                    LazyColumn(
                        state = rememberLazyListState(),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        animatedItems(
                            items = searchedApps,
                            key = { it.packageName }
                        ) { app ->

                            var isExpanded by remember { mutableStateOf(false) }
                            Column {
                                BoxButton(
                                    onClick = { isExpanded = !isExpanded },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .wrapContentHeight()

                                ) {
                                    PackageCard(app = app)
                                }
                                DrawUpAnimated(visible = isExpanded) {
                                    PackageSetting(
                                        packageName = app.packageName
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                Box(modifier = Modifier.size(64.dp)) {
                    CircularProgressIndicator(modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
}

@Composable
private fun PackageCard(app: AppInfo) {

    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(10.dp)
            .background(Color.Transparent)
    ) {
        val (iconRef, labelRef, nameRef) = createRefs()

        val ICON_SIZE = 50.dp

        Image(
            bitmap = app.icon.toBitmap().asImageBitmap(),
            contentDescription = app.name,
            modifier = Modifier
                .constrainAs(iconRef) {
                    start.linkTo(parent.start)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    width = Dimension.value(ICON_SIZE)
                    height = Dimension.value(ICON_SIZE)
                }
                .padding(
                    start = ComponentValue.DISPLAY_PADDING_START,
                    end = 16.dp
                )

        )

        Text(
            text = app.name,
            fontSize = 24.sp,
            minLines = 1,
            maxLines = 1,
            modifier = Modifier
                .constrainAs(labelRef) {
                    start.linkTo(iconRef.end)
                    top.linkTo(parent.top)
                    end.linkTo(parent.end)
                    width = Dimension.fillToConstraints
                    height = Dimension.wrapContent
                }

        )

        Text(
            text = app.packageName,
            fontSize = 12.sp,
            minLines = 1,
            maxLines = 1,
            modifier = Modifier
                .constrainAs(nameRef) {
                    start.linkTo(iconRef.end)
                    end.linkTo(parent.end)
                    bottom.linkTo(parent.bottom)
                    width = Dimension.fillToConstraints
                    height = Dimension.wrapContent
                }

        )
    }
}

@Composable
private fun PackageSetting(packageName: String) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {

        val TEXT_VERTICAL_PADDING = 10.dp

        val fileEditor = rememberFileEditor()

        val shareInfo = ShareInfo(packageName, context)
        var cheacked by remember { mutableStateOf(shareInfo.shareEnabled) }

        SettingSwitch(
            text = stringResource(id = R.string.setting_app_option_enabled),
            checked = cheacked,
            onClick = {
                scope.launch {
                    cheacked = !cheacked
                    fileEditor.changeShareEnabled(packageName, cheacked)
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        var isExpanded by remember { mutableStateOf(false) }

        RowButton(
            onClick = { isExpanded = !isExpanded },
            horizontalArrangement = Arrangement.SpaceBetween,
            innerPadding = PaddingValues(all = 10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)

        ) {
            Text(
                text = stringResource(id = R.string.setting_app_option_app_type),
                maxLines = 1,
                minLines = 1,
                modifier = Modifier
                    .wrapContentSize()
                    .align(Alignment.CenterVertically)
                    .padding(
                        start = ComponentValue.DISPLAY_PADDING_START,
                        top = TEXT_VERTICAL_PADDING,
                        bottom = TEXT_VERTICAL_PADDING
                    )
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = Icons.Default.KeyboardArrowDown.name,
                modifier = Modifier
                    .wrapContentSize()
                    .align(Alignment.CenterVertically)
                    .padding(end = ComponentValue.DISPLAY_PADDING_END)

            )
        }

        DrawUpAnimated(visible = isExpanded) {

            var selectedType by remember { mutableStateOf(shareInfo.type) }
            val types = remember {
                shareInfo.types.associateWith { type ->
                    println(type)
                    if (type == AppJsonPropaty.MYSET_NONE) context.getString(R.string.setting_app_option_value_nothing)
                    else type
                }
            }

            BusyOption(
                userScrollEnabled = false,
                contentPadding = PaddingValues(start = ComponentValue.DISPLAY_PADDING_START),
                modifier = Modifier.fillMaxWidth()
            ) {
                radioButtons(
                    options = types,
                    selected = { selectedType == it },
                    onClick = {
                        scope.launch {
                            selectedType = it
                            fileEditor.changeShareType(packageName, it)
                        }
                    },
                )
            }
        }
    }
}