package com.udacity.project4.utils

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.udacity.project4.R


object NotificationHelper {
    private const val CHANNEL_ID = "reminder"
    private const val CHANNEL_NAME = "reminder"
    private const val REQUEST_CODE = 100


    @SuppressLint("InLinedApi")
    fun getChannel(context: Context): ChanelDetails {
        return ChanelDetails(
            CHANNEL_ID,
            CHANNEL_NAME,
            context.getString(R.string.notification_channel_description),
            NotificationManager.IMPORTANCE_LOW,
            NotificationCompat.PRIORITY_HIGH,
            NotificationCompat.VISIBILITY_PUBLIC
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createNotificationChannel(context: Context, chanelDetails: ChanelDetails) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        NotificationChannel(chanelDetails.id, chanelDetails.name, chanelDetails.importance).apply {
            description = chanelDetails.description
            notificationManager.createNotificationChannel(this)
        }
    }

    fun sendNotification(context: Context, @StringRes messageResId: Int, @StringRes titleResId: Int, notificationId: Int, notifyIntent: Intent) {

        val mChannel = getChannel(context)
        val title = context.getString(titleResId)
        val message = context.getString(messageResId)

        val pendingIntent = PendingIntent.getActivity(
            context, REQUEST_CODE, notifyIntent, PendingIntent.FLAG_MUTABLE)
        val notification = NotificationCompat.Builder(context, mChannel.id)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setPriority(mChannel.priority)
            .setColor(ContextCompat.getColor(context, R.color.purple_500))
            .setSmallIcon(R.drawable.ic_add_location)
            .addAction(NotificationCompat.Action(null, "Check the status", pendingIntent)).build()

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notification)
    }

    fun clearNotification(context: Context, notificationId: Int) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(notificationId)
    }
}