package io.github.lanlacope.nxsharinghelper.activitys

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.activity.result.contract.ActivityResultContract
import com.google.zxing.client.android.Intents
import com.google.zxing.client.android.R
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.CaptureManager
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.journeyapps.barcodescanner.ScanOptions
import io.github.lanlacope.nxsharinghelper.SWITCH_LOCALHOST

/*
 * Copyright (C) 2012-2022 ZXing authors, Journey Mobile
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Modifications:
 * - Modified by pecolan.
 */

class SwitchCaptureActivity : Activity() {
    private lateinit var capture: SwitchCaptureManager
    private lateinit var barcodeScannerView: DecoratedBarcodeView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        println("\n\n\ncreate\n\n\n")

        barcodeScannerView = initializeContent()

        capture = SwitchCaptureManager(this, barcodeScannerView)
        capture.initializeFromIntent(intent, savedInstanceState)
        capture.decode()
    }

    protected fun initializeContent(): DecoratedBarcodeView {
        setContentView(R.layout.zxing_capture)
        return findViewById<View>(R.id.zxing_barcode_scanner) as DecoratedBarcodeView
    }

    override fun onResume() {
        super.onResume()
        capture.onResume()
    }

    override fun onPause() {
        super.onPause()
        capture.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        capture.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        capture.onSaveInstanceState(outState)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        capture.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return barcodeScannerView.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event)
    }

    class Contract : ActivityResultContract<ScanOptions, Pair<String, String>?>() {

        override fun createIntent(context: Context, input: ScanOptions): Intent {
                val intentScan = Intent(context, SwitchCaptureActivity::class.java)
                intentScan.setAction(Intents.Scan.ACTION)
                intentScan.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                return intentScan
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Pair<String, String>? {
            println("returnnow")
            if (resultCode == RESULT_OK) {
                println(intent?.action)
                val rawResult = intent?.action ?: ";"
                val splitResult = rawResult.split(";").toTypedArray()
                return Pair(splitResult[0], splitResult[1])
            } else {
                return null
            }
        }
    }
}

/*
 * Copyright (C) 2012-2022 ZXing authors, Journey Mobile
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Modifications:
 *  - Extended by pecolan.
 */
class SwitchCaptureManager(
    val activity: Activity,
    val barcodeView: DecoratedBarcodeView,
) : CaptureManager(activity, barcodeView) {

    private object ResultState {
        const val SUCCESSFUL_CARER: Int = 1
        const val FAILED_NOTSWITCH: Int = -1
        const val FAILED_LOCALHOST: Int = -2
    }

    override fun returnResult(_rawResult: BarcodeResult) {

        barcodeView.pause()

        val parcedResult = parceQr(_rawResult)

        if (parcedResult.second == ResultState.SUCCESSFUL_CARER) {
            activity.setResult(Activity.RESULT_OK, Intent(parcedResult.first))
            closeAndFinish()
        } else {
            barcodeView.resume()
        }
    }

    private fun parceQr(rawResult: BarcodeResult): Pair<String?, Int> {
        val text = rawResult.text

        var ssid: String? = null
        var password: String? = null

        if (!text.startsWith("WIFI:")) {
            if (text == SWITCH_LOCALHOST.INDEX) {
                return Pair(null, ResultState.FAILED_LOCALHOST)
            }
            return Pair(null, ResultState.FAILED_NOTSWITCH)
        }

        val parts = text.substring(5).split(";").toTypedArray()

        for (part in parts) {
            when {
                part.startsWith("S:") -> {
                    ssid = part.substring(2)

                    if (!ssid.startsWith("switch_")) {
                        return Pair(null, ResultState.FAILED_NOTSWITCH)
                    }
                }

                part.startsWith("P:") -> {
                    password = part.substring(2)

                    if (password.length != 8) {
                        return Pair(null, ResultState.FAILED_NOTSWITCH)
                    }
                }

                part.startsWith("T") -> {
                    if (part.substring(2) != "WPA") {
                        return Pair(null, ResultState.FAILED_NOTSWITCH)
                    }
                }

                part.isEmpty() -> {
                    // do nothing
                }

                else -> {
                    return Pair(null, ResultState.FAILED_NOTSWITCH)
                }
            }
        }

        if (!ssid.isNullOrEmpty()
            && !password.isNullOrEmpty()
        ) {
            return Pair("$ssid;$password", ResultState.SUCCESSFUL_CARER)
        } else {
            return Pair("$ssid;$password", ResultState.FAILED_NOTSWITCH)
        }
    }
}