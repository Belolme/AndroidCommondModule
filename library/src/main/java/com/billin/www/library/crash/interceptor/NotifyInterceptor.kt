package com.billin.www.library.crash.interceptor

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.StrictMode
import android.text.TextUtils
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.billin.www.library.R
import com.billin.www.library.crash.core.CrashInterceptor
import java.io.File
import java.util.*


/**
 * 在通知栏显示 crash 通知
 *
 * Create by Billin on 2018/12/28
 */
class NotifyInterceptor(private val context: Context) : CrashInterceptor {

    private val crashNotifyId = context.resources.getInteger(R.integer.notification_crash)

    private val chanelId = context.getString(R.string.notification_crash_chanel)

    override fun interceptor(chain: CrashInterceptor.Chain): HashMap<String, String> {
        val data = chain.process()

        if (TextUtils.isEmpty(data[PrintFileInterceptor.PATH])) return data

        createNotificationChannel()

        val file = File(data[PrintFileInterceptor.PATH])

        StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder().build())
        val uri = Uri.fromFile(file)

        // 这个方法不行，因为崩溃的时候 provider 也已经崩溃了，所以其他应用没办法通过这个 provider 获取内容
//        val uri = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
//            Uri.fromFile(file)
//        } else {
//            FileProvider.getUriForFile(context, context.getString(R.string.file_provider), file)
//        }

        val intent = Intent(Intent.ACTION_VIEW)
        intent.addCategory(Intent.CATEGORY_DEFAULT)
        intent.setDataAndType(uri, "text/plain")
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val pendingIntent = PendingIntent.getActivity(
                context, 1, intent,
                PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, chanelId)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher_round))
                .setContentTitle("crash")
                .setContentText("click to open")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)

        NotificationManagerCompat.from(context).notify(crashNotifyId, builder.build())

        return data
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "crash"
            val descriptionText = "report crash"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(chanelId, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}