package net.noahglaser

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import java.util.*


class NotifyReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.e("RECEIVED","RECEIVED")
        val app = context!!.applicationContext
        val sharedPreferences = app.getSharedPreferences("main", Context.MODE_PRIVATE)

        if (!sharedPreferences.getBoolean("should_schedule", true)) {
            Log.e("RECEIVED","NOT SHOWING NOTIFICATION")
            return
        }

        val notificationHoursString = sharedPreferences.getString("notification_hours", "");


        if (notificationHoursString == "" || notificationHoursString == null) {
            return
        }

        Log.e("RECEIVED","NOTIFY $notificationHoursString")

        val notificationHours = notificationHoursString.split(",").map { it.toInt() }

        val calendar = Calendar.getInstance()

        if (notificationHours.contains(calendar.get(Calendar.HOUR_OF_DAY))) {
            val builder = NotificationCompat.Builder(app)
                .setChannelId("cov-19")
                .setSmallIcon(R.raw.soap)
                .setLargeIcon(BitmapFactory.decodeResource(app.resources, R.raw.hw_rnd))
                .setContentTitle(sharedPreferences.getString("title", "Wash Your Hands!!! :)"))
                .setContentText(sharedPreferences.getString("body", "And clean your phone. :)"))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            val mNotificationManager =

                ContextCompat.getSystemService<NotificationManager>(
                    app,
                    NotificationManager::class.java
                )

            // notificationId is a unique int for each notification that you must define
            mNotificationManager?.notify(1, builder.build())
            Log.e("RECEIVED","NOTIFICATION SENT")
            return
        }

        Log.e("RECEIVED","NOTIFICATION NOT SENT")


    }


}