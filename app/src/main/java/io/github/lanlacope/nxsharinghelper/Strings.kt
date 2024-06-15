package io.github.lanlacope.nxsharinghelper

object SWITCH_LOCAL_HOST {
    val INDEX: String = "http://192.168.0.1/index.html"
    val DATA: String = "http://192.168.0.1/data.json"
    val IMAGE:String = "http://192.168.0.1/img/"
}

data class SwitchConfig(
    var ssid: String = "",
    var password: String = "",
    val encryptionType: String = "WPA" // not used
)

data class DownloadData(
    var fileType: String = "",
    var consoleName: String = "",
    val fileNames: MutableList<String> = mutableListOf()
)