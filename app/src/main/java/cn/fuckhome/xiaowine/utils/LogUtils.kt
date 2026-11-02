package cn.fuckhome.xiaowine.utils

import android.util.Log
import cn.fuckhome.xiaowine.config.KEY_DEBUG
import cn.fuckhome.xiaowine.config.xConfig
import cn.fuckhome.xiaowine.hook.MainHook

object LogUtils {

    private const val TAG = "Fuck_Home"
    private const val MAX_LENGTH = 4000

    fun i(message: Any?) {
        log(message)
    }

    fun e(throwable: Throwable) {
        log(throwable)
    }

    private fun log(message: Any?) {
        if (!MainHook.isModuleLoaded) return
        val content = if (message is Throwable) Log.getStackTraceString(message) else message.toString()
        // 始终输出到 logcat，便于排查；Debug 模式下同时写入框架日志
        content.chunked(MAX_LENGTH).forEach { Log.i(TAG, it) }
        runCatching {
            if (xConfig.getBoolean(KEY_DEBUG)) {
                content.chunked(MAX_LENGTH).forEach { MainHook.instance.log(Log.INFO, TAG, it) }
            }
        }
    }
}
