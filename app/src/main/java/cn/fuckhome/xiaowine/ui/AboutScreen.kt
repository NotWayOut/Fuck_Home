package cn.fuckhome.xiaowine.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import cn.fuckhome.xiaowine.R
import cn.fuckhome.xiaowine.utils.ActivityUtils
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.preference.ArrowPreference

/** 引用与致谢页面 */
@Composable
fun AboutScreen(onBack: (() -> Unit)?) {
    val context = LocalContext.current
    val references = listOf(
        stringResource(R.string.Xposed) to "https://github.com/rovo89/Xposed",
        stringResource(R.string.LSPosed) to "https://github.com/LSPosed/LSPosed",
        stringResource(R.string.libXposed) to "https://github.com/libxposed",
        stringResource(R.string.blockmiui) to "https://github.com/Block-Network/blockmiui",
        stringResource(R.string.EzXHelper) to "https://github.com/KyuubiRan/EzXHelper",
        stringResource(R.string.miuix) to "https://github.com/compose-miuix-ui/miuix",
        stringResource(R.string.XiaomiHelper) to "https://github.com/HowieHChen/XiaomiHelper",
    )

    SettingsScaffold(title = stringResource(R.string.About), onBack = onBack) {
        item {
            SmallTitle(text = stringResource(R.string.Author))
        }
        item {
            Card {
                ArrowPreference(
                    title = DOT_PREFIX + stringResource(R.string.AuthorXiaowine),
                    summary = stringResource(R.string.AuthorXiaowineSummary),
                    onClick = { ActivityUtils.openUrl(context, "https://github.com/xiaowine") },
                )
                ArrowPreference(
                    title = DOT_PREFIX + stringResource(R.string.AuthorNotWayOut),
                    summary = stringResource(R.string.AuthorNotWayOutSummary),
                    onClick = { ActivityUtils.openUrl(context, "https://github.com/NotWayOut") },
                )
                ArrowPreference(
                    title = DOT_PREFIX + stringResource(R.string.Coolapk),
                    summary = "https://www.coolapk.com/apk/cn.fuckhome.xiaowine",
                    onClick = {
                        ActivityUtils.openUrl(context, "https://www.coolapk.com/apk/cn.fuckhome.xiaowine")
                    },
                )
            }
        }
        item {
            SmallTitle(text = stringResource(R.string.ThkListTips))
        }
        item {
            Card {
                references.forEach { (title, url) ->
                    ArrowPreference(
                        title = DOT_PREFIX + title,
                        summary = url,
                        onClick = { ActivityUtils.openUrl(context, url) },
                    )
                }
            }
        }
    }
}
