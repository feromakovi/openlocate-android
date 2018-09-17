package com.openlocate.android.core

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_NONE
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.support.v4.app.NotificationCompat
import com.openlocate.android.core.InfoManager.Companion.CHANNEL_ID
import com.openlocate.android.core.InfoManager.Companion.NOTIFICATION_ID

/**
 * Created by feromakovi <feromakovi@gmail.com> on 31/08/2018.
 */
class InfoManager @JvmOverloads constructor(private val context : Context, private val debug : Boolean = false) {

  private val locationsDB : LocationDatabase = LocationDatabase(DatabaseHelper.getInstance(context))
  private val storedLocations: Long = locationsDB.size()
  private val notificationManager : NotificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

  fun store(count: Int) = count.takeIf { debug }?.let { createInfo("Stored: $it") }

  fun sync(count: Int) = count.takeIf { debug }?.let { createInfo("Synced: $it") }

  private fun createInfo(action: String) {
    val allStored = locationsDB.size()
    val text = "DB items: $storedLocations - $action - All: $allStored"

    val notification = NotificationCompat.Builder(context, CHANNEL_ID)
      .setSmallIcon(android.R.drawable.ic_menu_mylocation)
      .setContentTitle("Locations")
      .setContentText(text)
      .setPriority(NotificationCompat.PRIORITY_DEFAULT)
      .build()

    notificationManager.show(notification)
  }

  companion object {
    internal const val NOTIFICATION_ID = 123
    internal const val CHANNEL_ID = "open-locate"
  }
}

private fun NotificationManager.show(notification: Notification) {
  if (VERSION.SDK_INT >= VERSION_CODES.O) {
    val channel = NotificationChannel(CHANNEL_ID, "open locate service", IMPORTANCE_NONE)
    createNotificationChannel(channel)
  }

  notify(NOTIFICATION_ID, notification)
}
