package com.billin.www.library.crash

import android.content.Context
import android.os.Build
import android.util.Log
import com.billin.www.library.crash.core.CrashInterceptor
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


/**
 * 打印崩溃日志到外部文件
 *
 * Create by Billin on 2018/12/28
 */
class PrintFileInterceptor(
        context: Context,
        private val dirString: String
) : CrashInterceptor {

    private val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)

    override fun interceptor(chain: CrashInterceptor.Chain): HashMap<String, String> {
        val map = chain.process()

        val longTime = System.currentTimeMillis()
        val date = SimpleDateFormat("yyyy-MM-dd HH-mm-ss", Locale.US).format(Date(longTime))
        val deviceManufacturer = Build.MANUFACTURER
        val deviceModel = Build.MODEL
        val androidVersion = Build.VERSION.RELEASE
        val androidSdk = Build.VERSION.SDK_INT
        val fileName = "crash_${chain.throwable.javaClass.name}_$date.txt"
        val file = File(dirString, fileName)
        val dir = file.parentFile

        val head = """
            **************** Log Head ****************
            $TIME: $date
            $MANUFACTURER: $deviceManufacturer
            $MODEL: $deviceModel
            $ANDROID_VERSION: $androidVersion
            $ANDROID_SDK: $androidSdk
            $PACKAGE_NAME: ${packageInfo.packageName}
            $VERSION_CODE: ${packageInfo.versionCode}
            $VERSION_NAME: ${packageInfo.versionName}
            $THREAD: ${chain.thread}
            **************** Log Head ****************
        """.trimIndent()

        map[TIME] = longTime.toString()
        map[MANUFACTURER] = deviceManufacturer
        map[MODEL] = deviceModel
        map[ANDROID_VERSION] = androidVersion
        map[ANDROID_SDK] = androidSdk.toString()
        map[VERSION_CODE] = packageInfo.versionCode.toString()
        map[VERSION_NAME] = packageInfo.versionName
        map[PACKAGE_NAME] = packageInfo.packageName

        if (!dir.exists() && !dir.mkdirs()) {
            // 无法创建目录，那就不打印了
            Log.e(TAG, "cannot create dir!")
            return map
        }
        if (!file.exists() && !file.createNewFile()) {
            Log.e(TAG, "cannot create file!")
            return map
        }

        map[PATH] = file.path

        val printWriter = file.printWriter()
        printWriter.println(head)
        chain.throwable.printStackTrace(printWriter)
        printWriter.flush()
        printWriter.close()

        return map
    }

    companion object {
        private const val TAG = "PrintFileInterceptor"

        const val PATH = "path"

        const val TIME = "time"

        const val THREAD = "thread"

        const val MANUFACTURER = "MANUFACTURER"

        const val MODEL = "model"

        const val ANDROID_VERSION = "android_version"

        const val PACKAGE_NAME = "package_name"

        const val ANDROID_SDK = "android_sdk"

        const val VERSION_CODE = "version_code"

        const val VERSION_NAME = "version_name"
    }
}
