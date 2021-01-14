package com.billin.com.playground

import android.app.Application
import android.os.Build
import android.util.Log
import com.billin.www.library.crash.core.CrashHandler
import com.billin.www.library.crash.interceptor.NotifyInterceptor
import com.billin.www.library.crash.interceptor.PrintFileInterceptor

private const val TAG = "Application"

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        initCrash()
    }

    private fun initCrash() {
        CrashHandler.getInstance().apply {
            init()
            addInterceptor { chain ->
                val data = chain.process()
                Log.d(TAG, "Opp...Something crashed!!")
                data
            }

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                addInterceptor(
                    PrintFileInterceptor(
                        this@MyApplication,
                        getExternalFilesDir("").toString()
                    )
                )
                addInterceptor(NotifyInterceptor(this@MyApplication))
            }

            addInterceptor(systemDefaultInterceptor)
        }
    }
}