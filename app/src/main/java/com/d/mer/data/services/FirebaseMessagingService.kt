package com.d.mer.data.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.d.mer.R
import com.d.mer.data.firestore.FireStoreReferences
import com.d.mer.ui.activities.MainActivity
import com.d.mer.ui.common.Constants
import com.d.mer.ui.common.Logger
import com.d.mer.ui.common.PreferenceManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FirebaseMessagingService : FirebaseMessagingService() {

    private val tag = "FirebaseService"

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        Logger.info("Refreshed token: $token", tag)

        FireStoreReferences.uploadUserToken(token)

    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        try {
            Logger.info("onMessageReceived: ${remoteMessage.data}", tag)
            val type = remoteMessage.data["type"] ?: ""
            val title = remoteMessage.data["title"] ?: ""
            val message = remoteMessage.data["message"] ?: ""
            val time = remoteMessage.data["time"] ?: ""

            val reqCode = PreferenceManager.getInstance(this).getReqCodeForNotification()

            when (type.trim()) {

                Constants.TO_ALL -> {
                    notificationDialog(title, message, this, reqCode, time)
                }

                Constants.TO_WINNER -> {

                }

            }

        } catch (e: Exception) {
            Logger.info("onMessageReceived: ${e.localizedMessage} -- $e", tag)
        }
    }

    private fun notificationDialog(
        title: String,
        text: String,
        context: Context,
        reqCode: Int,
        time: String
    ) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        } else ""

        val intent = Intent(context, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        val pendingIntent =
            PendingIntent.getActivity(context, reqCode, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val notificationBuilder = NotificationCompat.Builder(context, channelId)

        notificationBuilder.setAutoCancel(true)
            .setDefaults(Notification.DEFAULT_ALL)
            .setWhen(getProperTime(time))
            .setSmallIcon(R.drawable.ic_notification_icon)
            .setTicker(context.resources.getString(R.string.app_name))
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(pendingIntent)
        notificationManager.notify(reqCode, notificationBuilder.build())

        Logger.info("inside notification method - $reqCode")
    }

    private fun getProperTime(time: String): Long {
        return try {
            time.trim().toLong()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(): String {
        val channelId = "DMer_App_Notifications"
        val channelName = "D Mer App Notifications"
        val chan = NotificationChannel(
            channelId,
            channelName, NotificationManager.IMPORTANCE_DEFAULT
        )
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return channelId
    }
}

