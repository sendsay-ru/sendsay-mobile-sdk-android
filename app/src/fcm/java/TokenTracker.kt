import android.content.Context
import android.text.TextUtils
import android.widget.Toast
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

        @Volatile
        var lastToken = "wait and try again"
    }

    fun getToken(context: Context?, onGetTokenComplete: (String) -> Unit) {
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->

                // Check whether the token is empty.
                if (!TextUtils.isEmpty(token)) {
                    lastToken = token
                    context?.copyToClipboard(lastToken)
                    onGetTokenComplete.invoke(lastToken)
                    Logger.d(LOG_TAG, "getToken onSuccess token = $lastToken")
                }
            }.addOnFailureListener { throwable ->
                Toast.makeText(context, "Токен недоступен", Toast.LENGTH_SHORT)
                    .show()
                Logger.e(LOG_TAG, "getToken onFailure", throwable)
            }
    }

    fun trackToken(context: Context?) {
        getToken(context) { token ->
            Sendsay.trackPushToken(token)
        }
    }

    fun testLocalPush(context: Context?) {
        Toast.makeText(context, "RSM only!", Toast.LENGTH_SHORT).show()
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
