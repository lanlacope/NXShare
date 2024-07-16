package io.github.lanlacope.nxsharinghelper.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.core.graphics.drawable.toBitmap
import io.github.lanlacope.nxsharinghelper.R
import io.github.lanlacope.nxsharinghelper.activity.component.DrawDownAnimated
import io.github.lanlacope.nxsharinghelper.activity.component.animatedItems
import io.github.lanlacope.nxsharinghelper.clazz.InfoManager.AppInfo
import io.github.lanlacope.nxsharinghelper.clazz.InfoManager.ShareInfo
import io.github.lanlacope.nxsharinghelper.ui.theme.Clear
import io.github.lanlacope.nxsharinghelper.ui.theme.AppTheme
import io.github.lanlacope.nxsharinghelper.widgit.Box
import io.github.lanlacope.nxsharinghelper.widgit.Row
import io.github.lanlacope.nxsharinghelper.clazz.propaty.AppPropaty.APP_JSON_PROPATY
import io.github.lanlacope.nxsharinghelper.clazz.rememberFileEditor
import io.github.lanlacope.nxsharinghelper.clazz.rememberInfoManager
import kotlinx.collections.immutable.toImmutableList


class EditPackageInfoActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PackageList()
                }
            }
        }
    }
}

@Composable
private fun PackageList() {

    val infoManager = rememberInfoManager()
    val apps = infoManager.getAppInfo().toImmutableList()

    LazyColumn(
        state = rememberLazyListState(),
        modifier = Modifier
            .fillMaxSize()
    ) {
        animatedItems(items = apps) { app ->
            var isExpanded by remember {
                mutableStateOf(false)
            }
            Column {
                Box(
                    onClick = {
                        isExpanded = !isExpanded
                    },
                    modifier = Modifier
                        .wrapContentSize()


                ) {
                    PackageCard(
                        app = app
                    )
                }
                DrawDownAnimated(visible = isExpanded) {
                    PackageSetting(
                        packageName = app.packageName
                    )
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
            .background(Clear)
    ) {
        val (
            icon,
            label,
            name
        ) = createRefs()

        val ICON_SIZE = 50.dp

        Image(
            bitmap = app.icon.toBitmap().asImageBitmap(),
            contentDescription = app.name,
            modifier = Modifier
                .constrainAs(icon) {
                    start.linkTo(parent.start)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    width = Dimension.value(ICON_SIZE)
                    height = Dimension.value(ICON_SIZE)
                }
                .padding(end = 10.dp)

        )

        Text(
            text = app.name,
            fontSize = 24.sp,
            minLines = 1,
            maxLines = 1,
            modifier = Modifier
                .constrainAs(label) {
                    start.linkTo(icon.end)
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
                .constrainAs(name) {
                    start.linkTo(icon.end)
                    end.linkTo(parent.end)
                    bottom.linkTo(parent.bottom)
                    width = Dimension.fillToConstraints
                    height = Dimension.wrapContent
                }

        )
    }
}

@Composable
private fun PackageSetting(
    packageName: String
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        val TEXT_PADDING = 10.dp

        val fileEditor = rememberFileEditor()
        val shareInfo = ShareInfo(packageName, context)

        var cheacked by remember {
            mutableStateOf(shareInfo.shareEnabled)
        }

        val onSwitchChange = {
                cheacked = !cheacked
                fileEditor.changeShareEnabled(packageName, cheacked)
            }

        Box(
            onClick = onSwitchChange,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()

            ) {
            Text(
                text = stringResource(id = R.string.summary_app_enabled),
                maxLines = 1,
                minLines = 1,
                modifier = Modifier
                    .wrapContentSize()
                    .align(Alignment.CenterStart)
                    .padding(all = TEXT_PADDING)

            )

            Switch(
                checked = cheacked,
                onCheckedChange = {
                    onSwitchChange()
                },
                modifier = Modifier
                    .wrapContentSize()
                    .align(Alignment.CenterEnd)
                    .padding(end = 50.dp)

            )
        }

        var isExpanded by remember {
            mutableStateOf(false)
        }
        Box(
            onClick = {
                isExpanded = !isExpanded
            },
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()


        ) {
            Text(
                text = stringResource(id = R.string.summary_app_type),
                maxLines = 1,
                minLines = 1,
                modifier = Modifier
                    .wrapContentSize()
                    .align(Alignment.CenterStart)
                    .padding(all = TEXT_PADDING)
            )
        }

        DrawDownAnimated(visible = isExpanded) {
            val selectedType = remember {
                mutableStateOf(shareInfo.type)
            }
            val typeNames = shareInfo.types

            typeNames.forEach { type ->
                TypeSelector(
                    packageName = packageName,
                    type = type,
                    selectedType = selectedType,
                )
            }
        }
    }
}

@Composable
private fun TypeSelector(
    packageName: String,
    type: String,
    selectedType: MutableState<String>
) {
    val fileEditor = rememberFileEditor()
    val onClick = {
        selectedType.value = type
        fileEditor.changeShareType(packageName, type)
    }

    Row(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()

    ) {
        RadioButton(
            selected = type == selectedType.value,
            onClick = onClick,
            modifier = Modifier
                .wrapContentSize()
                .align(Alignment.CenterVertically)
                .padding(start = 10.dp)

        )

        Text(
            text = if (type != APP_JSON_PROPATY.TYPE_NONE) type else stringResource(id = R.string.summart_type_nothing),
            maxLines = 1,
            minLines = 1,
            modifier = Modifier
                .wrapContentSize()
                .align(Alignment.CenterVertically)

        )
    }
}

