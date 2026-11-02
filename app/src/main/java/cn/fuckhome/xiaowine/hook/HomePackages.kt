package cn.fuckhome.xiaowine.hook

/**
 * 桌面进程包名。
 *
 * 独立于 [MainHook]（入口类继承 compileOnly 的 XposedModule，不能被打包进 APK 的应用进程引用），
 * 供模块设置界面向桌面进程广播字体操作等场景使用。
 */
object HomePackages {
    const val MIUI_HOME = "com.miui.home"
    const val POCO_HOME = "com.mi.android.globallauncher"
    val list = arrayOf(POCO_HOME, MIUI_HOME)
}
