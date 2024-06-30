package io.github.lanlacope.nxsharinghelper

import android.os.Build

fun isAfterAndroidX(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
}