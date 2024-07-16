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
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import io.github.lanlacope.nxsharinghelper.clazz.propaty.DevicePropaty

@Suppress("unused")
@Composable
fun rememberConnectionManager(): ConnectionManager {
    val context = LocalContext.current
    return remember {
        ConnectionManager(context)
    }
}

@Stable
class ConnectionManager(_context: Context) {

    data class WifiConfig(
        val ssid: String,
        val password: String
    )

    data class OnConnection(
        val onSuccesful: (ConnectionManager) -> Unit,
        val onFailed: (ConnectionManager) -> Unit
    )

    private val context = _context.applicationContext

    private val wifiManager by lazy {
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    }
    private val connectivityManager by lazy {
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    private var lastNetworkId = -1


    fun start(config: WifiConfig, onConnection: OnConnection) {
        println("startConnect")
        try {
            if (DevicePropaty.isAfterAndroidX()) {
                if (!SettingManager(context).getAlternativeConnectionEnabled()){
                    connectSwitch(config.ssid, config.password, onConnection)
                } else {
                    connectSwitchSubstitute(config.ssid, config.password, onConnection)
                }
            } else {
                connectSwitchLegacy(config.ssid, config.password, onConnection)
            }
        } catch (e: Exception) {
            println("oooooooooooooooo")
            onConnection.onFailed(this)
        }
    }

    fun disconnection() {
        if (DevicePropaty.isAfterAndroidX()) {
            connectivityManager.bindProcessToNetwork(null)
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
        onConnect: OnConnection
    ) {

        println("nowConnection")

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
            object : NetworkCallback() {
                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    if (connectivityManager.bindProcessToNetwork(network)) {
                        println("onAvailable OK")
                        onConnect.onSuccesful(this@ConnectionManager)
                    } else {
                        println("onAvailable NO")
                        onConnect.onFailed(this@ConnectionManager)
                    }
                }

                override fun onLost(network: Network) {
                    super.onLost(network)
                    Toast.makeText(context, "DDDDDDDDDDDDDDDDDd", Toast.LENGTH_LONG).show()
                    connectivityManager.unregisterNetworkCallback(this)
                }
            }
        )
    }

    @Suppress("DEPRECATION")
    private fun connectSwitchLegacy(
        ssid: String,
        password: String,
        onConnect: OnConnection
    ) {

        println("nowConnectionL")

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
            onConnect.onSuccesful(this@ConnectionManager)
        } else {
            onConnect.onFailed(this@ConnectionManager)
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun connectSwitchSubstitute(
        ssid: String,
        password: String,
        onConnect: OnConnection
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
            onConnect.onFailed(this@ConnectionManager)
            return
        }

        connectivityManager.registerNetworkCallback(
            networkRequest,
            object : NetworkCallback() {
                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    if (connectivityManager.bindProcessToNetwork(network)) {
                        onConnect.onSuccesful(this@ConnectionManager)
                    } else {
                        disconnection()
                        onConnect.onFailed(this@ConnectionManager)
                    }
                }

                override fun onLost(network: Network) {
                    super.onLost(network)
                    connectivityManager.unregisterNetworkCallback(this)
                }
            }
        )
    }
}