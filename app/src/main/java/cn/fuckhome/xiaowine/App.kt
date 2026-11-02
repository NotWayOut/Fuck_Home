package cn.fuckhome.xiaowine

import android.app.Application
import io.github.libxposed.service.XposedService
import io.github.libxposed.service.XposedServiceHelper
import kotlin.concurrent.Volatile

class App : Application(), XposedServiceHelper.OnServiceListener {

    companion object {
        @Volatile
        var mService: XposedService? = null
            private set
    }

    override fun onCreate() {
        super.onCreate()
        XposedServiceHelper.registerListener(this)
    }

    override fun onServiceBind(service: XposedService) {
        mService = service
    }

    override fun onServiceDied(service: XposedService) {
        mService = null
    }
}
