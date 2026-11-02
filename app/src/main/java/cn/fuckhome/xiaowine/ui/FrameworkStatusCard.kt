package cn.fuckhome.xiaowine.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import top.yukonga.miuix.kmp.icon.extended.Ok
import top.yukonga.miuix.kmp.theme.MiuixTheme

/** libXposed API 101 为模块所需的最低框架 API 版本 */
private const val REQUIRED_API = 101

private enum class FrameworkStatus { Ok, NoRoot, Unsupported, Inactive }

private data class StatusColors(val background: Color, val content: Color)

/**
 * 主页顶部框架状态卡片（HyperEars 风格）：
 * 检测模块激活状态、框架 API 版本（>= 101 即支持）与 Root 权限（重启桌面所需）。
 */
@Composable
fun FrameworkStatusCard() {
    val service = App.serviceState.value
    var rootGranted by remember { mutableStateOf<Boolean?>(null) }
    LaunchedEffect(Unit) {
        rootGranted = withContext(Dispatchers.IO) {
            runCatching {
                val result = Shell.SU.run("id")
                result.isSuccess && result.stdout().contains("uid=0")
            }.getOrDefault(false)
        }
    }
    val apiVersion = remember(service) { runCatching { service?.apiVersion }.getOrNull() }
    val scopeCount = remember(service) { runCatching { service?.scope?.size }.getOrNull() }

    val status = when {
        service == null -> FrameworkStatus.Inactive
        apiVersion == null || apiVersion < REQUIRED_API -> FrameworkStatus.Unsupported
        rootGranted == false -> FrameworkStatus.NoRoot
        else -> FrameworkStatus.Ok
    }
    val isDark = when (ThemeState.mode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    val colors = statusColors(status, isDark)

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
                        }
                    ),
                    color = colors.content,
                    style = MiuixTheme.textStyles.body2,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(
                        R.string.FrameworkStats,
                        apiVersion ?: 0,
                        scopeCount ?: 0,
                        stringResource(
                            when (rootGranted) {
                                true -> R.string.RootGranted
                                false -> R.string.RootDenied
                                null -> R.string.RootChecking
                            }
                        ),
                    ),
                    color = colors.content,
                    style = MiuixTheme.textStyles.footnote1,
                )
            }
            Icon(
                imageVector = if (status == FrameworkStatus.Ok) MiuixIcons.Ok else MiuixIcons.Close,
                contentDescription = null,
                tint = colors.content,
                modifier = Modifier.size(56.dp),
            )
        }
    }
}

@Composable
private fun statusColors(status: FrameworkStatus, isDark: Boolean): StatusColors = when (status) {
    FrameworkStatus.Ok -> if (isDark) {
        StatusColors(Color(0xFF1B3B24), Color(0xFFA5D6A7))
    } else {
        StatusColors(Color(0xFFE1F2E5), Color(0xFF1B5E20))
    }

    FrameworkStatus.NoRoot -> if (isDark) {
        StatusColors(Color(0xFF3E2F16), Color(0xFFFFCC80))
    } else {
        StatusColors(Color(0xFFFFF3E0), Color(0xFFE65100))
    }

    else -> if (isDark) {
        StatusColors(Color(0xFF3E1B1B), Color(0xFFEF9A9A))
    } else {
        StatusColors(Color(0xFFFFEBEE), Color(0xFFB71C1C))
    }
}
