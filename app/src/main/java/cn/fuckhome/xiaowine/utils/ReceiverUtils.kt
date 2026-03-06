package cn.fuckhome.xiaowine.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.os.Build

object ReceiverUtils {
    fun safeRegisterReceiver(
        context: Context,
        receiver: BroadcastReceiver,
        filter: IntentFilter,
        exported: Boolean = false
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val flag = if (exported) Context.RECEIVER_EXPORTED else Context.RECEIVER_NOT_EXPORTED
            context.registerReceiver(receiver, filter, flag)
        } else {
            context.registerReceiver(receiver, filter)
        }
    }
}