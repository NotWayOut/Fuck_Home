package cn.fuckhome.xiaowine.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.fuckhome.xiaowine.App
import cn.fuckhome.xiaowine.R
import com.jaredrummler.ktsh.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Close
import top.yukonga.miuix.kmp.icon.extended.Lock
import top.yukonga.miuix.kmp.icon.extended.Ok
import top.yukonga.miuix.kmp.theme.MiuixTheme

/** libxposed API 101 为模块所需的最低框架 API 版本 */
private const val REQUIRED_API = 101

/** 框架名称未知时的回退值 */
private const val DEFAULT_FRAMEWORK_NAME = "LSPosed"

private enum class FrameworkStatus { Ok, NoRoot, Unsupported, Inactive }

private data class StatusColors(val background: Color, val content: Color)

/**
 * 主页顶部框架状态卡片（显示逻辑参考 MIUINavBar）：
 * 展示模块激活状态、框架名称与 API 版本、Root 权限与作用域数量。
 *
 * Root 仅在模块已激活且框架 API 满足要求时检测，避免未激活时弹出授权弹窗。
 */
@Composable
fun FrameworkStatusCard() {
    val service = App.serviceState.value
    val frameworkName = remember(service) {
        runCatching { service?.frameworkName }.getOrNull() ?: DEFAULT_FRAMEWORK_NAME
    }
    val apiVersion = remember(service) { runCatching { service?.apiVersion }.getOrNull() }
    var rootGranted by remember(service) { mutableStateOf<Boolean?>(null) }

    // 仅在模块已激活且框架版本满足要求时检测 Root，避免无谓的授权弹窗
    LaunchedEffect(service, apiVersion) {
        if (service != null && apiVersion != null && apiVersion >= REQUIRED_API) {
            rootGranted = withContext(Dispatchers.IO) { checkRoot() }
        }
    }

    val status = when {
        service == null -> FrameworkStatus.Inactive
        apiVersion == null || apiVersion < REQUIRED_API -> FrameworkStatus.Unsupported
        rootGranted == false -> FrameworkStatus.NoRoot
        else -> FrameworkStatus.Ok
    }
    val colors = statusColors(status)

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .background(colors.background)
                .padding(horizontal = 20.dp, vertical = 18.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(
                        when (status) {
                            FrameworkStatus.Ok -> R.string.FrameworkOk
                            FrameworkStatus.NoRoot -> R.string.FrameworkNoRoot
                            FrameworkStatus.Unsupported -> R.string.FrameworkUnsupported
                            FrameworkStatus.Inactive -> R.string.FrameworkInactive
                        }
                    ),
                    color = colors.content,
                    style = MiuixTheme.textStyles.title1.copy(
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                )
                Text(
                    text = stringResource(
                        when (status) {
                            FrameworkStatus.Ok -> R.string.FrameworkOkTips
                            FrameworkStatus.NoRoot -> R.string.FrameworkNoRootTips
                            FrameworkStatus.Unsupported -> R.string.FrameworkUnsupportedTips
                            FrameworkStatus.Inactive -> R.string.FrameworkInactiveTips
                        },
                        frameworkName,
                        REQUIRED_API,
                    ),
                    color = colors.content,
                    style = MiuixTheme.textStyles.body2,
                )
            }
            Icon(
                imageVector = when (status) {
                    FrameworkStatus.Ok -> MiuixIcons.Ok
                    FrameworkStatus.NoRoot -> MiuixIcons.Lock
                    else -> MiuixIcons.Close
                },
                contentDescription = null,
                tint = colors.content,
                modifier = Modifier.size(56.dp),
            )
        }
    }
}

/** 通过 su 检测 Root 权限（在 IO 线程执行） */
private fun checkRoot(): Boolean = runCatching {
    val result = Shell.SU.run("id")
    result.isSuccess && result.stdout().contains("uid=0")
}.getOrDefault(false)

@Composable
private fun statusColors(status: FrameworkStatus): StatusColors {
    val isDark = MiuixTheme.colorScheme.background.luminanceIsDark()
    return when (status) {
        FrameworkStatus.Ok -> if (isDark) {
            StatusColors(Color(0xFF1B3B24), Color(0xFFA5D6A7))
        } else {
            StatusColors(Color(0xFFE1F2E5), Color(0xFF1B5E20))
        }

        FrameworkStatus.NoRoot -> if (isDark) {
            StatusColors(Color(0xFF3A2F14), Color(0xFFFFE082))
        } else {
            StatusColors(Color(0xFFFFF8E1), Color(0xFF8D6E00))
        }

        else -> if (isDark) {
            StatusColors(Color(0xFF3E1B1B), Color(0xFFEF9A9A))
        } else {
            StatusColors(Color(0xFFFFEBEE), Color(0xFFB71C1C))
        }
    }
}

private fun Color.luminanceIsDark(): Boolean = (red * 0.299f + green * 0.587f + blue * 0.114f) < 0.5f
