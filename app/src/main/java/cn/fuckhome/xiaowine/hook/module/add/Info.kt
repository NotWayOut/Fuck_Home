package cn.fuckhome.xiaowine.hook.module.add

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import cn.fuckhome.xiaowine.BuildConfig
import cn.fuckhome.xiaowine.R
import cn.fuckhome.xiaowine.config.KEY_BG_COLOR
import cn.fuckhome.xiaowine.config.KEY_BG_CORNERS
import cn.fuckhome.xiaowine.config.KEY_CLEAN_MODE
import cn.fuckhome.xiaowine.config.KEY_GRAVITY
import cn.fuckhome.xiaowine.config.KEY_MARGIN_LEFT_LANDSCAPE
import cn.fuckhome.xiaowine.config.KEY_MARGIN_LEFT_PORTRAIT
import cn.fuckhome.xiaowine.config.KEY_MARGIN_TOP_LANDSCAPE
import cn.fuckhome.xiaowine.config.KEY_MARGIN_TOP_PORTRAIT
import cn.fuckhome.xiaowine.config.KEY_MEMORY_VIEW
import cn.fuckhome.xiaowine.config.KEY_OPTIMIZE_ANIMATION
import cn.fuckhome.xiaowine.config.KEY_RUNNING_APP_TOTAL
import cn.fuckhome.xiaowine.config.KEY_RUNNING_SERVICE_TOTAL
import cn.fuckhome.xiaowine.config.KEY_STORAGE_VIEW
import cn.fuckhome.xiaowine.config.KEY_UPTIME
import cn.fuckhome.xiaowine.config.KEY_UNIT
import cn.fuckhome.xiaowine.config.KEY_WARNING
import cn.fuckhome.xiaowine.config.KEY_ZARM_VIEW
import cn.fuckhome.xiaowine.config.xConfig
import cn.fuckhome.xiaowine.hook.MainHook
import cn.fuckhome.xiaowine.utils.FileUtils
import cn.fuckhome.xiaowine.utils.LogUtils
import cn.fuckhome.xiaowine.utils.MemoryUtils
import cn.fuckhome.xiaowine.utils.Utils.Uptime
import cn.fuckhome.xiaowine.utils.Utils.catchNoClass
import cn.fuckhome.xiaowine.utils.Utils.formatSize
import cn.fuckhome.xiaowine.utils.findClass
import cn.fuckhome.xiaowine.utils.findConstructor
import cn.fuckhome.xiaowine.utils.findMethod
import cn.fuckhome.xiaowine.utils.getObjectFieldAs
import cn.fuckhome.xiaowine.utils.setObjectFieldIfExist
import java.io.File
import java.util.Timer
import java.util.TimerTask
import kotlin.math.roundToInt

/**
 * 在最近任务界面添加真实的内存 / zRAM / 存储 / 开机时间 / 运行统计信息。
 */
object Info {

    private const val RECENTS_CONTAINER = "com.miui.home.recents.views.RecentsContainer"

    /** 桌面进程广播：来自模块设置界面的字体操作 */
    private const val ACTION_HOME_SERVER = "MIUIHOME_Server"

    /** 模块进程广播：桌面进程回传字体操作结果 */
    private const val ACTION_APP_SERVER = "MIUIHOME_App_Server"

    /** 剩余内存低于该百分比时以红色警示 */
    private const val WARNING_THRESHOLD = 20

    private val infoKeys = listOf(
        KEY_MEMORY_VIEW, KEY_ZARM_VIEW, KEY_STORAGE_VIEW, KEY_UPTIME,
        KEY_RUNNING_APP_TOTAL, KEY_RUNNING_SERVICE_TOTAL
    )

    private lateinit var defaultTextColors: ColorStateList
    private lateinit var infoLayout: LinearLayout
    private val textViews = LinkedHashMap<String, TextView>()
    private val enabledKeys = ArrayList<String>()

    private val handler = Handler(Looper.getMainLooper())
    private var timer: Timer? = null

    private val appContext: Context get() = MainHook.appContext
    private val moduleRes get() = MainHook.moduleRes
    private val config get() = xConfig

    private val displayMetrics by lazy { appContext.resources.displayMetrics }
    private val widthPixels by lazy { displayMetrics.widthPixels }
    private val heightPixels by lazy { displayMetrics.heightPixels }
    private var topMargin = 0
    private var leftMargin = 0

    fun hook() {
        hookRootView()
        hookInflateFinished()
        hookRotationChanged()
        hookFadeIn()
        hookFadeOut()
        registerFontReceiver()
    }

    /* -------------------------------------- Hook -------------------------------------- */

    private fun hookRootView() {
        catchNoClass {
            val container = findClass(RECENTS_CONTAINER)
            MainHook.instance.hook(container.findConstructor { parameterCount == 2 }).intercept { chain ->
                chain.proceed()
                LogUtils.i(moduleRes.getString(R.string.InitRootView))
                val layout = LinearLayout(appContext).apply {
                    orientation = LinearLayout.VERTICAL
                    if (config.getBoolean(KEY_OPTIMIZE_ANIMATION)) visibility = View.GONE
                }
                layout.background = GradientDrawable().apply {
                    setColor(Color.parseColor(config.backgroundColor))
                    cornerRadius = config.backgroundCorners.toFloat()
                    setStroke(layout.width, Color.BLACK)
                }
                infoLayout = layout
                (chain.thisObject as FrameLayout).addView(layout)
                enabledKeys.clear()
                infoKeys.forEach { key -> if (config.getBoolean(key)) enabledKeys.add(key) }
                null
            }
        }
    }

    private fun hookInflateFinished() {
        catchNoClass {
            val container = findClass(RECENTS_CONTAINER)
            MainHook.instance.hook(container.findMethod { name == "onFinishInflate" }).intercept { chain ->
                chain.proceed()
                LogUtils.i(moduleRes.getString(R.string.InitAddView))
                val recentsContainer = chain.thisObject
                val memoryGroup = recentsContainer.getObjectFieldAs<ViewGroup>("mTxtMemoryContainer")
                recentsContainer.setObjectFieldIfExist("mSeparatorForMemoryInfo", View(appContext))
                for (i in 0 until memoryGroup.childCount) {
                    memoryGroup.getChildAt(i).visibility = View.GONE
                }
                defaultTextColors =
                    recentsContainer.getObjectFieldAs<TextView>("mTxtMemoryInfo1").textColors
                textViews.clear()
                enabledKeys.forEach { key ->
                    val view = TextView(appContext).apply {
                        applyTextColor(this, null)
                        gravity = config.gravity
                        textSize = 12f
                        marqueeRepeatLimit = -1
                        isSingleLine = true
                        maxLines = 1
                    }
                    infoLayout.addView(view)
                    textViews[key] = view
                }
                null
            }
        }
    }

    private fun hookRotationChanged() {
        catchNoClass {
            val container = findClass(RECENTS_CONTAINER)
            MainHook.instance.hook(container.findMethod { name == "updateRotation" }).intercept { chain ->
                chain.proceed()
                if (chain.args[0] as Int == 0) {
                    topMargin = if (config.useScaleUnit) {
                        (10 + config.getInt(KEY_MARGIN_TOP_PORTRAIT, 4) / 100.0 * heightPixels).toInt()
                    } else {
                        10 + config.getInt(KEY_MARGIN_TOP_PORTRAIT, 4)
                    }
                    leftMargin = if (config.useScaleUnit) {
                        (10 + config.getInt(KEY_MARGIN_LEFT_PORTRAIT) / 100.0 * widthPixels).roundToInt()
                    } else {
                        10 + config.getInt(KEY_MARGIN_LEFT_PORTRAIT)
                    }
                } else {
                    topMargin = if (config.useScaleUnit) {
                        (10 + config.getInt(KEY_MARGIN_TOP_LANDSCAPE, 5) / 100.0 * widthPixels).roundToInt()
                    } else {
                        10 + config.getInt(KEY_MARGIN_TOP_LANDSCAPE, 5)
                    }
                    leftMargin = if (config.useScaleUnit) {
                        (10 + config.getInt(KEY_MARGIN_LEFT_LANDSCAPE) / 100.0 * heightPixels).roundToInt()
                    } else {
                        10 + config.getInt(KEY_MARGIN_LEFT_LANDSCAPE)
                    }
                }
                null
            }
        }
    }

    private fun hookFadeIn() {
        catchNoClass {
            val container = findClass(RECENTS_CONTAINER)
            MainHook.instance.hook(container.findMethod { name == "startRecentsContainerFadeInAnim" })
                .intercept { chain ->
                    chain.proceed()
                    LogUtils.i(moduleRes.getString(R.string.VisibleView))
                    val params = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT
                    )
                    params.topMargin = topMargin
                    params.leftMargin = leftMargin
                    infoLayout.layoutParams = params
                    LogUtils.i(moduleRes.getString(R.string.UpdateView))
                    infoLayout.startAnimation(AlphaAnimation(0f, 1f).apply {
                        duration = 300
                        setAnimationListener(object : Animation.AnimationListener {
                            override fun onAnimationStart(animation: Animation?) {}
                            override fun onAnimationRepeat(animation: Animation?) {}
                            override fun onAnimationEnd(animation: Animation?) {
                                infoLayout.clearAnimation()
                            }
                        })
                    })
                    infoLayout.visibility = View.VISIBLE
                    startTimer()
                    null
                }
        }
    }

    private fun hookFadeOut() {
        catchNoClass {
            val container = findClass(RECENTS_CONTAINER)
            MainHook.instance.hook(container.findMethod { name == "startRecentsContainerFadeOutAnim" })
                .intercept { chain ->
                    chain.proceed()
                    LogUtils.i(moduleRes.getString(R.string.GoneView))
                    stopTimer()
                    if (infoLayout.visibility != View.GONE) {
                        infoLayout.startAnimation(AlphaAnimation(1f, 0f).apply {
                            duration = 300
                            setAnimationListener(object : Animation.AnimationListener {
                                override fun onAnimationStart(animation: Animation?) {}
                                override fun onAnimationRepeat(animation: Animation?) {}
                                override fun onAnimationEnd(animation: Animation?) {
                                    infoLayout.clearAnimation()
                                    infoLayout.visibility = View.GONE
                                }
                            })
                        })
                    }
                    null
                }
        }
    }

    /* ------------------------------------ 数据刷新 ------------------------------------ */

    private fun startTimer() {
        stopTimer()
        timer = Timer().apply {
            schedule(object : TimerTask() {
                override fun run() {
                    handler.post { updateInfo() }
                }
            }, 0, 1000L)
        }
    }

    private fun stopTimer() {
        timer?.cancel()
        timer = null
    }

    private fun updateInfo() {
        val memoryInfo = MemoryUtils().getMemoryInfo(appContext)
        val swapInfo = MemoryUtils().getPartitionInfo("SwapTotal", "SwapFree")
        val storageInfo = MemoryUtils().getStorageInfo(Environment.getExternalStorageDirectory())
        val activityManager = appContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val cleanMode = config.getBoolean(KEY_CLEAN_MODE)
        textViews.forEach { (key, view) ->
            view.text = when (key) {
                KEY_MEMORY_VIEW -> moduleRes.getString(
                    if (cleanMode) R.string.MemoryView2 else R.string.MemoryView
                ).format(
                    memoryInfo.availMem.formatSize(),
                    memoryInfo.totalMem.formatSize(),
                    memoryInfo.percentValue
                )

                KEY_ZARM_VIEW -> moduleRes.getString(
                    if (cleanMode) R.string.ZarmView2 else R.string.ZarmView
                ).format(
                    swapInfo.availMem.formatSize(),
                    swapInfo.totalMem.formatSize(),
                    swapInfo.percentValue
                )

                KEY_STORAGE_VIEW -> moduleRes.getString(
                    if (cleanMode) R.string.StorageView2 else R.string.StorageView
                ).format(
                    storageInfo.availMem.formatSize(),
                    storageInfo.totalMem.formatSize(),
                    storageInfo.percentValue
                )

                KEY_UPTIME -> moduleRes.getString(R.string.BootTimeView).format(Uptime.get())

                KEY_RUNNING_APP_TOTAL -> moduleRes.getString(R.string.RunningAppTotalView)
                    .format(activityManager.runningAppProcesses.size)

                KEY_RUNNING_SERVICE_TOTAL -> moduleRes.getString(R.string.RunningServiceTotalView)
                    .format(activityManager.getRunningServices(999).size)

                else -> null
            }
            applyTextColor(view, when (key) {
                KEY_MEMORY_VIEW -> memoryInfo
                KEY_ZARM_VIEW -> swapInfo
                KEY_STORAGE_VIEW -> storageInfo
                else -> null
            })
            view.width = view.paint.measureText(view.text.toString()).toInt() + 40
        }
    }

    private fun applyTextColor(view: TextView, info: MemoryUtils?) {
        if (info != null && config.getBoolean(KEY_WARNING) && info.percentValue < WARNING_THRESHOLD) {
            view.setTextColor(Color.RED)
        } else if (config.color.isEmpty()) {
            view.setTextColor(defaultTextColors)
        } else {
            view.setTextColor(Color.parseColor(config.color))
        }
    }

    /* ---------------------------------- 自定义字体广播 ---------------------------------- */

    private fun registerFontReceiver() {
        catchNoClass {
            ContextCompat.registerReceiver(
                appContext,
                fontReceiver,
                IntentFilter().apply { addAction(ACTION_HOME_SERVER) },
                ContextCompat.RECEIVER_EXPORTED
            )
        }
    }

    private val fontReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            catchNoClass {
                when (intent.getStringExtra("Type")) {
                    "copy_font" -> copyFont(intent.getStringExtra("Font_Path"))
                    "delete_font" -> deleteFont()
                }
            }
        }

        private fun copyFont(path: String?) {
            LogUtils.i(moduleRes.getString(R.string.CustomFont))
            if (path.isNullOrEmpty()) return
            val fontFile = File(appContext.filesDir.path + "/font")
            if (fontFile.exists() && fontFile.canWrite()) fontFile.delete()
            val error = FileUtils(appContext).copyFile(File(path), appContext.filesDir.path, "font")
            if (error.isEmpty()) {
                textViews.values.forEach { view ->
                    view.typeface = Typeface.createFromFile(appContext.filesDir.path + "/font")
                }
                LogUtils.i(moduleRes.getString(R.string.CustomFontSuccess))
                replyToApp("CopyFont", true)
            } else {
                runCatching {
                    val file = File(appContext.filesDir.path + "/font")
                    if (file.exists() && file.canWrite()) {
                        file.delete()
                        LogUtils.i(moduleRes.getString(R.string.CustomFontFail))
                    }
                }
                replyToApp("CopyFont", false, "font_error" to error)
            }
        }

        private fun deleteFont() {
            LogUtils.i(moduleRes.getString(R.string.DeleteFont))
            var success = false
            val fontFile = File(appContext.filesDir.path + "/font")
            if (fontFile.exists() && fontFile.canWrite()) success = fontFile.delete()
            textViews.values.forEach { view -> view.typeface = Typeface.createFromFile("") }
            replyToApp("DeleteFont", success)
        }

        private fun replyToApp(type: String, success: Boolean, extra: Pair<String, String>? = null) {
            val intent = Intent(ACTION_APP_SERVER).apply {
                putExtra("app_Type", type)
                putExtra(type, success)
                extra?.let { putExtra(it.first, it.second) }
                setPackage(BuildConfig.APPLICATION_ID)
            }
            appContext.sendBroadcast(intent)
        }
    }
}
