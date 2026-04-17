import android.content.Context
import com.sendsay.sdk.Sendsay
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.NotificationParams
import com.google.firebase.messaging.RemoteMessage
import com.sendsay.sdk.models.NotificationData
import com.sendsay.sdk.util.Logger
import com.sendsay.sdk.util.copyToClipboard

class TokenTracker {
    companion object {
        const val LOG_TAG = "TokenTracker"
    }

    var lastToken = "wait and try again"

    fun trackToken(context: Context?) {
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            Sendsay.trackPushToken(token)
            lastToken = token
        }
    }

    fun getToken(context: Context?): String {
        trackToken(context)
        context?.copyToClipboard(lastToken)
        Logger.d(LOG_TAG, "getToken onSuccess token = $lastToken")
        return lastToken
    }

    fun testLocalPush(context: Context?) {
//        if (lastToken == "wait and try again") return
//
//        val testNotificationPayload = RemoteMessage.Builder(getToken(context))
//            .addData("title", "RuStore Push Title")
//            .addData("body", "testRsmLocalPush-Puck-Serenk")
//            .addData("imgUrl", "https://static.rustore.ru/rustore-strapi/6/logo_color_30_px_2_fa2039288f.svg")
//            .addData("some_key", "some_value")
//            .build()
//
//        FirebaseMessaging.getInstance().send(testNotificationPayload)
    }
}
