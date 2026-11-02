package cn.fuckhome.xiaowine.ui

import android.app.Activity
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import androidx.navigationevent.compose.rememberNavigationEventDispatcherOwner
import cn.fuckhome.xiaowine.R
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.ThemeColorSpec
import top.yukonga.miuix.kmp.theme.ThemeController
import top.yukonga.miuix.kmp.theme.ThemePaletteStyle

/** 设置界面需要由 Activity 提供的能力 */
data class SettingsActions(
    val pickDocument: (((android.net.Uri?) -> Unit)) -> Unit,
    val createBackupDocument: (String, (android.net.Uri?) -> Unit) -> Unit,
    val onHideIconChange: (Boolean) -> Unit,
    val onReset: () -> Unit,
    val onRestartHome: () -> Unit,
    val onRefresh: () -> Unit,
)

enum class SettingsScreen { Main, ModuleSettings, AddInfo, Style, About }

/** 菜单项前的圆点前缀 */
const val DOT_PREFIX = "•  "

@Composable
fun SettingsApp(actions: SettingsActions) {
    val isDark = when (ThemeState.mode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    val controller = rememberThemeController(isDark)

    // 状态栏图标颜色随明暗主题切换
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDark
        }
    }

    MiuixTheme(controller = controller) {
        // miuix 的 overlay 组件依赖 NavigationEventDispatcher；当前 navigationevent 版本
        // 不会自动从 Activity 解析，这里显式提供（无父级时创建根 dispatcher）
        val activityOwner = LocalNavigationEventDispatcherOwner.current
        val dispatcherOwner = rememberNavigationEventDispatcherOwner(parent = activityOwner)
        CompositionLocalProvider(LocalNavigationEventDispatcherOwner provides dispatcherOwner) {
            AppContent(actions)
        }
    }
}

@Composable
private fun AppContent(actions: SettingsActions) {
    var screen by remember { mutableStateOf(SettingsScreen.Main) }
    val onBack: (() -> Unit)? =
        if (screen == SettingsScreen.Main) null else ({ screen = SettingsScreen.Main })
    BackHandler(enabled = screen != SettingsScreen.Main || DialogDismissRegistry.hasOpenDialog) {
        // 返回键优先关闭已打开的弹窗，其次才返回主页
        if (DialogDismissRegistry.hasOpenDialog) {
            DialogDismissRegistry.dismissTop()
        } else {
            screen = SettingsScreen.Main
        }
    }
    AnimatedContent(
        modifier = Modifier
            .fillMaxSize()
            .background(MiuixTheme.colorScheme.surface),
        targetState = screen,
        transitionSpec = {
            if (targetState.depth > initialState.depth) {
                (slideInHorizontally { width -> width / 3 } + fadeIn()) togetherWith
                    (slideOutHorizontally { width -> -width / 4 } + fadeOut())
            } else {
                (slideInHorizontally { width -> -width / 3 } + fadeIn()) togetherWith
                    (slideOutHorizontally { width -> width / 4 } + fadeOut())
            }
        },
        label = "settings-page-transition",
    ) { current ->
        // 动画期间退场页面仍处于组合中，需阻断其触摸，避免点到上一个页面的控件
        val interactive = current == screen
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (interactive) {
                        Modifier
                    } else {
                        Modifier.pointerInput(Unit) {
                            awaitPointerEventScope {
                                while (true) {
                                    awaitPointerEvent(PointerEventPass.Initial)
                                        .changes.forEach { it.consume() }
                                }
                            }
                        }
                    }
                ),
        ) {
            when (current) {
                SettingsScreen.Main -> MainScreen(onNavigate = { screen = it })
                SettingsScreen.ModuleSettings -> ModuleSettingsScreen(onBack = onBack, actions = actions)
                SettingsScreen.AddInfo -> AddInfoScreen(onBack = onBack)
                SettingsScreen.Style -> StyleScreen(onBack = onBack, actions = actions)
                SettingsScreen.About -> AboutScreen(onBack = onBack)
            }
        }
    }
}

/** 页面层级：主页为 0，其余为 1，用于判断过渡动画方向 */
private val SettingsScreen.depth: Int
    get() = when (this) {
        SettingsScreen.Main -> 0
        else -> 1
    }

@Composable
private fun rememberThemeController(isDark: Boolean): ThemeController {
    val context = LocalContext.current
    return remember(
        ThemeState.mode,
        ThemeState.useMiuixMonet,
        ThemeState.useDynamicColor,
        ThemeState.seedColor,
        ThemeState.paletteStyle,
        ThemeState.colorSpec,
        isDark
    ) {
        buildThemeController(context, isDark)
    }
}

private fun buildThemeController(context: android.content.Context, isDark: Boolean): ThemeController {
    val mode = ThemeState.mode
    if (!ThemeState.useMiuixMonet) {
        return ThemeController(
            colorSchemeMode = when (mode) {
                ThemeMode.SYSTEM -> ColorSchemeMode.System
                ThemeMode.LIGHT -> ColorSchemeMode.Light
                ThemeMode.DARK -> ColorSchemeMode.Dark
            },
            isDark = isDark,
        )
    }
    val keyColor = if (ThemeState.useDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        Color(context.getColor(android.R.color.system_accent1_500))
    } else {
        Color(ThemeState.seedColor)
    }
    return ThemeController(
        colorSchemeMode = when (mode) {
            ThemeMode.SYSTEM -> ColorSchemeMode.MonetSystem
            ThemeMode.LIGHT -> ColorSchemeMode.MonetLight
            ThemeMode.DARK -> ColorSchemeMode.MonetDark
        },
        keyColor = keyColor,
        paletteStyle = when (ThemeState.paletteStyle) {
            PaletteStyle.TonalSpot -> ThemePaletteStyle.TonalSpot
            PaletteStyle.Neutral -> ThemePaletteStyle.Neutral
            PaletteStyle.Vibrant -> ThemePaletteStyle.Vibrant
            PaletteStyle.Expressive -> ThemePaletteStyle.Expressive
            PaletteStyle.Rainbow -> ThemePaletteStyle.Rainbow
            PaletteStyle.Monochrome -> ThemePaletteStyle.Monochrome
        },
        colorSpec = when (ThemeState.colorSpec) {
            ColorSpec.SPEC_2021 -> ThemeColorSpec.Spec2021
            ColorSpec.SPEC_2025 -> ThemeColorSpec.Spec2025
        },
        isDark = isDark,
    )
}

/**
 * 通用页面骨架：顶部标题栏 + 可滚动内容。
 *
 * [overlay] 用于放置 OverlayDialog 等弹窗组件——miuix 的弹窗依赖 Scaffold 的 popupHost，
 * 必须声明在 Scaffold 内部（即本槽位中）才能正常弹出。
 */
@Composable
fun SettingsScaffold(
    title: String,
    onBack: (() -> Unit)? = null,
    overlay: @Composable () -> Unit = {},
    content: LazyListScope.() -> Unit,
) {
    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = title,
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = MiuixIcons.Back,
                                contentDescription = stringResource(R.string.Cancel),
                            )
                        }
                    }
                },
            )
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding(),
                contentPadding = PaddingValues(
                    start = 12.dp,
                    end = 12.dp,
                    top = padding.calculateTopPadding() + 8.dp,
                    bottom = 24.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) { content() }
            overlay()
        }
    }
}
