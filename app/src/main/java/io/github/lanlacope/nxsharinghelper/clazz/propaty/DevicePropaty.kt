package io.github.lanlacope.nxsharinghelper.clazz.propaty

import android.os.Build
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun isAfterAndroidX(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
}

fun getSimpleDate(): String {
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        return dateFormat.format(Date())
}
