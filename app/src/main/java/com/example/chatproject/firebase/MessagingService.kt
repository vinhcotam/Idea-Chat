package com.example.chatproject.firebase

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.chatproject.R
import com.example.chatproject.activities.ChatActivity
import com.example.chatproject.models.User
import com.example.chatproject.utilities.Constants.Companion.KEY_FCM_TOKEN
import com.example.chatproject.utilities.Constants.Companion.KEY_MESSAGE
import com.example.chatproject.utilities.Constants.Companion.KEY_NAME
import com.example.chatproject.utilities.Constants.Companion.KEY_USER
import com.example.chatproject.utilities.Constants.Companion.KEY_USER_ID
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.*

class MessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val user = User()
        user.id = message.data[KEY_USER_ID].toString()
        user.name = message.data[KEY_NAME].toString()
        user.token = message.data[KEY_FCM_TOKEN].toString()
        val notificationId = Random().nextInt()
        val channelId = "chat_message"

        val intent = Intent(this, ChatActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        intent.putExtra(KEY_USER, user)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)

        val builder = NotificationCompat.Builder(this, channelId)
        builder.setSmallIcon(R.drawable.ic_notifications)
        builder.setContentTitle(user.name)
        builder.setContentText(message.data[KEY_MESSAGE])
        builder.setStyle(NotificationCompat.BigTextStyle().bigText(message.data[KEY_MESSAGE]))
        builder.priority = NotificationCompat.PRIORITY_DEFAULT
        builder.setContentIntent(pendingIntent)
        builder.setAutoCancel(true)

        val channelName: CharSequence = "Chat Message"
        val channelDescription = "This notification channel used for chat message notifications"
        val importance: Int = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(channelId, channelName, importance)
        channel.description = channelDescription
        val notificationManager: NotificationManager =
            getSystemService(NotificationManager::class.java) as NotificationManager
        notificationManager.createNotificationChannel(channel)
        val notificationManagerCompat = NotificationManagerCompat.from(this)
        notificationManagerCompat.notify(notificationId,builder.build())
    }
}
