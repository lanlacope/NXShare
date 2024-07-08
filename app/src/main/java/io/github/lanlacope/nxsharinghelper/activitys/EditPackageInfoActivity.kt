package io.github.lanlacope.nxsharinghelper.activitys

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.core.graphics.drawable.toBitmap
import io.github.lanlacope.nxsharinghelper.R
import io.github.lanlacope.nxsharinghelper.classes.AppInfo
import io.github.lanlacope.nxsharinghelper.classes.SHARE_JSON_PROPATY
import io.github.lanlacope.nxsharinghelper.classes.FileManager
import io.github.lanlacope.nxsharinghelper.classes.ShareInfo
import io.github.lanlacope.nxsharinghelper.createDummyResolveInfo
import io.github.lanlacope.nxsharinghelper.isAfterAndroidVII
import io.github.lanlacope.nxsharinghelper.ui.theme.Clear
import io.github.lanlacope.nxsharinghelper.ui.theme.NXSharingHelperTheme
import org.json.JSONArray
import org.json.JSONObject


class EditPackageInfoActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            NXSharingHelperTheme {
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

    val fileManager = FileManager(LocalContext.current)
    val apps = fileManager.getAppInfo()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
    ) {
        items(apps) { app ->
            var isExpanded by remember {
                mutableStateOf(false)
            }
            Box(
                modifier = Modifier
                    .wrapContentSize()
                    .clickable {
                        isExpanded = !isExpanded
                    }
            ) {
                PackageCard(
                    app = app
                )
            }
            if (isExpanded) {
                PackageSetting(
                    app = app
                )
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
            contentDescription = app.appName,
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
            text = app.appName,
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
    app: AppInfo
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        val TEXT_PADDING = 10.dp

        val fileManager = FileManager(context)
        val shareInfo = ShareInfo(app, context)

        var cheacked by remember {
            mutableStateOf(shareInfo.shareEnabled)
        }

        if (isAfterAndroidVII()) {
            val onSwitchChange = {
                cheacked = !cheacked
                fileManager.changeShareEnabled(app, cheacked)
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .clickable {
                        onSwitchChange()
                    }

            ) {
                Text(
                    text = "アプリを使う",
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
        }

        var isExpanded by remember {
            mutableStateOf(false)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .clickable {
                    isExpanded = !isExpanded
                }

        ) {
            Text(
                text = "コピーを使う",
                maxLines = 1,
                minLines = 1,
                modifier = Modifier
                    .wrapContentSize()
                    .align(Alignment.CenterStart)
                    .padding(all = TEXT_PADDING)
            )
        }

        if (isExpanded) {
            val selectedType = remember {
                mutableStateOf(shareInfo.type)
            }
            val typeNames = shareInfo.types

            typeNames.forEach() { type ->
                TypeSelector(
                    app = app,
                    type = type,
                    selectedType = selectedType,
                )
            }
        }
    }
}

@Composable
private fun TypeSelector(
    app: AppInfo,
    type: String,
    selectedType: MutableState<String>
) {
    val fileManager = FileManager(LocalContext.current)
    val onClick = {
        selectedType.value = type
        fileManager.changeShareType(app, type)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clickable(
                onClick = {
                    onClick()
                }
            )

    ) {
        RadioButton(
            selected = type == selectedType.value,
            onClick = {
                onClick()
            },
            modifier = Modifier
                .wrapContentSize()
                .align(Alignment.CenterVertically)
                .padding(start = 10.dp)

        )

        Text(
            text = type,
            maxLines = 1,
            minLines = 1,
            modifier = Modifier
                .wrapContentSize()
                .align(Alignment.CenterVertically)
        )
    }
}

