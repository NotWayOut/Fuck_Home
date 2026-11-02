package cn.fuckhome.xiaowine.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.fuckhome.xiaowine.R
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun MainScreen(onNavigate: (SettingsScreen) -> Unit) {
    Scaffold(
        // 模块名置于左上角并放大（HyperEars / InstallerX 风格）
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.AppName),
                    style = MiuixTheme.textStyles.title1.copy(fontSize = 30.sp),
                )
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 12.dp,
                end = 12.dp,
                top = padding.calculateTopPadding() + 8.dp,
                bottom = 24.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // 主页顶部：框架状态卡片
            item {
                FrameworkStatusCard()
            }
            // 第一栏：模块设置
            item {
                Card {
                    ArrowPreference(
                        title = DOT_PREFIX + stringResource(R.string.ModuleSettings),
                        summary = stringResource(R.string.ModuleSettingsSummary),
                        onClick = { onNavigate(SettingsScreen.ModuleSettings) },
                    )
                }
            }
            // 主要功能菜单
            item {
                Card {
                    ArrowPreference(
                        title = DOT_PREFIX + stringResource(R.string.AddInformation),
                        onClick = { onNavigate(SettingsScreen.AddInfo) },
                    )
                    ArrowPreference(
                        title = DOT_PREFIX + stringResource(R.string.AddInformationStyle),
                        onClick = { onNavigate(SettingsScreen.Style) },
                    )
            }
        }
            // 关于
            item {
                Card {
                    ArrowPreference(
                        title = DOT_PREFIX + stringResource(R.string.About),
                        onClick = { onNavigate(SettingsScreen.About) },
                    )
                }
            }
        }
    }
}
