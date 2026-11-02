package cn.fuckhome.xiaowine.activity

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import android.app.AlertDialog
import cn.fuckhome.xiaowine.R
import cn.fuckhome.xiaowine.config.KEY_HIDE_LAUNCHER_ICON
import cn.fuckhome.xiaowine.config.localConfig
import cn.fuckhome.xiaowine.ui.SettingsActions
import cn.fuckhome.xiaowine.ui.SettingsApp
import cn.fuckhome.xiaowine.ui.ThemeState
import com.jaredrummler.ktsh.Shell

class SettingsActivity : ComponentActivity() {

    private var pendingOpenDocument: ((Uri?) -> Unit)? = null
    private var pendingCreateDocument: ((Uri?) -> Unit)? = null

    private val openDocumentLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            pendingOpenDocument?.invoke(uri)
            pendingOpenDocument = null
        }

    private val createDocumentLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument(MIME_JSON)
    ) { uri ->
        pendingCreateDocument?.invoke(uri)
        pendingCreateDocument = null
    }

    private val appReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val titleRes: Int
            val message: String
            when (intent.getStringExtra("app_Type")) {
                "CopyFont" -> {
                    titleRes = R.string.CustomFont
                    message = if (intent.getBooleanExtra("CopyFont", false)) {
                        getString(R.string.CustomFontSuccess)
                    } else {
                        getString(R.string.CustomFontFail) + "\n" + intent.getStringExtra("font_error")
                    }
                }

                "DeleteFont" -> {
                    titleRes = R.string.DeleteFont
                    message = if (intent.getBooleanExtra("DeleteFont", false)) {
                        getString(R.string.DeleteFontSuccess)
                    } else {
                        getString(R.string.DeleteFontFail)
                    }
                }

                else -> return
            }
            AlertDialog.Builder(this@SettingsActivity)
                .setTitle(titleRes)
                .setMessage(message)
                .setPositiveButton(R.string.Ok, null)
                .show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // 边到边显示：状态栏/导航栏透明，由界面自行处理 Insets，避免纯黑状态栏
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        ThemeState.init(localConfig(this).prefs)
        ContextCompat.registerReceiver(
            this,
            appReceiver,
            IntentFilter(ACTION_APP_SERVER),
            ContextCompat.RECEIVER_EXPORTED
        )
applyLauncherAliasState()
        setContent {
            SettingsApp(
                actions = SettingsActions(
                    pickDocument = { onPicked ->
                        pendingOpenDocument = onPicked
                        openDocumentLauncher.launch(arrayOf("*/*"))
                    },
                    createBackupDocument = { fileName, onCreated ->
                        pendingCreateDocument = onCreated
                        createDocumentLauncher.launch(fileName)
                    },
                    onHideIconChange = { hide -> setLauncherAliasEnabled(!hide) },
                    onReset = {
                        localConfig(this).clear()
                        recreate()
                    },
                    onRestartHome = {
                        Thread { Shell("su").run("am force-stop com.miui.home") }.start()
                    },
                    onRefresh = { recreate() },
                )
            )
        }
    }


    /** 按配置应用桌面图标（launcher 别名）显隐。开关打开 = 隐藏图标 */
    override fun onDestroy() {
        unregisterReceiver(appReceiver)
        super.onDestroy()
    }

    fun applyLauncherAliasState() {
        val hide = localConfig(this).prefs.getBoolean(KEY_HIDE_LAUNCHER_ICON, false)
        setLauncherAliasEnabled(!hide)
    }

    private fun setLauncherAliasEnabled(enabled: Boolean) {
        val alias = ComponentName(this, "$packageName.launcher")
        packageManager.setComponentEnabledSetting(
            alias,
            if (enabled) {
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            } else {
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED
            },
            PackageManager.DONT_KILL_APP
        )
    }

    private companion object {
        const val ACTION_APP_SERVER = "MIUIHOME_App_Server"

        const val MIME_JSON = "application/json"
    }
}