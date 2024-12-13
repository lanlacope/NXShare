package io.github.lanlacope.nxsharinghelper.clazz.propaty

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf

const val ERROR = "ERROR"


object AppPropaty {

    object MineType {
        const val JPG: String = "image/jpg"
        const val MP4: String = "video/mp4"
        const val JSON: String = "application/json"
    }

    object SwitchLocalHost {
        const val INDEX: String = "http://192.168.0.1/index.html"
        const val DATA: String = "http://192.168.0.1/data.json"
        const val IMAGE: String = "http://192.168.0.1/img/"
    }

    object AppGitHost {
        const val SOURCE: String = "https://github.com/lanlacope/NXShare"
        const val LICENSE: String = "https://github.com/lanlacope/NXShare/blob/master/README.MD#license"
        const val LATEST: String = "https://github.com/lanlacope/NXShare/releases/latest"
        const val TAG: String = "tag_name"
    }

    object SwitchJsonPropaty {
        const val FILETYPE: String = "FileType"
        const val FILENAMES: String = "FileNames"
        const val CONSOLENAME: String = "ConsoleName"
        const val FILETYPE_PHOTO: String = "photo"
        const val FILETYPE_MOVIE: String = "movie"
    }

    object SettingJsonPropaty {
        const val APP_THEME: String = "ThemeType"
        const val THEME_SYSTEM: String = "SystemTheme"
        const val THEME_LIGHT: String = "LightTheme"
        const val THEME_DARK: String = "DarkTheme"
        val APP_THEME_LIST: PersistentList<String> = persistentListOf(
            THEME_SYSTEM,
            THEME_LIGHT,
            THEME_DARK
        )
        const val FOR_LEGACY: String = "ForLegacy"
    }

    object AppJsonPropaty {
        const val PACKAGE_TYPE: String = "PackageType"
        const val MYSET_NONE: String = "NONE"
        const val PAKCAGE_ENABLED: String = "PackageEnabled"
    }

    object MySetJsonPropaty {
        const val MYSET_TITLE: String = "Title"
        const val PREFIX_TEXT: String = "PrefixText"
        const val SUFFIX_TEXT: String = "SuffixText"
        const val GAME_DATA: String = "GameData"
        const val GAME_TITLE: String = "GameTitle"
        const val GAME_TEXT: String = "GameText"
    }
}