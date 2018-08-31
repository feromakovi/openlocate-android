package com.openlocate.android.core

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_NONE
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.support.v4.app.NotificationCompat
import com.openlocate.android.core.InfoManager.Companion.NOTIFICATION_ID

/**
 * Created by feromakovi <feromakovi@gmail.com> on 31/08/2018.
 */
class InfoManager(val context : Context) {

  private val locationsDB : LocationDatabase = LocationDatabase(DatabaseHelper.getInstance(context))
  private val storedLocations: Long = locationsDB.size()
  private val notificationManager : NotificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

  fun store(count: Int) {
    val allStored = locationsDB.size()
    val text = "Location in DB: $storedLocations - Newly inserted: $count - All stored locations: $allStored"
    notificationManager.show(context, text)
  }

  fun sync(count: Int) {
    val allStored = locationsDB.size()
    val text = "Location in DB: $storedLocations - Synced: $count - All stored locations: $allStored"
    notificationManager.show(context, text)
  }

  companion object {
    const val NOTIFICATION_ID = 123
  }
}

private fun NotificationManager.show(context: Context, text : String) {
  val channelId = "open-locate"
  val channel = NotificationChannel(channelId, "open locate service", IMPORTANCE_NONE)
  createNotificationChannel(channel)

  val notification = Notification.Builder(context, channelId)
      .setSmallIcon(android.R.drawable.ic_menu_mylocation)
      .setContentTitle("Locations")
      .setContentText(text)
      .setPriority(NotificationCompat.PRIORITY_DEFAULT)
      .build()


  notify(NOTIFICATION_ID, notification)
}
