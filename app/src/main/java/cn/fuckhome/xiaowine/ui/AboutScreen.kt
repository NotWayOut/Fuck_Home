package cn.fuckhome.xiaowine.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cn.fuckhome.xiaowine.R
import cn.fuckhome.xiaowine.utils.ActivityUtils
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.File
import top.yukonga.miuix.kmp.icon.extended.Info
import top.yukonga.miuix.kmp.icon.extended.Link
import top.yukonga.miuix.kmp.icon.extended.Messages
import top.yukonga.miuix.kmp.theme.MiuixTheme

private const val REPO_URL = "https://github.com/NotWayOut/Fuck_Home"

/**
 * 关于页：布局参考 Hyper Helper，内容取自项目 README。
 */
@Composable
fun AboutScreen(onBack: (() -> Unit)?) {
    val context = LocalContext.current

    SettingsScaffold(title = stringResource(R.string.About), onBack = onBack) {
        item {
            Card {
                AboutIconRow(
                    title = stringResource(R.string.BasedOnMiuix),
                    summary = stringResource(R.string.BasedOnMiuixSummary),
                    icon = MiuixIcons.Info,
                    onClick = { ActivityUtils.openUrl(context, "https://github.com/compose-miuix-ui/miuix") },
                )
                AboutIconRow(
                    title = stringResource(R.string.BasedOnLibXposed),
                    summary = stringResource(R.string.BasedOnLibXposedSummary),
                    icon = MiuixIcons.Info,
                    onClick = { ActivityUtils.openUrl(context, "https://github.com/libxposed") },
                )
                AboutIconRow(
                    title = stringResource(R.string.BasedOnUpstream),
                    summary = stringResource(R.string.BasedOnUpstreamSummary),
                    icon = MiuixIcons.Info,
                    onClick = { ActivityUtils.openUrl(context, "https://github.com/xiaowine") },
                )
            }
        }
        item { AboutSectionTitle(stringResource(R.string.Author)) }
        item {
            Card {
                BasicComponent(
                    title = stringResource(R.string.AuthorXiaowine),
                    summary = stringResource(R.string.AuthorXiaowineSummary),
                    startAction = { AvatarImage(R.drawable.avatar_xiaowine) },
                    onClick = { ActivityUtils.openUrl(context, "https://github.com/xiaowine") },
                )
                BasicComponent(
                    title = stringResource(R.string.AuthorNotWayOut),
                    summary = stringResource(R.string.AuthorNotWayOutSummary),
                    startAction = { AvatarImage(R.drawable.avatar_notwayout) },
                    onClick = { ActivityUtils.openUrl(context, "https://github.com/NotWayOut") },
                )
            }
        }
        item { AboutSectionTitle(stringResource(R.string.Other)) }
        item {
            Card {
                AboutIconRow(
                    title = stringResource(R.string.SourceCode),
                    summary = stringResource(R.string.SourceCodeSummary),
                    icon = MiuixIcons.File,
                    onClick = { ActivityUtils.openUrl(context, REPO_URL) },
                )
                AboutIconRow(
                    title = stringResource(R.string.IssueTracker),
                    summary = stringResource(R.string.IssueTrackerSummary),
                    icon = MiuixIcons.Messages,
                    onClick = { ActivityUtils.openUrl(context, "$REPO_URL/issues") },
                )
                AboutIconRow(
                    title = stringResource(R.string.OpenSourceLicense),
                    summary = stringResource(R.string.OpenSourceLicenseSummary),
                    icon = MiuixIcons.Info,
                    onClick = { ActivityUtils.openUrl(context, "$REPO_URL/blob/main/LICENSE") },
                )
                AboutIconRow(
                    title = stringResource(R.string.Coolapk),
                    summary = "cn.fuckhome.xiaowine",
                    icon = MiuixIcons.Link,
                    onClick = {
                        ActivityUtils.openUrl(context, "https://www.coolapk.com/apk/cn.fuckhome.xiaowine")
                    },
                )
            }
        }
        item { AboutSectionTitle(stringResource(R.string.ThkListTips)) }
        item {
            Card {
                AboutIconRow(
                    title = stringResource(R.string.Xposed),
                    summary = "rovo89",
                    icon = MiuixIcons.Link,
                    onClick = { ActivityUtils.openUrl(context, "https://github.com/rovo89/Xposed") },
                )
                AboutIconRow(
                    title = stringResource(R.string.LSPosed),
                    summary = "LSPosed",
                    icon = MiuixIcons.Link,
                    onClick = { ActivityUtils.openUrl(context, "https://github.com/LSPosed/LSPosed") },
                )
                AboutIconRow(
                    title = stringResource(R.string.libXposed),
                    summary = "libxposed",
                    icon = MiuixIcons.Link,
                    onClick = { ActivityUtils.openUrl(context, "https://github.com/libxposed") },
                )
                AboutIconRow(
                    title = stringResource(R.string.blockmiui),
                    summary = "Block-Network",
                    icon = MiuixIcons.Link,
                    onClick = { ActivityUtils.openUrl(context, "https://github.com/Block-Network/blockmiui") },
                )
                AboutIconRow(
                    title = stringResource(R.string.EzXHelper),
                    summary = "KyuubiRan",
                    icon = MiuixIcons.Link,
                    onClick = { ActivityUtils.openUrl(context, "https://github.com/KyuubiRan/EzXHelper") },
                )
                AboutIconRow(
                    title = stringResource(R.string.miuix),
                    summary = "Apache-2.0",
                    icon = MiuixIcons.Link,
                    onClick = { ActivityUtils.openUrl(context, "https://github.com/compose-miuix-ui/miuix") },
                )
                AboutIconRow(
                    title = stringResource(R.string.XiaomiHelper),
                    summary = "HowieHChen",
                    icon = MiuixIcons.Link,
                    onClick = { ActivityUtils.openUrl(context, "https://github.com/HowieHChen/XiaomiHelper") },
                )
                AboutIconRow(
                    title = "KtSh",
                    summary = "jaredrummler",
                    icon = MiuixIcons.Link,
                    onClick = { ActivityUtils.openUrl(context, "https://github.com/jaredrummler/KtSh") },
                )
            }
        }
        item {
            Text(
                text = stringResource(R.string.AboutCopyright),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                style = MiuixTheme.textStyles.footnote1,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
        }
    }
}

@Composable
private fun AboutSectionTitle(text: String) {
    Text(
        text = text,
        modifier = Modifier.padding(start = 12.dp, top = 4.dp, bottom = 2.dp),
        style = MiuixTheme.textStyles.footnote1,
        color = MiuixTheme.colorScheme.onBackgroundVariant,
    )
}

@Composable
private fun AboutIconRow(
    title: String,
    summary: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    BasicComponent(
        title = title,
        summary = summary,
        startAction = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = MiuixTheme.colorScheme.primary,
            )
        },
        onClick = onClick,
    )
}

@Composable
private fun AvatarImage(resId: Int) {
    Image(
        painter = painterResource(resId),
        contentDescription = null,
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape),
        contentScale = ContentScale.Crop,
    )
}
