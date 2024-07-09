package io.github.lanlacope.nxsharinghelper

import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.drawable.Drawable


fun createDummyResolveInfo(
    packageName: String,
    appName: String,
    appIcon: Drawable
): ResolveInfo {
    return object : ResolveInfo() {
        val activityInfo = ActivityInfo().apply {
            this.packageName = packageName
        }
        override fun loadLabel(pm: PackageManager): CharSequence {
            return appName
        }
        override fun loadIcon(pm: PackageManager): Drawable {
            return appIcon
        }
    }
}