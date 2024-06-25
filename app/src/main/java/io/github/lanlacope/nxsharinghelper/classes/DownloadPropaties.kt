package io.github.lanlacope.nxsharinghelper.classes

object SWITCH_LOCALHOST {
    val INDEX: String = "http://192.168.0.1/index.html"
    val DATA: String = "http://192.168.0.1/data.json"
    val IMAGE: String = "http://192.168.0.1/img/"
}

val APP_FOLDER: String = "NXShare"

object MINETYPE {
    val JPG: String = "image/jpg"
    val MP4: String = "video/mp4"
}

object JSON_PROPATY {
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