package net.noahglaser

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.BitmapFactory
import androidx.core.content.ContextCompat.getSystemService
import android.os.Build
import android.util.Log
import android.view.View
import androidx.core.app.NotificationCompat
import kotlinx.android.synthetic.main.activity_main.*
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent




class MainActivity : AppCompatActivity() {

    var hours = 1
    val times = (1..24).toList().map {
        if (it <= 11) {
             "$it:00 AM"
        }
        else if (it == 12) {
            "12:00 PM"
        }

        else {
            "${it - 12}:00 PM"
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initApp()

    }

    fun onSchedule(view: View){

        val startHour = getHourFromString(start.selectedItem.toString())
        val endHour = getHourFromString(stop.selectedItem.toString())

        if (startHour > endHour) {
            Toast.makeText(applicationContext, "From time must be less than the To time..", Toast.LENGTH_LONG).show()
            return
        }

        val hoursInStringForm = (startHour..endHour step hours).map {  it.toString() }.toList().joinToString(",")

        val sharedPref = this.getSharedPreferences("main", Context.MODE_PRIVATE) ?: return
        with (sharedPref.edit()) {
            putString("start_time", start.selectedItem.toString())
            putString("stop_time", stop.selectedItem.toString())
            putString("title", titleText.text.toString())
            putString("body", bodyMessage.text.toString())
            putInt("hour_interval", hours)
            putString("notification_hours", hoursInStringForm.toString())

            putBoolean("should_schedule", notifyUser.isChecked)
            apply()
        }


        val alarmManager = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager


        if (!notifyUser.isChecked) {
            Toast.makeText(applicationContext, "Hand washing notification turned off.", Toast.LENGTH_LONG).show()
            return
        }


        val alarmAlreadySetup = PendingIntent.getBroadcast(
            applicationContext, 0,
            Intent(this, NotifyReceiver::class.java),
            PendingIntent.FLAG_NO_CREATE
        ) != null

        Log.d(MainActivity::class.java.simpleName, "ALARM IS SETUP $alarmAlreadySetup")

        if (!alarmAlreadySetup) {
            Log.d(MainActivity::class.java.simpleName, "ALARM BEING SET")
            val intent = Intent(this, NotifyReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + AlarmManager.INTERVAL_HOUR,
                AlarmManager.INTERVAL_HOUR, pendingIntent)

        }

        Toast.makeText(applicationContext, "Handing washing app ready to go!!!", Toast.LENGTH_LONG).show()


    }


    fun addHour(view: View) {
        if (hours < 24) {
            hours += 1
        }
        setHours()
    }

    fun minusHour(view: View) {
        if (hours > 1) {
            hours -= 1
        }
        setHours()
    }

    fun sendTestNotification(view: View) {
        val builder = NotificationCompat.Builder(applicationContext)
            .setChannelId("cov-19")
            .setSmallIcon(R.raw.soap)
            .setLargeIcon(BitmapFactory.decodeResource(application.resources, R.raw.hw_rnd))
            .setContentTitle(titleText.text)
            .setContentText(bodyMessage.text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        val contentIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT
        )
        builder.setContentIntent(contentIntent)
        val mNotificationManager =

            getSystemService<NotificationManager>(applicationContext, NotificationManager::class.java)



        // notificationId is a unique int for each notification that you must define
        mNotificationManager?.notify(1, builder.build())
        Log.d("NOTIFY", "got here")

    }

   private fun setHours() {
        if (hours == 1) {
            hoursTextView.text = "1 hour"
            return
        }

        hoursTextView.text = "$hours hours"
    }

    private fun getHourFromString(time: String): Int {

       val secondPart = if (time[1] == ':') "" else time[1].toString()

        val hour = (time[0] + secondPart).toInt()
        Log.e("RECEIVED","STRING HOUR $hour")

        if (hour == 12) {
            return 12
        }

        return hour + if (time.contains("PM")) 12 else 0
    }


    private fun initApp() {
        val startSpinner = findViewById<Spinner>(R.id.start)
        val stopSpinner = findViewById<Spinner>(R.id.stop)

        val startAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, times.toTypedArray())
        // Set layout to use when the list of choices appear
        startAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        // Set Adapter to Spinner
        startSpinner.adapter = startAdapter


        val stopAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, times.toTypedArray())
        // Set layout to use when the list of choices appear
        stopAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        // Set Adapter to Spinner
        stopSpinner.adapter = stopAdapter


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("cov-19", "Hand Washing Reminders", importance).apply {
                description = "this is for notify you about handing washing."
            }
            // Register the channel with the system
            val notificationManager =
                getSystemService<NotificationManager>(applicationContext, NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }

        val sharedPref = this.getSharedPreferences("main",Context.MODE_PRIVATE) ?: return
        val startTimeIndex = times.indexOf(sharedPref.getString("start_time", "9:00 AM"))
        val stopTimeIndex = times.indexOf(sharedPref.getString("stop_time", "9:00 PM"))
        hours = sharedPref.getInt("hour_interval", 1)
        notifyUser.isChecked = sharedPref.getBoolean("should_schedule", true)
        titleText.setText(sharedPref.getString("title", "Wash Your Hands!!! :)"))
        bodyMessage.setText(sharedPref.getString("body", "And clean your phone. :)"))
        startSpinner.setSelection(startTimeIndex)
        stopSpinner.setSelection(stopTimeIndex)
    }

}
