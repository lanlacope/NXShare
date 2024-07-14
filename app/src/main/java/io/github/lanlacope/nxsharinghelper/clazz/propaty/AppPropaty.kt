package io.github.lanlacope.nxsharinghelper.clazz.propaty

object AppPropaty {

    object MINETYPE {
        val JPG: String = "image/jpg"
        val MP4: String = "video/mp4"
    }
    object SWITCH_LOCALHOST {
        val INDEX: String = "http://192.168.0.1/index.html"
        val DATA: String = "http://192.168.0.1/data.json"
        val IMAGE: String = "http://192.168.0.1/img/"
    }

    object SWITCH_JSON_PROPATY {
        val FILETYPE: String = "FileType"
        val FILENAMES: String = "FileNames"
        val CONSOLENAME: String = "ConsoleName"
        val FILETYPE_PHOTO: String = "photo"
        val FILETYPE_MOVIE: String = "movie"
    }

    object SETTING_JSON_PROPATY {
        val SUBSTITUTE_CONNECTION_ENABlED: String = "SubstituteConnectionEnabled"
    }

    object APP_JSON_PROPATY {
        val PACKAGE_NAME: String = "PackageName"
        val PACKAGE_TYPE: String = "PackageType"
        val TYPE_NONE: String = "NotUse"
        val PAKCAGE_ENABLED: String = "PackageEnabled"
    }

    object GAME_JSON_PROPATY {
        val COMMON_TITLE: String = "CommonTitle"
        val COMMON_TEXT: String = "CommonText"
        val GAME_DATA: String = "GameData"
        val GAME_TITLE: String = "GameTitle"
        val GAME_ID: String = "GameID"
        val GAME_TEXT: String = "GameText"
    }
}