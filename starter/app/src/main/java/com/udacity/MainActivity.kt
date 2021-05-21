package com.udacity

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import timber.log.Timber


class MainActivity : AppCompatActivity() {

    private var downloadID: Long = 0
    private val notificationId = 0

    private lateinit var notificationManager: NotificationManager
    private lateinit var pendingIntent: PendingIntent
    private lateinit var action: NotificationCompat.Action

    private lateinit var downloadManager: DownloadManager

    override fun onCreate(savedInstanceState: Bundle?) {

        Timber.i(" called.")

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        Timber.plant(Timber.DebugTree())

        // register download receiver
        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        // custom button settings
        if (!custom_button.hasOnClickListeners()){
            custom_button.setOnClickListener {
                val url = when (radio_group.checkedRadioButtonId) {
                    radio_button_glide.id -> URL_GLIDE
                    radio_button_loadApp.id -> URL
                    radio_button_retrofit.id -> URL_RETROFIT
                    else -> {
                        Toast.makeText(applicationContext, getString(R.string.emptySelection), Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                }
                download(url)
                custom_button.onClicked()
            }
        }

        // download
        downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager

        // notifications
        notificationManager =
                getSystemService(NotificationManager::class.java)
        createChannel(CHANNEL_ID, getString(R.string.notification_title), getString(R.string.notification_description))
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }


    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1) ?: -1
            val cursor = downloadManager.query(DownloadManager.Query().setFilterById(id))
            if (cursor.moveToFirst()){
                val status = cursor.getInt(
                        cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                )
                val fileName = cursor.getString(
                        cursor.getColumnIndex(DownloadManager.COLUMN_TITLE)
                )
                sendNotification(id.toInt(), status, fileName)
            }
        }
    }

    private fun download(url: String) {

        Timber.i("Downloading $url")

        val request =
                DownloadManager.Request(Uri.parse(url))
                        .setDescription(getString(R.string.app_description))
                        .setRequiresCharging(false)
                        .setAllowedOverMetered(true)
                        .setAllowedOverRoaming(true)

        downloadID =
                downloadManager.enqueue(request)// enqueue puts the download request in the queue.
    }

    private fun sendNotification(id: Int, status: Int, fileName: String) {

        Timber.i("sending notification")

        val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notifyIntent = Intent(applicationContext, DetailActivity::class.java)
        notifyIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        notifyIntent.putExtras(DetailActivity.getBundle(id, status, fileName))

        pendingIntent = PendingIntent.getActivity(
                applicationContext,
                notificationId,
                notifyIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        )

        action = NotificationCompat.Action(R.drawable.ic_baseline_cloud_download_24, getString(R.string.showDetails), pendingIntent)

        val notification = NotificationCompat.Builder(applicationContext,
                CHANNEL_ID
        )
                .setSmallIcon(R.drawable.ic_baseline_cloud_download_24)
                .setContentTitle(getString(R.string.notification_title))
                .setContentText(getString(R.string.notification_description))
                .addAction(action)
                .setAutoCancel(true)
                .build()

        notificationManager.notify(id, notification)
    }

    private fun createChannel(channelId: String, channelName: String, channelDescription: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel =
                    NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
                            .apply {
                                setShowBadge(false)
                            }
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(true)
            notificationChannel.description = channelDescription

            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    companion object {
        private const val URL =
                "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"
        private const val URL_GLIDE = "https://github.com/bumptech/glide/archive/master.zip"
        private const val URL_RETROFIT = "https://github.com/square/retrofit/archive/master.zip"
        private const val CHANNEL_ID = "channelId"
    }

}
