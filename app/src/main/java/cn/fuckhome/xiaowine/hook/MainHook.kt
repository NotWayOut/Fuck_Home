package cn.fuckhome.xiaowine.hook

import android.content.Context
import android.content.res.AssetManager
import android.content.res.Resources
import cn.fuckhome.xiaowine.R
import cn.fuckhome.xiaowine.config.KEY_MAIN_SWITCH
import cn.fuckhome.xiaowine.config.RemoteConfig
import cn.fuckhome.xiaowine.hook.module.add.Info
import cn.fuckhome.xiaowine.utils.LogUtils
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface.ModuleLoadedParam
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam

/**
 * libxposed API 模块入口。
 *
 * 由 META-INF/xposed/java_init.list 注册，框架在每个作用域进程中实例化一次。
 */
class MainHook : XposedModule() {

    companion object {
        val homeList = HomePackages.list

        lateinit var instance: MainHook
            private set

        /** 当前进程是否已加载模块（仅被 Hook 的进程为 true） */
        @Volatile
        var isModuleLoaded = false
            private set

        lateinit var appContext: Context
            private set

        lateinit var moduleRes: Resources
            private set

        @Volatile
        var targetClassLoader: ClassLoader = ClassLoader.getSystemClassLoader()
            private set
    }

    private var isHooked = false

    init {
        instance = this
        isModuleLoaded = true
    }

    override fun onModuleLoaded(param: ModuleLoadedParam) {
        LogUtils.i("onModuleLoaded: ${param.processName}")
    }

    override fun onPackageReady(param: PackageReadyParam) {
        targetClassLoader = param.classLoader
        if (param.packageName !in homeList) return
        LogUtils.i("onPackageReady: ${param.packageName}")
        if (!RemoteConfig.instance.getBoolean(KEY_MAIN_SWITCH)) {
            LogUtils.i("总开关未打开")
            return
        }
        hookApplicationAttach()
    }

    /**
     * Hook [android.app.Application.attach] 以获取桌面进程的 ApplicationContext，
     * 并在此之前完成模块资源与各功能 Hook 的初始化。
     */
    private fun hookApplicationAttach() {
        runCatching {
            val applicationClass = Class.forName("android.app.Application", false, targetClassLoader)
            val attachMethod = applicationClass.getDeclaredMethod("attach", Context::class.java)
            hook(attachMethod).intercept { chain ->
                if (!isHooked) {
                    isHooked = true
                    appContext = chain.args[0] as Context
                    moduleRes = createModuleResources()
                    initHooks()
                }
                chain.proceed()
                null
            }
        }.onFailure { LogUtils.i("hook Application.attach failed: $it") }
    }

    private fun createModuleResources(): Resources {
        val appInfo = moduleApplicationInfo
        return runCatching {
            appContext.packageManager.getResourcesForApplication(appInfo)
        }.getOrElse {
            val assetManager = AssetManager::class.java.newInstance()
            assetManager.javaClass.getMethod("addAssetPath", String::class.java)
                .invoke(assetManager, appInfo.sourceDir)
            @Suppress("DEPRECATION")
            Resources(assetManager, appContext.resources.displayMetrics, appContext.resources.configuration)
        }
    }

    private fun initHooks() {
        runCatching {
            Info.hook()
            LogUtils.i(moduleRes.getString(R.string.HookSuccess))
        }.onFailure { LogUtils.i(moduleRes.getString(R.string.HookFailed)) }
    }
}
