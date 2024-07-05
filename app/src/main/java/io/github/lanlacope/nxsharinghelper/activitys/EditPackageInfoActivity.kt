package io.github.lanlacope.nxsharinghelper.activitys

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
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
import androidx.lifecycle.ViewModel
import io.github.lanlacope.nxsharinghelper.R
import io.github.lanlacope.nxsharinghelper.classes.AppInfo
import io.github.lanlacope.nxsharinghelper.classes.SHARE_JSON_PROPATY
import io.github.lanlacope.nxsharinghelper.classes.FileEditer
import io.github.lanlacope.nxsharinghelper.classes.ShareInfo
import io.github.lanlacope.nxsharinghelper.createDummyResolveInfo
import io.github.lanlacope.nxsharinghelper.ui.theme.Clear
import io.github.lanlacope.nxsharinghelper.ui.theme.NXSharingHelperTheme
import org.json.JSONArray
import org.json.JSONObject


class EditPackageInfoActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val fileEditer = FileEditer(this)
        val apps = fileEditer.getAppInfo()

        setContent {
            NXSharingHelperTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PackageList(
                        apps = apps,
                        onSwitchChange = onSwitchChange,
                        onRadioButtonClick = onRadioButtonClick
                    )
                }

            }
        }
    }

    private val onSwitchChange: (AppInfo, Boolean) -> Unit = { app, isEnable ->
        val fileEditer = FileEditer(this)
        val file = fileEditer.getAppSettingFile()
        val jsonArray = JSONArray(file.readText())

        if (isEnable) {
            val jsonObject = JSONObject().apply {
                put(SHARE_JSON_PROPATY.PACKAGE_NAME, app.packageName)
                put(SHARE_JSON_PROPATY.PAKCAGE_ENABLED, isEnable)
            }
            jsonArray.put(jsonObject)
        } else {
            List(jsonArray.length()) { index ->
                val jsonObject = jsonArray.getJSONObject(index)
                if (jsonObject.getString(SHARE_JSON_PROPATY.PACKAGE_NAME) == app.packageName) {
                    jsonArray.remove(index)
                }
            }
        }
        file.writeText(jsonArray.toString())
    }

    private val onRadioButtonClick: (AppInfo, String) -> Unit = { app, name ->
        val fileEditer = FileEditer(this)
        val file = fileEditer.getAppSettingFile()
        val jsonArray = JSONArray(file.readText())
        List(jsonArray.length()) { index ->
            val jsonObject = jsonArray.getJSONObject(index)
            if (jsonObject.getString(SHARE_JSON_PROPATY.PACKAGE_NAME) == app.packageName) {
                jsonObject.put(SHARE_JSON_PROPATY.PACKAGE_TYPE, name)
            }
        }
        file.writeText(jsonArray.toString())
    }
}

@Composable
private fun PackageList(
    apps: List<AppInfo>,
    onSwitchChange: (AppInfo, Boolean) -> Unit,
    onRadioButtonClick: (AppInfo, String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
    ) {
        items(apps) { app ->
            var isExpanded by remember {
                mutableStateOf(true)
            }
            Box(
                modifier = Modifier
                    .wrapContentSize()
                    .clickable {
                        isExpanded = !isExpanded
                    }
            ) {
                PackageCard(
                    app = app)
            }
            if (isExpanded) {
                PackageSetting(
                    app = app,
                    onSwitchChange = onSwitchChange,
                    _onRadioButtonClick = onRadioButtonClick
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
    app: AppInfo,
    onSwitchChange: (AppInfo, Boolean) -> Unit,
    _onRadioButtonClick: (AppInfo, String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        val TEXT_PADDING = 10.dp

        val shareInfo = ShareInfo(app, LocalContext.current)

        var cheacked by remember {
            mutableStateOf(shareInfo.shareEnabled)
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .clickable {
                    cheacked = !cheacked
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
                    onSwitchChange(app, cheacked)
                },
                modifier = Modifier
                    .wrapContentSize()
                    .align(Alignment.CenterEnd)
                    .padding(end = 50.dp)

            )
        }

        var isExpanded by remember {
            mutableStateOf(true)
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

        val selectedType by remember {
            mutableStateOf(shareInfo.type)
        }
        val typeNames = shareInfo.types

         typeNames.forEach() { type ->

             val onRadioButtonClick = { _onRadioButtonClick(app, type) }

             TypeSelector(
                 type = type,
                 selectedType = selectedType,
                 onRadioButtonClick = onRadioButtonClick
             )
         }

    }
}

@Composable
private fun TypeSelector(
    type: String,
    selectedType: String,
    onRadioButtonClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clickable(
                onClick = onRadioButtonClick
            )

    ) {
        RadioButton(
            selected = type == selectedType,
            onClick = onRadioButtonClick,
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

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
private fun PackageListPreviewLight() {
    val context = LocalContext.current
    val packageManager = context.packageManager
    val resolveInfo = createDummyResolveInfo(
        packageName = "com.example.app",
        appName = "app",
        appIcon = AppCompatResources.getDrawable(
            context,
            R.drawable.ic_launcher_background
        )!!
    )
    val apps = mutableListOf<AppInfo>()
    val onSwitchChange: (AppInfo, Boolean) -> Unit =  { _, _ -> }
    val onRadioButtonClick: (AppInfo, String) -> Unit = { _, _ -> }

    apps.add(
        AppInfo(resolveInfo, packageManager)
    )
    apps.add(
        AppInfo(resolveInfo, packageManager)
    )
    NXSharingHelperTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            PackageList(
                apps = apps,
                onSwitchChange = onSwitchChange,
                onRadioButtonClick = onRadioButtonClick
            )
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PackageListPreviewDark() {
    val context = LocalContext.current
    val packageManager = context.packageManager
    val resolveInfo = createDummyResolveInfo(
        packageName = "com.example.app",
        appName = "app",
        appIcon = AppCompatResources.getDrawable(
            context,
            R.drawable.ic_launcher_background
        )!!
    )
    val apps = mutableListOf<AppInfo>()
    val onSwitchChange: (AppInfo, Boolean) -> Unit =  { _, _ -> }
    val onRadioButtonClick: (AppInfo, String) -> Unit = { _, _ -> }

    apps.add(
        AppInfo(resolveInfo, packageManager)
    )
    apps.add(
        AppInfo(resolveInfo, packageManager)
    )
    NXSharingHelperTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            PackageList(
                apps = apps,
                onSwitchChange = onSwitchChange,
                onRadioButtonClick = onRadioButtonClick
            )
        }
    }
}

