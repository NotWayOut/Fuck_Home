package cn.fuckhome.xiaowine.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.fuckhome.xiaowine.App
import cn.fuckhome.xiaowine.BuildConfig
import cn.fuckhome.xiaowine.R
import com.jaredrummler.ktsh.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Close
import top.yukonga.miuix.kmp.icon.extended.Lock
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType

/** libxposed API 101 为模块所需的最低框架 API 版本 */
private const val REQUIRED_API = 101

/** 框架名称未知时的回退值 */
private const val DEFAULT_FRAMEWORK_NAME = "LSPosed"

private enum class FrameworkStatus { Ok, NoRoot, Unsupported, Inactive }

private data class StatusColors(val background: Color, val accent: Color)

/**
 * 主页顶部框架状态卡片（显示逻辑参考 MIUINavBar）：
 * 展示模块激活状态、框架名称与 API 版本、Root 权限与作用域数量。
 *
 * 外观与交互移植自 HyperEars 主页头部状态卡：大面积容器色 + 溢出的强调色图标，
 * 并带 miuix 的 Tilt 触碰反馈（点按可重新检测状态）。
 */
@Composable
fun FrameworkStatusCard() {
    val service = App.serviceState.value
    val frameworkName = remember(service) {
        runCatching { service?.frameworkName }.getOrNull() ?: DEFAULT_FRAMEWORK_NAME
    }
    val apiVersion = remember(service) { runCatching { service?.apiVersion }.getOrNull() }
    var rootGranted by remember(service) { mutableStateOf<Boolean?>(null) }
    var refreshTick by remember { mutableIntStateOf(0) }

    // 仅在模块已激活且框架版本满足要求时检测 Root，避免无谓的授权弹窗
    LaunchedEffect(service, apiVersion, refreshTick) {
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

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.defaultColors(
            color = colors.background,
            contentColor = MiuixTheme.colorScheme.onSurface,
        ),
        onClick = { refreshTick++ },
        showIndication = true,
        pressFeedbackType = PressFeedbackType.Tilt,
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(50.dp, 38.dp),
                contentAlignment = Alignment.BottomEnd,
            ) {
                Icon(
                    imageVector = when (status) {
                        FrameworkStatus.Ok -> ImageVector.vectorResource(
                            R.drawable.ic_check_circle_outline
                        )

                        FrameworkStatus.NoRoot -> MiuixIcons.Lock
                        else -> MiuixIcons.Close
                    },
                    contentDescription = null,
                    modifier = Modifier.size(170.dp),
                    tint = colors.accent,
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            ) {
                Text(
                    text = stringResource(
                        when (status) {
                            FrameworkStatus.Ok -> R.string.FrameworkOk
                            FrameworkStatus.NoRoot -> R.string.FrameworkNoRoot
                            FrameworkStatus.Unsupported -> R.string.FrameworkUnsupported
                            FrameworkStatus.Inactive -> R.string.FrameworkInactive
                        }
                    ),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.ModuleVersionLine, BuildConfig.VERSION_NAME),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MiuixTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                )
                Spacer(Modifier.height(36.dp))
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
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MiuixTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                )
            }
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
        FrameworkStatus.Ok -> StatusColors(
            background = if (isDark) Color(0xFF1A3825) else Color(0xFFDFFAE4),
            accent = Color(0xFF36D167),
        )

        FrameworkStatus.NoRoot -> StatusColors(
            background = if (isDark) Color(0xFF3A2F14) else Color(0xFFFFF6DE),
            accent = Color(0xFFFFB300),
        )

        else -> StatusColors(
            background = if (isDark) Color(0xFF381A1A) else Color(0xFFFAEEEE),
            accent = Color(0xFFF72727),
        )
    }
}

private fun Color.luminanceIsDark(): Boolean = (red * 0.299f + green * 0.587f + blue * 0.114f) < 0.5f
