package cn.fuckhome.xiaowine.activity

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import cn.fuckhome.xiaowine.BuildConfig
import cn.fuckhome.xiaowine.R
import cn.fuckhome.xiaowine.config.KEY_DEBUG
import cn.fuckhome.xiaowine.config.KEY_HIDE_LAUNCHER_ICON
import cn.fuckhome.xiaowine.config.KEY_MAIN_SWITCH
import cn.fuckhome.xiaowine.config.KEY_MEMORY_VIEW
import cn.fuckhome.xiaowine.config.KEY_ZARM_VIEW
import cn.fuckhome.xiaowine.config.localConfig

class SettingsActivity : AppCompatActivity() {

    val config get() = localConfig(this)

    private var pendingOpenDocument: ((Uri?) -> Unit)? = null
    private var pendingCreateDocument: ((Uri?) -> Unit)? = null

    private val appReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.getStringExtra("app_Type")) {
                "CopyFont" -> showDialog(
                    R.string.CustomFont,
                    if (intent.getBooleanExtra("CopyFont", false)) {
                        getString(R.string.CustomFontSuccess)
                    } else {
                        getString(R.string.CustomFontFail) + "\n" + intent.getStringExtra("font_error")
                    }
                )

                "DeleteFont" -> showDialog(
                    R.string.DeleteFont,
                    if (intent.getBooleanExtra("DeleteFont", false)) {
                        getString(R.string.DeleteFontSuccess)
                    } else {
                        getString(R.string.DeleteFontFail)
                    }
                )
            }
        }
    }

    /**
     * 文档选择器：选择任意文件。
     * 使用通配 MIME 避免部分设备为 .json 文件分配非 application/json 类型而导致无法选择。
     */
    private val openDocumentLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            pendingOpenDocument?.invoke(uri)
            pendingOpenDocument = null
        }

    /** 创建备份文档 */
    private val createDocumentLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument(MIME_JSON)
    ) { uri ->
        pendingCreateDocument?.invoke(uri)
        pendingCreateDocument = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        if (supportFragmentManager.findFragmentById(R.id.fragment_container) == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, MainFragment())
                .commit()
        }
        ContextCompat.registerReceiver(
            this,
            appReceiver,
            IntentFilter(ACTION_APP_SERVER),
            ContextCompat.RECEIVER_EXPORTED
        )
        applyLauncherAliasState()
        applyDebugDefaults()
    }

    override fun onDestroy() {
        unregisterReceiver(appReceiver)
        super.onDestroy()
    }

    fun showScreen(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    /** 选择文档（备份恢复、自定义字体等），结果通过 [onPicked] 回调 */
    fun pickDocument(onPicked: (Uri?) -> Unit) {
        pendingOpenDocument = onPicked
        openDocumentLauncher.launch(arrayOf("*/*"))
    }

    /** 创建备份文档，结果通过 [onCreated] 回调 */
    fun createBackupDocument(fileName: String, onCreated: (Uri?) -> Unit) {
        pendingCreateDocument = onCreated
        createDocumentLauncher.launch(fileName)
    }

    /** 按配置应用桌面图标（launcher 别名）显隐；恢复配置后需调用。开关打开 = 隐藏图标 */
    fun applyLauncherAliasState() {
        val alias = ComponentName(this, "$packageName.launcher")
        val hide = config.prefs.getBoolean(KEY_HIDE_LAUNCHER_ICON, false)
        packageManager.setComponentEnabledSetting(
            alias,
            if (hide) {
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED
            } else {
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            },
            PackageManager.DONT_KILL_APP
        )
    }

    /** Debug 构建下默认打开常用开关，便于开发调试 */
    private fun applyDebugDefaults() {
        if (!BuildConfig.DEBUG) return
        config.prefs.edit()
            .putBoolean(KEY_MAIN_SWITCH, true)
            .putBoolean(KEY_MEMORY_VIEW, true)
            .putBoolean(KEY_ZARM_VIEW, true)
            .putBoolean(KEY_DEBUG, true)
            .apply()
    }

    private fun showDialog(titleRes: Int, message: String) {
        AlertDialog.Builder(this)
            .setTitle(titleRes)
            .setMessage(message)
            .setPositiveButton(R.string.Ok, null)
            .show()
    }

    private companion object {
        const val ACTION_APP_SERVER = "MIUIHOME_App_Server"
        const val MIME_JSON = "application/json"
    }
}
