package cn.fuckhome.xiaowine.ui

import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class ThemeMode { LIGHT, DARK, SYSTEM }

enum class PaletteStyle(val label: String) {
    TonalSpot("TonalSpot"),
    Neutral("Neutral"),
    Vibrant("Vibrant"),
    Expressive("Expressive"),
    Rainbow("Rainbow"),
    Monochrome("Monochrome"),
}

enum class ColorSpec(val label: String) {
    SPEC_2021("2021"),
    SPEC_2025("2025"),
}

/** 预设主题色 */
val presetSeedColors = listOf(
    0xFF3482FF.toInt(), // 初音蓝
    0xFF00B578.toInt(), // 翠绿
    0xFFFF7D00.toInt(), // 橙
    0xFFF50057.toInt(), // 玫红
    0xFF7C4DFF.toInt(), // 紫
    0xFF00BCD4.toInt(), // 青
    0xFFFFC400.toInt(), // 金
    0xFF795548.toInt(), // 棕
)

/**
 * 主题设置状态：以 Compose 状态保存，写入模块配置（与被 Hook 进程无关，仅界面使用）。
 */
object ThemeState {

    private const val KEY_MODE = "ThemeMode"
    private const val KEY_MONET = "UseMiuixMonet"
    private const val KEY_DYNAMIC = "UseDynamicColor"
    private const val KEY_SEED = "SeedColor"
    private const val KEY_PALETTE = "PaletteStyle"
    private const val KEY_SPEC = "ColorSpec"

    private const val DEFAULT_SEED = 0xFF3482FF.toInt()

    lateinit var prefs: SharedPreferences
        private set

    var mode by mutableStateOf(ThemeMode.SYSTEM)
        private set
    var useMiuixMonet by mutableStateOf(false)
        private set
    var useDynamicColor by mutableStateOf(false)
        private set
    var seedColor by mutableIntStateOf(DEFAULT_SEED)
        private set
    var paletteStyle by mutableStateOf(PaletteStyle.TonalSpot)
        private set
    var colorSpec by mutableStateOf(ColorSpec.SPEC_2025)
        private set

    fun init(prefs: SharedPreferences) {
        this.prefs = prefs
        mode = ThemeMode.entries.find { it.name == prefs.getString(KEY_MODE, null) } ?: ThemeMode.SYSTEM
        useMiuixMonet = prefs.getBoolean(KEY_MONET, false)
        useDynamicColor = prefs.getBoolean(KEY_DYNAMIC, false)
        seedColor = prefs.getInt(KEY_SEED, DEFAULT_SEED)
        paletteStyle = PaletteStyle.entries.find { it.name == prefs.getString(KEY_PALETTE, null) }
            ?: PaletteStyle.TonalSpot
        colorSpec = ColorSpec.entries.find { it.name == prefs.getString(KEY_SPEC, null) }
            ?: ColorSpec.SPEC_2025
    }

    fun updateMode(value: ThemeMode) {
        mode = value
        prefs.edit().putString(KEY_MODE, value.name).apply()
    }

    fun updateUseMiuixMonet(value: Boolean) {
        useMiuixMonet = value
        prefs.edit().putBoolean(KEY_MONET, value).apply()
    }

    fun updateUseDynamicColor(value: Boolean) {
        useDynamicColor = value
        prefs.edit().putBoolean(KEY_DYNAMIC, value).apply()
    }

    fun updateSeedColor(value: Int) {
        seedColor = value
        prefs.edit().putInt(KEY_SEED, value).apply()
    }

    fun updatePaletteStyle(value: PaletteStyle) {
        paletteStyle = value
        prefs.edit().putString(KEY_PALETTE, value.name).apply()
    }

    fun updateColorSpec(value: ColorSpec) {
        colorSpec = value
        prefs.edit().putString(KEY_SPEC, value.name).apply()
    }
}
