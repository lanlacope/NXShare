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
import android.net.wifi.WifiNetworkSuggestion
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import io.github.lanlacope.nxsharinghelper.R
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
                if (SettingManager(context).getSubstituteConnectionEnabled()){
                    connectSwitch(config.ssid, config.password, onConnect)
                } else {
                    connectSwitchSubstitute(config.ssid, config.password, onConnect)
                }
            } else {
                connectSwitchLegacy(config.ssid, config.password, onConnect)
            }
        } catch (e: Exception) {
            e.printStackTrace()
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

        connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        networkCallback = object : NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                networkCallback = this
                if (connectivityManager.bindProcessToNetwork(network)) {
                    onConnect()
                } else {
                    noficateFaild()
                    disconnection()
                }
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                disconnection()
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
        } else {
            noficateFaild()
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun connectSwitchSubstitute(
        ssid: String,
        password: String,
        onConnect: () -> Unit
    ) {
        val wifiNetworkSuggestion = WifiNetworkSuggestion.Builder()
            .setSsid(ssid)
            .setWpa2Passphrase(password)
            .setIsAppInteractionRequired(true)
            .build()

        val networkRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()

        val suggestionsList = listOf(wifiNetworkSuggestion)
        val status = wifiManager.addNetworkSuggestions(suggestionsList)

        if (status != WifiManager.STATUS_NETWORK_SUGGESTIONS_SUCCESS) {
            noficateFaild()
            return
        }

        connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        networkCallback = object : NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                networkCallback = this
                if (connectivityManager.bindProcessToNetwork(network)) {
                    onConnect()
                } else {
                    disconnection()
                    noficateFaild()
                }
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                disconnection()
            }
        }

        connectivityManager.registerNetworkCallback(
            networkRequest,
            networkCallback as NetworkCallback
        )
    }

    private fun noficateFaild() {
        Toast.makeText(context, context.getString(R.string.failed_connect), Toast.LENGTH_SHORT)
            .show()
    }
}