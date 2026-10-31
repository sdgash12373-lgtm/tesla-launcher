package com.sdgash.carlauncher.data

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Process
import android.os.UserHandle
import android.provider.Settings
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.Collator
import java.util.Locale

/** One launchable activity on the device. */
data class AppEntry(
    val label: String,
    val component: ComponentName,
    val user: UserHandle,
    val icon: ImageBitmap,
) {
    val packageName: String get() = component.packageName
    /** Stable key used for the dock and for list keys. */
    val key: String get() = component.flattenToShortString()
}

/** Reads installed apps through [LauncherApps], the same API real launchers use. */
class AppRepository(private val context: Context) {

    private val launcherApps = context.getSystemService(LauncherApps::class.java)
    private val iconSizePx = (64 * context.resources.displayMetrics.density).toInt().coerceAtLeast(96)

    suspend fun loadApps(): List<AppEntry> = withContext(Dispatchers.IO) {
        val self = context.packageName
        val collator = Collator.getInstance(Locale.getDefault())
        launcherApps.profiles.flatMap { user ->
            launcherApps.getActivityList(null, user)
        }
            .filter { it.componentName.packageName != self }
            .map { info ->
                AppEntry(
                    label = info.label?.toString() ?: info.componentName.packageName,
                    component = info.componentName,
                    user = info.user,
                    icon = drawableToBitmap(info.getBadgedIcon(0)),
                )
            }
            .sortedWith { a, b -> collator.compare(a.label, b.label) }
    }

    private fun drawableToBitmap(d: Drawable): ImageBitmap =
        d.toBitmap(iconSizePx, iconSizePx).asImageBitmap()

    fun launch(app: AppEntry) {
        runCatching {
            launcherApps.startMainActivity(app.component, app.user, null, null)
        }
    }

    fun openAppInfo(app: AppEntry) {
        runCatching {
            launcherApps.startAppDetailsActivity(app.component, app.user, null, null)
        }.onFailure {
            context.startActivity(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${app.packageName}"))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }

    fun openSystemSettings() {
        runCatching {
            context.startActivity(Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }

    fun openHomeSettings() {
        runCatching {
            context.startActivity(Intent(Settings.ACTION_HOME_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }.onFailure { openSystemSettings() }
    }

    /** Calls [onChange] whenever an app is installed, removed or updated. */
    fun registerChanges(onChange: () -> Unit): () -> Unit {
        val callback = object : LauncherApps.Callback() {
            override fun onPackageRemoved(packageName: String?, user: UserHandle?) = onChange()
            override fun onPackageAdded(packageName: String?, user: UserHandle?) = onChange()
            override fun onPackageChanged(packageName: String?, user: UserHandle?) = onChange()
            override fun onPackagesAvailable(p: Array<out String>?, user: UserHandle?, r: Boolean) = onChange()
            override fun onPackagesUnavailable(p: Array<out String>?, user: UserHandle?, r: Boolean) = onChange()
        }
        launcherApps.registerCallback(callback)
        return { launcherApps.unregisterCallback(callback) }
    }

    companion object {
        val myUser: UserHandle get() = Process.myUserHandle()
    }
}
