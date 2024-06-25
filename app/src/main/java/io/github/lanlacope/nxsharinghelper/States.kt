package io.github.lanlacope.nxsharinghelper

import android.os.Build
import androidx.lifecycle.viewmodel.viewModelFactory



fun isAfterAndroidX(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
}