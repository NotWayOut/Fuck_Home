package cn.fuckhome.xiaowine.ui

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cn.fuckhome.xiaowine.App
import cn.fuckhome.xiaowine.R
import cn.fuckhome.xiaowine.config.PREFS_GROUP
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.preference.SwitchPreference

/**
 * 界面使用的配置存储。
 *
 * 与 [localConfig] 一致：框架已绑定时使用 XposedService 远程首选项（hook 侧可见），
 * 否则退化为应用私有文件；服务绑定状态变化时自动重新解析。
 */
@Composable
fun rememberSharedPreferences(): SharedPreferences {
    val context = LocalContext.current
    val service = App.serviceState.value
    return remember(context, service) {
        service?.getRemotePreferences(PREFS_GROUP)
            ?: context.getSharedPreferences(PREFS_GROUP, Context.MODE_PRIVATE)
    }
}

/**
 * 记录当前打开的弹窗，使返回键优先关闭弹窗而非返回上一页。
 */
object DialogDismissRegistry {
    private val dismissers = mutableListOf<() -> Unit>()
    var openCount by mutableIntStateOf(0)
        private set

    val hasOpenDialog: Boolean get() = openCount > 0

    fun register(dismiss: () -> Unit): () -> Unit {
        dismissers.add(dismiss)
        openCount = dismissers.size
        return {
            dismissers.remove(dismiss)
            openCount = dismissers.size
        }
    }

    fun dismissTop() {
        dismissers.lastOrNull()?.invoke()
    }
}

/** 弹窗打开期间注册返回键关闭回调 */
@Composable
fun RegisterDialogDismiss(show: Boolean, onDismiss: () -> Unit) {
    if (show) {
        DisposableEffect(show) {
            val unregister = DialogDismissRegistry.register(onDismiss)
            onDispose { unregister() }
        }
    }
}

/** 直接由外部状态驱动的开关行 */
@Composable
fun StateSwitchRow(
    title: String,
    summary: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    SwitchPreference(
        title = title,
        summary = summary,
        checked = checked,
        onCheckedChange = onCheckedChange,
    )
}

/** 绑定布尔配置的开关行，变更即时写入 SharedPreferences */
@Composable
fun SwitchRow(
    key: String,
    title: String,
    summary: String? = null,
    defaultValue: Boolean = false,
    onCheckedChange: ((Boolean) -> Unit)? = null,
) {
    val prefs = rememberSharedPreferences()
    var checked by remember(prefs, key) { mutableStateOf(prefs.getBoolean(key, defaultValue)) }
    SwitchPreference(
        title = title,
        summary = summary,
        checked = checked,
        onCheckedChange = {
            checked = it
            prefs.edit().putBoolean(key, it).apply()
            onCheckedChange?.invoke(it)
        },
    )
}

/** 绑定整型配置，返回当前值与写入函数 */
@Composable
fun rememberPrefInt(key: String, defaultValue: Int): Pair<Int, (Int) -> Unit> {
    val prefs = rememberSharedPreferences()
    val state: MutableState<Int> =
        remember(prefs, key) { mutableStateOf(prefs.getInt(key, defaultValue)) }
    return state.value to { value ->
        state.value = value
        prefs.edit().putInt(key, value).apply()
    }
}

/** 绑定字符串配置，返回当前值与写入函数 */
@Composable
fun rememberPrefString(key: String, defaultValue: String): Pair<String, (String) -> Unit> {
    val prefs = rememberSharedPreferences()
    val state: MutableState<String> = remember(prefs, key) {
        mutableStateOf(prefs.getString(key, defaultValue) ?: defaultValue)
    }
    return state.value to { value ->
        state.value = value
        prefs.edit().putString(key, value).apply()
    }
}

/** 文本输入对话框（颜色、数值等），样式参考 HyperHelper：居中 + 取消/确认并排 */
@Composable
fun InputDialog(
    title: String,
    summary: String?,
    initialValue: String,
    show: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var text by remember(show, initialValue) { mutableStateOf(initialValue) }
    OverlayDialog(
        title = title,
        summary = summary,
        show = show,
        onDismissRequest = onDismiss,
    ) {
        RegisterDialogDismiss(show = show, onDismiss = onDismiss)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            TextField(
                value = text,
                onValueChange = { text = it },
                label = title,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(text = stringResource(R.string.Cancel))
                }
                Button(
                    onClick = {
                        onConfirm(text)
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColorsPrimary(),
                ) {
                    Text(text = stringResource(R.string.Ok))
                }
            }
        }
    }
}

/** 确认对话框，样式与 [InputDialog] 一致 */
@Composable
fun ConfirmDialog(
    title: String,
    summary: String,
    show: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    OverlayDialog(
        title = title,
        summary = summary,
        show = show,
        onDismissRequest = onDismiss,
    ) {
        RegisterDialogDismiss(show = show, onDismiss = onDismiss)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Button(
                onClick = onDismiss,
                modifier = Modifier.weight(1f),
            ) {
                Text(text = stringResource(R.string.Cancel))
            }
            Button(
                onClick = {
                    onConfirm()
                    onDismiss()
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColorsPrimary(),
            ) {
                Text(text = stringResource(R.string.Ok))
            }
        }
    }
}
