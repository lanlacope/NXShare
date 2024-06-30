package io.github.lanlacope.nxsharinghelper.classes

object SWITCH_LOCALHOST {
    val INDEX: String = "http://192.168.0.1/index.html"
    val DATA: String = "http://192.168.0.1/data.json"
    val IMAGE: String = "http://192.168.0.1/img/"
}

val FOLDER_THIS: String = "NXShare"

object MINETYPE {
    val JPG: String = "image/jpg"
    val MP4: String = "video/mp4"
}

object DOWNLOAD_JSON_PROPATY {
    val FILETYPE: String = "FileType"
    val FILENAMES: String = "FileNames"
    val CONSOLENAME: String = "ConsoleName"
    val FILETYPE_PHOTO: String = "photo"
    val FILETYPE_MOVIE: String = "movie"
}

data class DownloadData(
    val fileType: String = "",
    val consoleName: String = "",
    val fileNames: List<String> = listOf()
)

val FOLDER_SHARE = "share_setting"
val FILE_OTHERAPPS = "apps"

object SHARE_JSON_PROPATY {
    val PACKAGE_NAME: String = "package_name"
    val PACKAGE_TYPE: String = "package_type"
    val COMMON_TEXT: String = "common_text"
    val GAME_DATA: String = "game_data"
    val GAME_TITLE: String = "game_title"
    val GAME_HASH: String = "game_hash"
    val GAME_TEXT: String = "game_text"
}