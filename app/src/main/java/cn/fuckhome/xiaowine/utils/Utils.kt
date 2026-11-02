package cn.fuckhome.xiaowine.utils

import android.os.SystemClock
import android.text.format.Formatter
import cn.fuckhome.xiaowine.hook.MainHook

object Utils {

    fun Any?.isNull(): Boolean = this == null

    fun Any?.isNotNull(): Boolean = this != null

    fun Any.formatSize(): String = Formatter.formatFileSize(MainHook.appContext, this as Long)

    fun catchNoClass(callback: () -> Unit) {
        runCatching(callback).onFailure { LogUtils.i(it) }
    }

    object Uptime {

        /** 开机时长，格式 HH:MM:SS 或 MM:SS */
        fun get(): String {
            val seconds = SystemClock.elapsedRealtime() / 1000
            val hours = seconds / 3600
            val minutes = seconds % 3600 / 60
            val remainder = seconds % 60
            return if (hours > 0) {
                String.format("%02d:%02d:%02d", hours, minutes, remainder)
            } else {
                String.format("%02d:%02d", minutes, remainder)
            }
        }
    }
}
