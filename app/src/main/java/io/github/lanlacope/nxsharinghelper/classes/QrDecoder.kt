package io.github.lanlacope.nxsharinghelper.classes

import io.github.lanlacope.nxsharinghelper.SWITCH_LOCAL_HOST

class QrDecoder {

    data class SwitchConfig(
        val ssid: String = "",
        val password: String = "",
        val encryptionType: String = "WPA" // not used
    )

    object DecordingStates {
        const val SUCCESSFUL_CARER: Int = 1
        const val SUCCESSFUL_FAIR: Int = 2
        const val SUCCESSFUL_POOR: Int = 3
        const val SUCCESSFUL_BAD: Int = 4
        const val FAILED_UNKNOWN: Int = -1
        const val FAILED_NOTWIFI: Int = -2
        const val FAILED_LOCALHOST: Int = -3
    }

    var decordingResult = SwitchConfig()
        private set

    var decordingState = DecordingStates.FAILED_UNKNOWN
        private set

    private var creditScore: Int = 0

    fun isSuccesed(): Boolean {
        return decordingState >= DecordingStates.SUCCESSFUL_CARER
    }

    /*
     * SwitchのQRは以下の形式
     * WIFI:S:switch_型番号;T:WPA;P:八桁のワンタイムパスワード;;
     */

    fun startDecode(contents: String) {

        // 初期化
        creditScore = 0
        decordingState = DecordingStates.SUCCESSFUL_CARER
        decordingResult = SwitchConfig()

        try {
            parseQr(contents)

            println(
                "SSID : ${decordingResult.ssid}\n" +
                        "PASS : ${decordingResult.password}"
            )

            if (isSuccesed()) {
                decordingState =
                    when (creditScore) {
                        0 -> DecordingStates.SUCCESSFUL_CARER
                        in 1..15 -> DecordingStates.SUCCESSFUL_FAIR
                        in 16..20 -> DecordingStates.SUCCESSFUL_POOR
                        else -> DecordingStates.SUCCESSFUL_BAD
                    }

            }
        } catch (e: Exception) {
            decordingState = DecordingStates.FAILED_UNKNOWN
        }
    }

    private fun parseQr(contents: String) {

        var ssid = ""
        var password = ""

        if (!contents.startsWith("WIFI:")) {
            if (contents == SWITCH_LOCAL_HOST.INDEX) {
                decordingState = DecordingStates.FAILED_LOCALHOST
                return
            }
            decordingState = DecordingStates.FAILED_NOTWIFI
            return
        }

        val parts = contents.substring(5).split(";").toTypedArray()

        for (part in parts) {
            when {
                part.startsWith("S:") -> {
                    ssid = part.substring(2)

                    if (!decordingResult.ssid.startsWith("switch_")) {
                        creditScore += 25
                    }
                }

                part.startsWith("P:") -> {
                    password = part.substring(2)

                    if (decordingResult.password.length != 8) {
                        creditScore += 5
                    }
                }

                part.startsWith("T") -> {
                    if (part.substring(2) != decordingResult.encryptionType) {
                        creditScore += 10
                    }
                }

                part.isEmpty() -> {
                    // do nothing
                }

                else -> {
                    creditScore += 5
                }
            }
        }

        decordingResult = SwitchConfig(
            ssid = ssid,
            password = password
        )
    }
}