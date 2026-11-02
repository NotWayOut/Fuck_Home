package cn.fuckhome.xiaowine.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import cn.fuckhome.xiaowine.R
import cn.fuckhome.xiaowine.config.KEY_CLEAN_MODE
import cn.fuckhome.xiaowine.config.KEY_MEMORY_VIEW
import cn.fuckhome.xiaowine.config.KEY_RUNNING_APP_TOTAL
import cn.fuckhome.xiaowine.config.KEY_RUNNING_SERVICE_TOTAL
import cn.fuckhome.xiaowine.config.KEY_STORAGE_VIEW
import cn.fuckhome.xiaowine.config.KEY_UPTIME
import cn.fuckhome.xiaowine.config.KEY_WARNING
import cn.fuckhome.xiaowine.config.KEY_ZARM_VIEW
import top.yukonga.miuix.kmp.basic.Card

@Composable
fun AddInfoScreen(onBack: (() -> Unit)?) {
    SettingsScaffold(title = stringResource(R.string.AddInformation), onBack = onBack) {
        item {
            Card {
                SwitchRow(
                    key = KEY_MEMORY_VIEW,
                    title = stringResource(R.string.Memory),
                )
                SwitchRow(
                    key = KEY_ZARM_VIEW,
                    title = stringResource(R.string.Zarm),
                )
                SwitchRow(
                    key = KEY_STORAGE_VIEW,
                    title = stringResource(R.string.Storage),
                )
                SwitchRow(
                    key = KEY_UPTIME,
                    title = stringResource(R.string.Uptime),
                )
                SwitchRow(
                    key = KEY_RUNNING_APP_TOTAL,
                    title = stringResource(R.string.RunningAppTotal),
                )
                SwitchRow(
                    key = KEY_RUNNING_SERVICE_TOTAL,
                    title = stringResource(R.string.RunningServiceTotal),
                )
                SwitchRow(
                    key = KEY_WARNING,
                    title = stringResource(R.string.warning),
                )
                SwitchRow(
                    key = KEY_CLEAN_MODE,
                    title = stringResource(R.string.CleanMode),
                )
            }
        }
    }
}
