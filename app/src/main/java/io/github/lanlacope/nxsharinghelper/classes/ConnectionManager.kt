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
import androidx.annotation.RequiresApi
import io.github.lanlacope.nxsharinghelper.isAfterAndroidX

class ConnectionManager(val context: Context) {

    fun start(config: Pair<String, String>, onConnect: () -> Unit) {

        println("start connect")
        try {
            if (isAfterAndroidX()) {
                connectSwitch(config.first, config.second, onConnect)
            } else {
                connectSwitchLegacy(config.first, config.second, onConnect)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    val connectivityManager by lazy {
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun connectSwitch(
        ssid: String,
        password: String,
        onConnect: () -> Unit
    ) {

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
                    onConnect()
                }
            }
        )
    }

    fun disConnection() {
        connectivityManager.bindProcessToNetwork(null)
    }

    @Suppress("DEPRECATION")
    private fun connectSwitchLegacy(
        ssid: String,
        password: String,
        onConnect: () -> Unit
    ) {
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
            onConnect()
        }
    }
}