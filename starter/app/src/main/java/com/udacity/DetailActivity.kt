package com.udacity

import android.app.DownloadManager
import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.content_detail.*

class DetailActivity : AppCompatActivity() {

    private var id = -1
    private var status = -1
    private lateinit var fileName: String

    //private val successString = getString(R.string.success)
    //private val failureString = getString(R.string.failure)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        setSupportActionBar(toolbar)

        loadBundle()

        fillContent()

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.cancel(id)

        ok_button.setOnClickListener {
            finish()
        }
    }

    private fun loadBundle() {
        intent.extras?.let {
            id = it.getInt(BUNDLE_DOWNLOAD_ID)
            status = it.getInt(BUNDLE_DOWNLOAD_STATUS)
            fileName = it.getString(BUNDLE_FILE_NAME) ?: ""
        }
    }

    private fun fillContent() {
        filename_text.text = fileName
        status_text.text = when (status) {
            DownloadManager.STATUS_SUCCESSFUL -> "Success"
            else -> "Failure"
        }
    }

    companion object {
        private const val BUNDLE_DOWNLOAD_ID = "download_id"
        private const val BUNDLE_DOWNLOAD_STATUS = "download_status"
        private const val BUNDLE_FILE_NAME = "file_name"

        fun getBundle(
            id: Int,
            status: Int,
            fileName: String
        ): Bundle {
            return Bundle().apply {
                putInt(BUNDLE_DOWNLOAD_ID, id)
                putInt(BUNDLE_DOWNLOAD_STATUS, status)
                putString(BUNDLE_FILE_NAME, fileName)
            }
        }
    }

}
