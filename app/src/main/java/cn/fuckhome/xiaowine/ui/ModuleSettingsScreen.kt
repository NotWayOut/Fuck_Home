package cn.fuckhome.xiaowine.ui

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cn.fuckhome.xiaowine.BuildConfig
import cn.fuckhome.xiaowine.R
import cn.fuckhome.xiaowine.config.KEY_DEBUG
import cn.fuckhome.xiaowine.config.KEY_HIDE_LAUNCHER_ICON
import cn.fuckhome.xiaowine.config.KEY_MAIN_SWITCH
import cn.fuckhome.xiaowine.utils.ActivityUtils
import cn.fuckhome.xiaowine.utils.BackupUtils
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.DropdownItem
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.preference.WindowSpinnerPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * 模块设置页：总开关 + 主题设置 + 菜单功能（由主题设置页与设置页合并而来）。
 */
@Composable
fun ModuleSettingsScreen(onBack: (() -> Unit)?, actions: SettingsActions) {
    val context = LocalContext.current
    val prefs = rememberSharedPreferences()
    var showResetDialog by remember { mutableStateOf(false) }
    val resetSuccess = stringResource(R.string.ResetSuccess)

    val modeLabels = listOf(
        stringResource(R.string.ThemeModeLight),
        stringResource(R.string.ThemeModeDark),
        stringResource(R.string.ThemeModeSystem),
    )
    val modeValues = listOf(ThemeMode.LIGHT, ThemeMode.DARK, ThemeMode.SYSTEM)
    val modeIndex = modeValues.indexOf(ThemeState.mode).coerceAtLeast(0)

    val paletteLabels = PaletteStyle.entries.map { it.label }
    val paletteIndex = PaletteStyle.entries.indexOf(ThemeState.paletteStyle).coerceAtLeast(0)

    val specLabels = ColorSpec.entries.map { it.label }
    val specIndex = ColorSpec.entries.indexOf(ThemeState.colorSpec).coerceAtLeast(0)

    SettingsScaffold(title = stringResource(R.string.ModuleSettings), onBack = onBack) {
        // 总开关
        item {
            Card {
                SwitchRow(
                    key = KEY_MAIN_SWITCH,
                    title = stringResource(R.string.MainSwitch),
                )
            }
        }
        // 主题设置
        item {
            SmallTitle(text = stringResource(R.string.ThemeSettings))
        }
        item {
            Card {
                WindowSpinnerPreference(
                    title = stringResource(R.string.ThemeMode),
                    summary = modeLabels[modeIndex],
                    items = modeLabels.map { DropdownItem(title = it) },
                    selectedIndex = modeIndex,
                    onSelectedIndexChange = { ThemeState.updateMode(modeValues[it]) },
                )
                StateSwitchRow(
                    title = stringResource(R.string.UseMiuixMonet),
                    summary = stringResource(R.string.UseMiuixMonetSummary),
                    checked = ThemeState.useMiuixMonet,
                    onCheckedChange = { ThemeState.updateUseMiuixMonet(it) },
                )
            }
        }
        if (ThemeState.useMiuixMonet) {
            item {
                Card {
                    StateSwitchRow(
                        title = stringResource(R.string.UseDynamicColor),
                        summary = stringResource(R.string.UseDynamicColorSummary),
                        checked = ThemeState.useDynamicColor,
                        onCheckedChange = { ThemeState.updateUseDynamicColor(it) },
                    )
                }
            }
            item {
                Card {
                    WindowSpinnerPreference(
                        title = stringResource(R.string.PaletteStyle),
                        summary = paletteLabels[paletteIndex],
                        items = paletteLabels.map { DropdownItem(title = it) },
                        selectedIndex = paletteIndex,
                        onSelectedIndexChange = { ThemeState.updatePaletteStyle(PaletteStyle.entries[it]) },
                    )
                    WindowSpinnerPreference(
                        title = stringResource(R.string.ColorSpec),
                        summary = specLabels[specIndex],
                        items = specLabels.map { DropdownItem(title = it) },
                        selectedIndex = specIndex,
                        onSelectedIndexChange = { ThemeState.updateColorSpec(ColorSpec.entries[it]) },
                    )
                }
            }
            if (!(ThemeState.useDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)) {
                item {
                    SmallTitle(text = stringResource(R.string.ThemeColor))
                }
                item {
                    Card {
                        SeedColorRow()
                    }
                }
            }
        }
        // 菜单功能
        item {
            Card {
                SwitchRow(
                    key = KEY_HIDE_LAUNCHER_ICON,
                    title = stringResource(R.string.HideDeskIcon),
                    defaultValue = false,
                    onCheckedChange = { actions.onHideIconChange(it) },
                )
                SwitchRow(
                    key = KEY_DEBUG,
                    title = stringResource(R.string.DebugMode),
                )
            }
        }
        item {
            Card {
                ArrowPreference(
                    title = stringResource(R.string.ResetModule),
                    onClick = { showResetDialog = true },
                )
                ArrowPreference(
                    title = stringResource(R.string.ReStartHome),
                    onClick = actions.onRestartHome,
                )
                ArrowPreference(
                    title = stringResource(R.string.Backup),
                    onClick = {
                        actions.createBackupDocument(BackupUtils.backupFileName()) { uri ->
                            BackupUtils.handleCreateDocument(context, prefs, uri)
                        }
                    },
                )
                ArrowPreference(
                    title = stringResource(R.string.Recovery),
                    onClick = {
                        actions.pickDocument { uri ->
                            BackupUtils.handleReadDocument(context, prefs, uri)
                            actions.onRefresh()
                        }
                    },
                )
            }
        }
        item {
            Card {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text("${stringResource(R.string.ModulePackName)}: ${BuildConfig.APPLICATION_ID}")
                    Text(
                        "${stringResource(R.string.ModuleVersion)}: " +
                            "${BuildConfig.VERSION_NAME}(${BuildConfig.VERSION_CODE})-${BuildConfig.BUILD_TYPE}"
                    )
                    Text(
                        "${stringResource(R.string.BuildTime)}: " +
                            android.text.format.DateFormat.format(
                                "yyyy-MM-dd HH:mm:ss",
                                BuildConfig.BUILD_TIME.toLong()
                            )
                    )
                }
            }
        }
    }

    ConfirmDialog(
        title = stringResource(R.string.ResetModuleDialog),
        summary = stringResource(R.string.ResetModuleDialogTips),
        show = showResetDialog,
        onDismiss = { showResetDialog = false },
        onConfirm = {
            actions.onReset()
            ActivityUtils.showToast(context, resetSuccess)
        },
    )
}

@Composable
private fun SeedColorRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        presetSeedColors.forEach { color ->
            val selected = ThemeState.seedColor == color
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(color))
                    .border(
                        width = if (selected) 3.dp else 0.dp,
                        color = if (selected) {
                            MiuixTheme.colorScheme.primary
                        } else {
                            Color.Transparent
                        },
                        shape = CircleShape,
                    )
                    .clickable { ThemeState.updateSeedColor(color) },
            )
        }
    }
}
