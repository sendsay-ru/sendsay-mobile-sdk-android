import android.content.Context
import android.text.TextUtils
import android.widget.Toast
import com.sendsay.sdk.Sendsay
import com.sendsay.sdk.util.Logger
import com.huawei.hms.aaid.HmsInstanceId
import com.huawei.hms.common.ApiException
import com.huawei.hms.push.RemoteMessage
import com.sendsay.sdk.util.copyToClipboard
import okhttp3.internal.wait
import kotlin.time.Duration

class TokenTracker {
    companion object {
        const val LOG_TAG = "TokenTracker"

        // Obtain the app ID from the agconnect-service.json file.
        const val APP_ID = "117576633"

        // Set tokenScope to HCM.
        const val TOKEN_SCOPE = "HCM"

        @Volatile
        var lastToken = "wait and try again"
    }

    fun trackToken(context: Context?) {
        object : Thread() {
            override fun run() {
                try {
                    val token = HmsInstanceId.getInstance(context).getToken(APP_ID, TOKEN_SCOPE)

                    // Check whether the token is empty.
                    if (!TextUtils.isEmpty(token)) {
                        lastToken = token
                        Sendsay.trackHmsPushToken(token)
                    }
                } catch (e: ApiException) {
                    Logger.e(this, "get hms token failed, $e")
                }
            }
        }.start()
    }

    fun getToken(context: Context?): String {
        object : Thread() {
            override fun run() {
                try {
                    val token = HmsInstanceId.getInstance(context).getToken(APP_ID, TOKEN_SCOPE)

                    // Check whether the token is empty.
                    if (!TextUtils.isEmpty(token)) {
                        lastToken = token
                    }
                } catch (e: ApiException) {
                    Logger.e(this, "get hms token failed, $e")
                }
            }
        }.start().also {
            context?.copyToClipboard(lastToken)
            Logger.d(LOG_TAG, "getToken onSuccess token = $lastToken")
        }
        return lastToken
    }

    fun testLocalPush(context: Context?) {
        Toast.makeText(context, "RSM only!", Toast.LENGTH_SHORT).show()
//        if (lastToken == "wait and try again") return

//        val testNotificationPayload = RemoteMessage.Builder(getToken(context))
//            .addData("title", "RuStore Push Title")
//            .addData("body", "testRsmLocalPush-Puck-Serenk")
//            .addData("imgUrl", "https://static.rustore.ru/rustore-strapi/6/logo_color_30_px_2_fa2039288f.svg")
//            .addData("some_key", "some_value")
//            .build()

//        HmsInstanceId.getInstance(context).sendNotif(testNotificationPayload)
    }
}
