package io.github.lanlacope.nxsharinghelper.clazz

import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSpecifier
import android.os.Build
import androidx.annotation.RequiresApi
import io.github.lanlacope.nxsharinghelper.activity.SwitchCaptureActivity.WifiConfig
import io.github.lanlacope.nxsharinghelper.clazz.propaty.DevicePropaty

class ConnectionManager(_context: Context) {

    private val context = _context.applicationContext

    companion object {
        private lateinit var connectivityManager: ConnectivityManager
        private lateinit var wifiManager: WifiManager
        private var networkCallback: NetworkCallback? = null
        private var lastNetworkId = -1
    }

    fun start(config: WifiConfig, onConnect: () -> Unit) {

        try {
            if (DevicePropaty.isAfterAndroidX()) {
                connectSwitch(config.ssid, config.password, onConnect)
            } else {
                connectSwitchLegacy(config.ssid, config.password, onConnect)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun connectSwitch(
        ssid: String,
        password: String,
        onConnect: () -> Unit
    ) {

        connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val wifiNetworkSpecifier = WifiNetworkSpecifier.Builder()
            .setSsid(ssid)
            .setWpa2Passphrase(password)
            .build()

        val networkRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .setNetworkSpecifier(wifiNetworkSpecifier)
            .build()

        networkCallback = object : NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                networkCallback = this
                if (connectivityManager.bindProcessToNetwork(network)) {
                    onConnect()
                }
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                connectivityManager.unregisterNetworkCallback(this)
            }
        }

        connectivityManager.requestNetwork(
            networkRequest,
            networkCallback as NetworkCallback
        )
    }

    @Suppress("DEPRECATION")
    private fun connectSwitchLegacy(
        ssid: String,
        password: String,
        onConnect: () -> Unit
    ) {

        wifiManager = context.getApplicationContext().getSystemService(Context.WIFI_SERVICE) as WifiManager

        lastNetworkId = wifiManager.connectionInfo.networkId

        if (!wifiManager.isWifiEnabled) {
            wifiManager.isWifiEnabled = true
        }

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

    fun disconnection() {
        if (DevicePropaty.isAfterAndroidX()) {
            connectivityManager.bindProcessToNetwork(null)
            if (networkCallback != null) {
                connectivityManager.unregisterNetworkCallback(networkCallback!!)
            }
        } else {
            @Suppress("DEPRECATION")
            if (lastNetworkId != -1) {
                wifiManager.disconnect()
                wifiManager.enableNetwork(lastNetworkId, true)
                wifiManager.reconnect()
                lastNetworkId = -1
            }
        }
    }
}