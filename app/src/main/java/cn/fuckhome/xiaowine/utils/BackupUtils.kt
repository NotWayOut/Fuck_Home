package cn.fuckhome.xiaowine.utils

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import cn.fuckhome.xiaowine.App
import cn.fuckhome.xiaowine.config.PREFS_GROUP
import org.json.JSONObject
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.time.LocalDateTime

object BackupUtils {

    private const val TAG = "Fuck_Home"
    private const val FLOAT_PREFIX = "Float:"

    fun backupFileName(): String = "Fuck_Home_${LocalDateTime.now()}.json"

    /** 将配置写入用户选择的文档 */
    fun handleCreateDocument(context: Context, prefs: SharedPreferences, uri: Uri?) {
        uri ?: return
        try {
            context.contentResolver.openOutputStream(uri)?.use { output ->
                BufferedWriter(OutputStreamWriter(output)).use { writer ->
                    writer.write(toBackupJson(prefs))
                }
            }
            ActivityUtils.showToast(context, "save ok")
        } catch (e: Throwable) {
            Log.e(TAG, "backup failed", e)
            ActivityUtils.showToast(context, "save fail\n$e")
        }
    }

    /** 从用户选择的文档恢复配置 */
    fun handleReadDocument(context: Context, prefs: SharedPreferences, uri: Uri?) {
        uri ?: return
        try {
            val content = context.contentResolver.openInputStream(uri)?.use { input ->
                BufferedReader(InputStreamReader(input)).use { it.readText() }
            }
            if (content.isNullOrEmpty()) {
                ActivityUtils.showToast(context, "load fail: empty file")
                return
            }
            val backup = parseBackupJson(content)
            applyBackup(prefs, backup)
            mirrorToRemotePreferences(backup)
            ActivityUtils.showToast(context, "load ok")
        } catch (e: Throwable) {
            Log.e(TAG, "restore failed", e)
            ActivityUtils.showToast(context, "load fail\n$e")
        }
    }

    /**
     * 将恢复的配置同步到框架侧远程首选项（若已绑定）。
     * 框架实现将该目录映射为模块 shared_prefs 时为空操作，
     * 此处保证两种实现下被 Hook 进程都能读到恢复后的配置。
     */
    private fun mirrorToRemotePreferences(backup: Map<String, Any>) {
        val service = App.mService ?: return
        val remote = runCatching { service.getRemotePreferences(PREFS_GROUP) }.getOrNull() ?: return
        runCatching { applyBackup(remote, backup) }.onFailure { Log.w(TAG, "mirror to remote prefs failed", it) }
    }

    /**
     * 解析备份 JSON 为键值映射。
     * 兼容旧版备份：浮点数以 "Float:<x1000 后的整数>" 字符串存储。
     */
    fun parseBackupJson(content: String): Map<String, Any> {
        val json = JSONObject(content.trimStart('\uFEFF', ' ', '\n', '\r', '\t'))
        val result = LinkedHashMap<String, Any>()
        for (key in json.keys()) {
            when (val value = json.get(key)) {
                is String -> if (value.startsWith(FLOAT_PREFIX)) {
                    result[key] = value.removePrefix(FLOAT_PREFIX).toFloat() / 1000f
                } else {
                    result[key] = value
                }

                is Boolean -> result[key] = value
                is Int -> result[key] = value
                is Long -> result[key] = value.toInt()
                is Double -> result[key] = value.toFloat()
            }
        }
        return result
    }

    /** 将解析后的配置写入 SharedPreferences */
    fun applyBackup(prefs: SharedPreferences, backup: Map<String, Any>) {
        val editor = prefs.edit()
        for ((key, value) in backup) {
            when (value) {
                is Boolean -> editor.putBoolean(key, value)
                is Int -> editor.putInt(key, value)
                is Float -> editor.putFloat(key, value)
                is String -> editor.putString(key, value)
            }
        }
        editor.apply()
    }

    /** 生成备份 JSON（浮点数转为 "Float:<x1000 后的整数>" 字符串以兼容旧版） */
    fun toBackupJson(prefs: SharedPreferences): String = JSONObject().also { json ->
        for ((key, value) in prefs.all) {
            when (value) {
                is Float -> json.put(key, FLOAT_PREFIX + (value * 1000).toInt())
                else -> json.put(key, value)
            }
        }
    }.toString()
}
