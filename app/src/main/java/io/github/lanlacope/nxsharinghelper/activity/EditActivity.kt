package io.github.lanlacope.nxsharinghelper.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.github.lanlacope.nxsharinghelper.activity.component.SettingMyset
import io.github.lanlacope.nxsharinghelper.activity.component.SettingPackage
import io.github.lanlacope.nxsharinghelper.activity.component.SettingRoot
import io.github.lanlacope.nxsharinghelper.ui.theme.AppTheme

/*
 * 設定を変更するアクティビティ
 */

class EditActivity: ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            AppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SettingView()
                }
            }
        }
    }
}

@Composable
fun SettingView() {

    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = "root"
    ) {
        composable("root") { SettingRoot(navController) }
        composable("package") { SettingPackage(navController) }
        composable("myset") { SettingMyset(navController) }
    }
}