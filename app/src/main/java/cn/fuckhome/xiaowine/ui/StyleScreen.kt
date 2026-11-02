package cn.fuckhome.xiaowine.ui

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.Gravity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import cn.fuckhome.xiaowine.R
import cn.fuckhome.xiaowine.config.KEY_BG_COLOR
import cn.fuckhome.xiaowine.config.KEY_BG_CORNERS
import cn.fuckhome.xiaowine.config.KEY_COLOR
import cn.fuckhome.xiaowine.config.KEY_GRAVITY
import cn.fuckhome.xiaowine.config.KEY_MARGIN_LEFT_LANDSCAPE
import cn.fuckhome.xiaowine.config.KEY_MARGIN_LEFT_PORTRAIT
import cn.fuckhome.xiaowine.config.KEY_MARGIN_TOP_LANDSCAPE
import cn.fuckhome.xiaowine.config.KEY_MARGIN_TOP_PORTRAIT
import cn.fuckhome.xiaowine.config.KEY_UNIT
import cn.fuckhome.xiaowine.hook.HomePackages
import cn.fuckhome.xiaowine.utils.ActivityUtils
import cn.fuckhome.xiaowine.utils.FileUtils
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.preference.OverlayDropdownPreference


@Composable
fun StyleScreen(onBack: (() -> Unit)?, actions: SettingsActions) {
    val context = LocalContext.current

    val colorTitle = stringResource(R.string.Color)
    val bgColorTitle = stringResource(R.string.BackgroundColor)
    val cornersTitle = stringResource(R.string.BgCorners)
    val colorTips = stringResource(R.string.LyricColorTips)
    val cornersTips = stringResource(R.string.BgCornersTips)
    val inputError = stringResource(R.string.InputError)
    val colorError = stringResource(R.string.LyricColorError)

    val (color, setColor) = rememberPrefString(KEY_COLOR, "")
    val (bgColor, setBgColor) = rememberPrefString(KEY_BG_COLOR, "#00000000")
    val corners = rememberPrefInt(KEY_BG_CORNERS, 30)
    val gravity = rememberPrefInt(KEY_GRAVITY, Gravity.START)

    val gravityLabels = listOf(
        stringResource(R.string.CENTER),
        stringResource(R.string.START),
        stringResource(R.string.END),
    )
    val gravityValues = listOf(Gravity.CENTER, Gravity.START, Gravity.END)
    var gravityIndex by remember {
        mutableIntStateOf(gravityValues.indexOf(gravity.first).coerceAtLeast(0))
    }

    var showColorDialog by remember { mutableStateOf(false) }
    var showBgColorDialog by remember { mutableStateOf(false) }
    var showCornersDialog by remember { mutableStateOf(false) }
    var showMarginKey by remember { mutableStateOf<String?>(null) }
    var showFontPathDialog by remember { mutableStateOf(false) }

    val fontPathTitle = stringResource(R.string.FontPath)
    val fontPathTips = stringResource(R.string.FontPathTips)
    val chooseFont = stringResource(R.string.ChooseFont)
    SettingsScaffold(
        title = stringResource(R.string.AddInformationStyle),
        onBack = onBack,
        overlay = {
            InputDialog(
                title = colorTitle,
                summary = colorTips,
                initialValue = color,
                show = showColorDialog,
                onDismiss = { showColorDialog = false },
                onConfirm = { value ->
                    if (value.isNotEmpty() && runCatching { Color.parseColor(value) }.isSuccess) {
                        setColor(value)
                    } else {
                        setColor("")
                        ActivityUtils.showToast(context, colorError)
                    }
                },
            )
            InputDialog(
                title = bgColorTitle,
                summary = colorTips,
                initialValue = bgColor,
                show = showBgColorDialog,
                onDismiss = { showBgColorDialog = false },
                onConfirm = { value ->
                    if (value.isNotEmpty() && runCatching { Color.parseColor(value) }.isSuccess) {
                        setBgColor(value)
                    } else {
                        setBgColor("#00000000")
                        ActivityUtils.showToast(context, colorError)
                    }
                },
            )
            InputDialog(
                title = cornersTitle,
                summary = cornersTips,
                initialValue = corners.first.toString(),
                show = showCornersDialog,
                onDismiss = { showCornersDialog = false },
                onConfirm = { value ->
                    val intValue = value.toIntOrNull()
                    if (intValue != null && intValue in 0..100) {
                        corners.second(intValue)
                    } else {
                        corners.second(0)
                        ActivityUtils.showToast(context, inputError)
                    }
                },
            )
            InputDialog(
                title = fontPathTitle,
                summary = fontPathTips,
                initialValue = "",
                show = showFontPathDialog,
                onDismiss = { showFontPathDialog = false },
                onConfirm = { path ->
                    if (path.isNotEmpty()) {
                        sendFontBroadcast(context, "copy_font", path)
                    } else {
                        ActivityUtils.showToast(context, inputError)
                    }
                },
            )
        },
    ) {
        item {
            Card {
                ArrowPreference(
                    title = colorTitle,
                    summary = color.ifEmpty { null },
                    onClick = { showColorDialog = true },
                )
                ArrowPreference(
                    title = bgColorTitle,
                    summary = bgColor,
                    onClick = { showBgColorDialog = true },
                )
                ArrowPreference(
                    title = cornersTitle,
                    summary = corners.first.toString(),
                    onClick = { showCornersDialog = true },
                )
                OverlayDropdownPreference(
                    title = stringResource(R.string.Gravity),
                    summary = gravityLabels[gravityIndex],
                    items = gravityLabels,
                    selectedIndex = gravityIndex,
                    onSelectedIndexChange = {
                        gravityIndex = it
                        gravity.second(gravityValues[it])
                    },
                    onExpandedChange = {},
                )
                SwitchRow(
                    key = KEY_UNIT,
                    title = stringResource(R.string.UnitMargin),
                    defaultValue = true,
                )
            }
        }
        item {
            Card {
                MarginRow(
                    title = stringResource(R.string.LeftMargin0),
                    key = KEY_MARGIN_LEFT_PORTRAIT,
                    show = showMarginKey == KEY_MARGIN_LEFT_PORTRAIT,
                    onShow = { showMarginKey = KEY_MARGIN_LEFT_PORTRAIT },
                    onDismiss = { showMarginKey = null },
                    inputError = inputError,
                )
                MarginRow(
                    title = stringResource(R.string.TopMargin0),
                    key = KEY_MARGIN_TOP_PORTRAIT,
                    show = showMarginKey == KEY_MARGIN_TOP_PORTRAIT,
                    onShow = { showMarginKey = KEY_MARGIN_TOP_PORTRAIT },
                    onDismiss = { showMarginKey = null },
                    inputError = inputError,
                )
                MarginRow(
                    title = stringResource(R.string.LeftMargin1),
                    key = KEY_MARGIN_LEFT_LANDSCAPE,
                    show = showMarginKey == KEY_MARGIN_LEFT_LANDSCAPE,
                    onShow = { showMarginKey = KEY_MARGIN_LEFT_LANDSCAPE },
                    onDismiss = { showMarginKey = null },
                    inputError = inputError,
                )
                MarginRow(
                    title = stringResource(R.string.TopMargin1),
                    key = KEY_MARGIN_TOP_LANDSCAPE,
                    show = showMarginKey == KEY_MARGIN_TOP_LANDSCAPE,
                    onShow = { showMarginKey = KEY_MARGIN_TOP_LANDSCAPE },
                    onDismiss = { showMarginKey = null },
                    inputError = inputError,
                )
                ArrowPreference(
                    title = stringResource(R.string.ChooseFont),
                    summary = chooseFont,
                    onClick = {
                        actions.pickDocument { uri ->
                            if (uri == null) return@pickDocument
                            val path = FileUtils(context).getFilePathByUri(uri)
                            if (path.isNullOrEmpty()) {
                                ActivityUtils.showToast(context, inputError)
                            } else {
                                sendFontBroadcast(context, "copy_font", path)
                            }
                        }
                    },
                )
                ArrowPreference(
                    title = fontPathTitle,
                    summary = fontPathTips,
                    onClick = { showFontPathDialog = true },
                )
                ArrowPreference(
                    title = stringResource(R.string.DeleteFont),
                    onClick = { sendFontBroadcast(context, "delete_font", null) },
                )
            }
        }
    }
}

@Composable
private fun MarginRow(
    title: String,
    key: String,
    show: Boolean,
    onShow: () -> Unit,
    onDismiss: () -> Unit,
    inputError: String,
) {
    val context = LocalContext.current
    val prefs = rememberSharedPreferences()
    val useScale = prefs.getBoolean(KEY_UNIT, true)
    val value = rememberPrefInt(key, 0)
    val tips = if (useScale) {
        stringResource(R.string.MarginTips1)
    } else {
        stringResource(R.string.MarginTips2)
    }
    val range = if (useScale) -100..100 else -2000..2000
    ArrowPreference(
        title = title,
        summary = value.first.toString(),
        onClick = onShow,
    )
    InputDialog(
        title = title,
        summary = tips,
        initialValue = value.first.toString(),
        show = show,
        onDismiss = onDismiss,
        onConfirm = { text ->
            val intValue = text.toIntOrNull()
            if (intValue != null && intValue in range) {
                value.second(intValue)
            } else {
                value.second(0)
                ActivityUtils.showToast(context, inputError)
            }
        },
    )
}

/** 向桌面进程广播字体操作（同时兼容 MIUI 桌面与 POCO 桌面） */
private fun sendFontBroadcast(context: Context, type: String, path: String?) {
    HomePackages.list.forEach { packageName ->
        val intent = Intent(ACTION_HOME_SERVER).apply {
            putExtra("Type", type)
            if (path != null) putExtra("Font_Path", path)
            setPackage(packageName)
        }
        context.sendBroadcast(intent)
    }
}

private const val ACTION_HOME_SERVER = "MIUIHOME_Server"
