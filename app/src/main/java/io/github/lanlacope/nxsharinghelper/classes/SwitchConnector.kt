package io.github.lanlacope.nxsharinghelper.classes

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSpecifier
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi

class SwitchConnector(val context: Context) {

    object ConnectingStates {
        val SUCCESED: Int = 1
        val FAILED: Int = -1
    }

    var connectingState = ConnectingStates.FAILED
        private set

    fun isSuccesed(): Boolean {
        return connectingState >= ConnectingStates.SUCCESED
    }

    fun startConnect(config: QrDecoder.SwitchConfig) {

        // 初期化
        connectingState = ConnectingStates.SUCCESED

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                connectSwitch(config.ssid, config.password)
            } else {
                connectSwitchLegacy(config.ssid, config.password)
            }
        } catch (e: Exception) {
            connectingState = ConnectingStates.FAILED
        }
    }

    val connectivityManager by lazy {
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun connectSwitch(ssid: String, password: String) {

        val wifiNetworkSpecifier = WifiNetworkSpecifier.Builder()
            .setSsid(ssid)
            .setWpa2Passphrase(password)
            .build()

        val networkRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .setNetworkSpecifier(wifiNetworkSpecifier)
            .build()

        connectivityManager.requestNetwork(
            networkRequest,
            object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    connectivityManager.bindProcessToNetwork(network)
                    connectingState = ConnectingStates.SUCCESED
                    Toast.makeText(context, "Connecting to Wi-Fi", Toast.LENGTH_SHORT)
                        .show()

                }
                override fun onUnavailable() {
                    super.onUnavailable()
                    connectingState = ConnectingStates.FAILED
                    Toast.makeText(context, "Failed to add Wi-Fi network", Toast.LENGTH_SHORT)
                        .show()

                }
            }
        )
    }

    fun endConnection() {

        connectivityManager.bindProcessToNetwork(null)
    }

    @Suppress("DEPRECATION")
    private fun connectSwitchLegacy(ssid: String, password: String) {

        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager

        val wifiConfig = WifiConfiguration().apply {
            SSID = "\"$ssid\""
            preSharedKey = "\"$password\""
            allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK)
        }

        val networkId = wifiManager.addNetwork(wifiConfig)

        if (networkId != -1) {
            wifiManager.disconnect()
            wifiManager.enableNetwork(networkId, true)
            wifiManager.reconnect()
            connectingState = ConnectingStates.SUCCESED
            Toast.makeText(context, "Connecting to Wi-Fi", Toast.LENGTH_SHORT)
                .show()

        } else {
            connectingState = ConnectingStates.FAILED
            Toast.makeText(context, "Failed to add Wi-Fi network", Toast.LENGTH_SHORT)
                .show()

        }
    }
}