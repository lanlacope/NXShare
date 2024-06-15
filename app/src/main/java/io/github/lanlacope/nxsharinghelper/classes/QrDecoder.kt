package io.github.lanlacope.nxsharinghelper.classes

import io.github.lanlacope.nxsharinghelper.SWITCH_LOCAL_HOST
import io.github.lanlacope.nxsharinghelper.SwitchConfig

class QrDecoder() {
    
    object DecordingStates {
        val SUCCESED_CREAR: Int = 1
        val SUCCESED_FAIR: Int = 2
        val SUCCESED_POOR: Int = 3
        val SUCCESED_BAD: Int = 4
        val FAILED_UNKNOWN: Int = -1
        val FAILED_NOTWIFI: Int = -2
        val FAILED_LOCALHOST: Int = -3
    }

    var decordingResult = SwitchConfig()
        private set

    var decordingState = DecordingStates.FAILED_UNKNOWN
        private set

    private var creditScore: Int = 0

    fun isSuccesed(): Boolean {
        return decordingState >= DecordingStates.SUCCESED_CREAR
    }

    /*
     * SwitchのQRは以下の形式
     * WIFI:S:switch_型番号;T:WPA;P:八桁のワンタイムパスワード;;
     */

    fun startDecode(contents: String) {

        try {

            creditScore = 0
            decordingState = DecordingStates.SUCCESED_CREAR

            decordingResult = parseQr(contents)

            println(
                "SSID : ${decordingResult.ssid}\n" +
                        "PASS : ${decordingResult.password}"
            )

            if (isSuccesed()) {
                decordingState =
                    when (creditScore) {
                        0 -> DecordingStates.SUCCESED_CREAR
                        in 1..15 -> DecordingStates.SUCCESED_FAIR
                        in 16..20 -> DecordingStates.SUCCESED_POOR
                        else -> DecordingStates.SUCCESED_BAD
                    }

            }
        } catch (e: Exception) {
            decordingState = DecordingStates.FAILED_UNKNOWN
        }
    }

    private fun parseQr(contents: String): SwitchConfig {

        val config = SwitchConfig()

        if (!contents.startsWith("WIFI:")) {
            if (contents == SWITCH_LOCAL_HOST) {
                decordingState = DecordingStates.FAILED_LOCALHOST
                return config
            }
            decordingState = DecordingStates.FAILED_NOTWIFI
            return config
        }

        val parts = contents.substring(5).split(";").toTypedArray()

        for (part in parts) {
            when {
                part.startsWith("S:") -> {
                    config.ssid = part.substring(2)

                    if (!config.ssid.startsWith("switch_")) {
                        creditScore += 25
                    }
                }

                part.startsWith("P:") -> {
                    config.password = part.substring(2)

                    if (config.password.length != 8) {
                        creditScore += 5
                    }
                }

                part.startsWith("T") -> {
                    if (part.substring(2) != config.encryptionType) {
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
        return config
    }
}