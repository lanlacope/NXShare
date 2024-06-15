package io.github.lanlacope.nxsharinghelper

val SWITCH_LOCAL_HOST: String = "http://192.168.0.1/index.html"

data class SwitchConfig(
    var ssid: String = "",
    var password: String = "",
    val encryptionType: String = "WPA" // not used
)