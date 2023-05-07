package edu.licenta.uptconnect.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import edu.licenta.uptconnect.R
import edu.licenta.uptconnect.view.activity.HomeActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.*
import java.net.URL
import javax.net.ssl.HttpsURLConnection


class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        var token: String? = null

        val key: String = "AAAAQuZ_Ba0:APA91bHXuMUUCZF9-lqtVVKf1sYTUQ9w2Oa7EsZko4tDZo21BqZscn9HyneM5QhbjaFSRtxLnpURNM0MWucStQ9Pq4dkg_IFSHjt3YFKmRgtn9flF9fU6WgK0ecq8cOBxbu5XsBjR-tz"

//        val key: String = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDMllLykD6S31IYByaF/Zwxw82Quivw1dxNuR8IG1OuP45eyS2HQ3FbEfzpT/J0WqSW9awYeNQHQaXeC3QPwGOElldVOdS+GeWtBokukalngELNKJGLzeZl7Hj1HWwLi8UfgTO4HiKDKkzmDQyIl/UMpedlM3mYaGGG9xb+2oWxKNpM/zFUOi0wWoq0SX1v/5TfL8cnMffV9htdSmxO9KIGHCbjfVW2cuGG9k6HiAwJG9p1deqQRpxSuiTcTgLpySFf2c4xpJv0v+1lFJ30AVld+1+rc9Lb75ImdUSKZU4NYR2v06E4ydwS8jUrVihllSolx4Oe83SWOiNrAMTs1TxdAgMBAAECggEAHqLsLPK4zBmxG3QGqs39k940bFXUuq3r3Z+5K+ebsl5GHZ+Wt8i7st2QuAeG0TG5r/l+GfQOHEASbJved3Vh8brpvRN57jXGlRCDfnvFmIA2gK5wjTT6KY0t19beFSSLElqwugOZO3ccKN8PX20R/yDg8+Cvdsk6wZFC2ksy9gJZi2BODf9xr0FsiXb2AjJoljhq2yHsLLuwwQkbnSi9bYwmjrmDZgiE9+qmSct7InMLqBeO+l4lGqyAZHZlOGWaPOSLzhkF1Sw+Uh3EP4TwwDg0VrWmASvlQaKkF68kvYq6zYY9074ayS7Ix6wMkWN/ZyRy6Ou/6v7P1cr6DsSgQQKBgQDsBaevzpjPBUGbGcmIJGeP9NBOBkPmE7hOvrACshYrMd9QIx7RzZrVhKKAQvE/yRdpCQnCIoXkKlSA5ct7QlXD6el72PCy7UhbUd2CNzLzJnUb7eQ2N6TFzEvqFhZ+EyRiCY56d2ghBZeU/d3+W17UHA+K5K4JkNlgPYhYhqGTpQKBgQDd54I/khy09irwrQiNz62mQxaq29YJclHl9JVjIv1iycMswe62AUMFr2LuBzDH0UFFXLAA8AuLopcfUhm6V9Ez+OsIxPyjOFlF2EKuQCPmQ8/Pbcr2j+VriUYokitTUxjiCk4+9n4zsEsUdBYhPN4zHonaBAXaR+2nJXeXtRDIWQKBgQDNP52zpVX9sfR7jNFVM1NcPOlo8MnbKwr8b5dgwL3GknhAmEoU6Vfclps6LvzdG2LgVJvH133YDXevICbz6Zvr417H9MBc2TwzLd4Tfy0vhiIRR69N9tWzrRXXW53zxXG+T9E3bt9+1/4Z8ys6JvswObbKclNmvjBLITeQxIN+gQKBgQDCVEjzRRDQxTfrl40DK9mF3gDO1kCF+1CPknb036KJTeoXmypix44bP3Hiw/dLNgz0ImycFZv2yAeSTULstpUl75pdDq20ftdnXDFhChimQcKOhDcXYALo/smrI/6/NOl4os/NQe5Zc8z8d4Ed0IFCwT6154n8k7sjAqPN/qpY2QKBgD+Rxgjqvs5jyraZ1yD6Ie5EiZjv5liG/j51YYDgrtkcDDqvWFWEMcTK+SINwbNPy+iz3O20caXU6ulyKVapTuyhO/CcCQdDHXNLmRo0Va/W/Y8fwyIi6sF4QS/C9F+oEoe365IzO6U+2KSlJdlcUK2HTkNw6wbrjLy0AEDdAX/E"

        fun subscribeTopic(context: Context, topic: String) {
            FirebaseMessaging.getInstance().subscribeToTopic(topic).addOnSuccessListener {
                Toast.makeText(context, "Subscribed $topic", Toast.LENGTH_LONG).show()
            }.addOnFailureListener {
                Toast.makeText(context, "Failed to Subscribe $topic", Toast.LENGTH_LONG).show()
            }
        }

        fun unsubscribeTopic(context: Context, topic: String) {
            FirebaseMessaging.getInstance().unsubscribeFromTopic(topic).addOnSuccessListener {
                Toast.makeText(context, "Unsubscribed $topic", Toast.LENGTH_LONG).show()
            }.addOnFailureListener {
                Toast.makeText(context, "Failed to Unsubscribe $topic", Toast.LENGTH_LONG).show()
            }
        }

        //this gives me segmentation fault
        fun sendNotification(context: Context, remoteMessage: RemoteMessage) {
            val intent = Intent(context, HomeActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT)
            val channelId = "1"
            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            val notificationBuilder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.user)
                .setContentTitle(remoteMessage.notification?.title)
                .setContentText(remoteMessage.notification?.body)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            // Since android Oreo notification channel is needed.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                notificationManager.createNotificationChannel(channel)
            }

            notificationManager.notify(0, notificationBuilder.build())
        }

        fun sendMessage(title: String, content: String, topic: String) {
            GlobalScope.launch { //need this cause a network request should not be done on the main thread
                val endpoint = "https://fcm.googleapis.com/fcm/send"
                try {
                    val url = URL(endpoint)
                    val httpsURLConnection: HttpsURLConnection =
                        url.openConnection() as HttpsURLConnection
                    httpsURLConnection.readTimeout = 10000
                    httpsURLConnection.connectTimeout = 15000
                    httpsURLConnection.requestMethod = "POST"
                    httpsURLConnection.doInput = true
                    httpsURLConnection.doOutput = true

                    // Adding the necessary headers
                    httpsURLConnection.setRequestProperty("authorization", "key=$key")
                    httpsURLConnection.setRequestProperty("Content-Type", "application/json")

                    // Creating the JSON with post params
                    val body = JSONObject()

                    val data = JSONObject()
                    data.put("title", title)
                    data.put("body", content)

                    body.put("to", "/topics/$topic")

                    val outputStream: OutputStream =
                        BufferedOutputStream(httpsURLConnection.outputStream)
                    val writer = BufferedWriter(OutputStreamWriter(outputStream, "utf-8"))
                    writer.write(body.toString())
                    writer.flush()
                    writer.close()
                    outputStream.close()
                    val responseCode: Int = httpsURLConnection.responseCode
                    val responseMessage: String = httpsURLConnection.responseMessage
                    Log.d("Response:", "$responseCode $responseMessage")
                    var result = String()
                    var inputStream: InputStream? = null
                    inputStream = if (responseCode in 400..499) {
                        httpsURLConnection.errorStream
                    } else {
                        httpsURLConnection.inputStream
                    }

                    if (responseCode == 200) {
                        println("SUCCESSSS")
                        Log.e("Success:", "notification sent $title \n $content")
                        // The details of the user can be obtained from the result variable in JSON format
                    } else {
                        Log.e("Error", "Error Response") //!!!!! ajunge aici de ce
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onMessageReceived(p0: RemoteMessage) {
        super.onMessageReceived(p0)
        Log.e("onMessageReceived: ", p0.data.toString())

        val title = p0.notification!!.title
        val content = p0.notification!!.body
        val defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP

        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            checkNotificationChannel("1")
        }

//        val person = Person.Builder().setName("test").build()
        val notification = NotificationCompat.Builder(applicationContext, "1")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(content)
//                .setStyle(NotificationCompat.MessagingStyle(person)
//                        .setGroupConversation(false)
//                        .addMessage(title,
//                                currentTimeMillis(), person)
//                )
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSound(defaultSound)

        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1, notification.build())

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun checkNotificationChannel(CHANNEL_ID: String) {
        val notificationChannel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.app_name),
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationChannel.description = "CHANNEL_DESCRIPTION"
        notificationChannel.enableLights(true)
        notificationChannel.enableVibration(true)
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(notificationChannel)
    }

    override fun onNewToken(p0: String) {
        token = p0
        super.onNewToken(p0)
    }

    override fun onMessageSent(messageId: String) {
        Log.d(TAG, "Message sent: $messageId")
    }

    override fun onSendError(message: String, exception: Exception) {
        Log.e(TAG, "Error sending message: $message", exception)
    }
}