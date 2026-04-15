import android.content.Context
import android.text.TextUtils
import com.sendsay.sdk.Sendsay
import com.sendsay.sdk.util.Logger
import com.huawei.hms.aaid.HmsInstanceId
import com.huawei.hms.common.ApiException

class TokenTracker {
    companion object {
        const val LOG_TAG = "TokenTracker"
    }

    fun trackToken(context: Context?) {
        object : Thread() {
            override fun run() {
                try {
                    // Obtain the app ID from the agconnect-service.json file.
                    val appId = "114729999"

                    // Set tokenScope to HCM.
                    val tokenScope = "HCM"
                    val token = HmsInstanceId.getInstance(context).getToken(appId, tokenScope)

                    // Check whether the token is empty.
                    if (!TextUtils.isEmpty(token)) {
                        Sendsay.trackHmsPushToken(token)
                    }
                } catch (e: ApiException) {
                    Logger.e(this, "get hms token failed, $e")
                }
            }
        }.start()
    }

    fun getToken(context: Context?) {
        return HmsInstanceId.getInstance(context).getToken(appId, tokenScope)
    }

    fun testLocalPush(context: Context?) {
        if (lastToken == "wait and try again") return

        val testNotificationPayload = RemoteMessage.Builder(getToken(context))
            .addData("title", "RuStore Push Title")
            .addData("body", "testRsmLocalPush-Puck-Serenk")
            .addData("imgUrl", "https://static.rustore.ru/rustore-strapi/6/logo_color_30_px_2_fa2039288f.svg")
            .addData("some_key", "some_value")
            .build()

        FirebaseMessaging.getInstance().send(testNotificationPayload)
    }
}
