package cn.fuckhome.xiaowine.config

import android.content.Context
import android.content.SharedPreferences
import android.view.Gravity
import cn.fuckhome.xiaowine.hook.MainHook

/** 模块与桌面进程共享的配置组名 */
const val PREFS_GROUP = "Fuck_Home_Config"

const val KEY_MAIN_SWITCH = "MainSwitch"
const val KEY_DEBUG = "Debug"
const val KEY_HIDE_LAUNCHER_ICON = "hLauncherIcon"
const val KEY_WARNING = "Warning"
const val KEY_CLEAN_MODE = "CleanMode"
const val KEY_OPTIMIZE_ANIMATION = "optimizeAnimation"
const val KEY_COLOR = "Color"
const val KEY_BG_COLOR = "BgColor"
const val KEY_BG_CORNERS = "BgCorners"
const val KEY_GRAVITY = "Gravity"
const val KEY_UNIT = "Unit"
const val KEY_MEMORY_VIEW = "MemoryView"
const val KEY_ZARM_VIEW = "ZarmView"
const val KEY_STORAGE_VIEW = "StorageView"
const val KEY_UPTIME = "Uptime"
const val KEY_RUNNING_APP_TOTAL = "RunningAppTotal"
const val KEY_RUNNING_SERVICE_TOTAL = "RunningServiceTotal"
const val KEY_MARGIN_LEFT_PORTRAIT = "LeftMargin0"
const val KEY_MARGIN_TOP_PORTRAIT = "TopMargin0"
const val KEY_MARGIN_LEFT_LANDSCAPE = "LeftMargin1"
const val KEY_MARGIN_TOP_LANDSCAPE = "TopMargin1"

class Config(val prefs: SharedPreferences) {

    var mainSwitch: Boolean
        get() = prefs.getBoolean(KEY_MAIN_SWITCH, false)
        set(value) = prefs.edit().putBoolean(KEY_MAIN_SWITCH, value).apply()

    var color: String
        get() = prefs.getString(KEY_COLOR, "") ?: ""
        set(value) = prefs.edit().putString(KEY_COLOR, value).apply()

    var backgroundColor: String
        get() = prefs.getString(KEY_BG_COLOR, "#00000000") ?: "#00000000"
        set(value) = prefs.edit().putString(KEY_BG_COLOR, value).apply()

    var backgroundCorners: Int
        get() = prefs.getInt(KEY_BG_CORNERS, 30)
        set(value) = prefs.edit().putInt(KEY_BG_CORNERS, value).apply()

    var gravity: Int
        get() = prefs.getInt(KEY_GRAVITY, Gravity.START)
        set(value) = prefs.edit().putInt(KEY_GRAVITY, value).apply()

    var useScaleUnit: Boolean
        get() = prefs.getBoolean(KEY_UNIT, true)
        set(value) = prefs.edit().putBoolean(KEY_UNIT, value).apply()

    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean =
        prefs.getBoolean(key, defaultValue)

    fun getInt(key: String, defaultValue: Int = 0): Int = prefs.getInt(key, defaultValue)

    fun getString(key: String, defaultValue: String = ""): String =
        prefs.getString(key, defaultValue) ?: defaultValue

    fun setValue(key: String, value: Any) {
        val editor = prefs.edit()
        when (value) {
            is Int -> editor.putInt(key, value)
            is Long -> editor.putLong(key, value)
            is Float -> editor.putFloat(key, value)
            is Boolean -> editor.putBoolean(key, value)
            is String -> editor.putString(key, value)
        }
        editor.apply()
    }

    fun clear() {
        prefs.edit().clear().apply()
    }
}

/** 被 Hook 进程中的只读配置（由框架提供的远程首选项） */
val xConfig: Config by lazy { Config(MainHook.instance.getRemotePreferences(PREFS_GROUP)) }

/**
 * 模块自身进程中的可写配置。
 *
 * 统一使用模块私有的 shared_prefs 文件：设置界面（PreferenceFragmentCompat）与
 * 备份/恢复均读写同一文件，框架通过该目录向被 Hook 进程提供只读远程配置。
 */
fun localConfig(context: Context): Config =
    Config(context.getSharedPreferences(PREFS_GROUP, Context.MODE_PRIVATE))
