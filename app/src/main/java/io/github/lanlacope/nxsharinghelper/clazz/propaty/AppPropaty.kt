package io.github.lanlacope.nxsharinghelper.clazz.propaty

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf


object AppPropaty {

    object MineType {
        val JPG: String = "image/jpg"
        val MP4: String = "video/mp4"
        val JSON: String = "application/json"
    }
    object SwitchLocalHost {
        val INDEX: String = "http://192.168.0.1/index.html"
        val DATA: String = "http://192.168.0.1/data.json"
        val IMAGE: String = "http://192.168.0.1/img/"
    }

    object SwitchJsonPropaty {
        val FILETYPE: String = "FileType"
        val FILENAMES: String = "FileNames"
        val CONSOLENAME: String = "ConsoleName"
        val FILETYPE_PHOTO: String = "photo"
        val FILETYPE_MOVIE: String = "movie"
    }

    object SettingJsonPropaty {
        val APP_THEME: String = "ThemeType"
        val THEME_SYSTEM: String = "SystemTheme"
        val THEME_LIGHT: String = "LightTheme"
        val THEME_DARK: String = "DarkTheme"
        val APP_THEME_LIST: PersistentList<String> = persistentListOf(
            THEME_SYSTEM,
            THEME_LIGHT,
            THEME_DARK
        )
        val ALTERNATIVE_CONNECTION_ENABlED: String = "AlternativeConnectionEnabled"
    }

    object AppJsonPropaty {
        val PACKAGE_NAME: String = "PackageName"
        val PACKAGE_TYPE: String = "PackageType"
        val TYPE_NONE: String = "NONE"
        val PAKCAGE_ENABLED: String = "PackageEnabled"
    }

    object MySetJsonPropaty {
        val MYSET_TITLE: String = "MySetTitle"
        val HEAD_TEXT: String = "HeadText"
        val TAIL_TEXT: String = "TailText"
        val GAME_DATA: String = "GameData"
        val GAME_TITLE: String = "GameTitle"
        val GAME_ID: String = "GameID"
        val GAME_TEXT: String = "GameText"
    }
}