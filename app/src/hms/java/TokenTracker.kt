import android.content.Context
import android.os.Build
import android.text.Html
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.sendsay.sdk.Sendsay
import com.sendsay.sdk.util.Logger
import com.huawei.hms.aaid.HmsInstanceId
import com.huawei.hms.common.ApiException
import com.huawei.hms.push.RemoteMessage
import com.sendsay.sdk.util.copyToClipboard
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

//    @RequiresApi(Build.VERSION_CODES.N)
    fun checkPushAvailability(context: Context): Boolean {
        val isInstalled = try {
            context.packageManager.getPackageInfo("ru.vk.store", 0)
            true
        } catch (e: Exception) {
            val result = false
            Logger.d(LOG_TAG, "RuStore installed = $result!")
            showAlertDialogWithUrl(context)
            result
        }

        Logger.d(LOG_TAG, "RuStore installed = $isInstalled")

//        RuStorePushClient.checkPushAvailability()
//            .addOnSuccessListener { result ->
//                Logger.i(LOG_TAG, "checkPushAvailability SUCCESS !")
//                when (result) {
//                    FeatureAvailabilityResult.Available -> {
//                        Logger.i(LOG_TAG, "checkPushAvailability -> ! AVAILABLE !")
//                    }
//
//                    is FeatureAvailabilityResult.Unavailable -> {
//                        Logger.i(LOG_TAG, "checkPushAvailability -> ...UNAVAILABLE...")
//                        result.cause.resolveForPush(context)
//                    }
//                }
//            }
//            .addOnFailureListener { throwable ->
//                showAlertDialogWithUrl(context)
//                Logger.e(LOG_TAG, "checkPushAvailability onFailure", throwable)
//            }

        return isInstalled
    }

    fun getToken(context: Context?, onGetLastToken: (String) -> Unit) {
        context?.let {
            object : Thread() {
                override fun run() {
                    try {
                        val token = HmsInstanceId.getInstance(context).getToken(APP_ID, TOKEN_SCOPE)

                        // Check whether the token is empty.
                        if (!TextUtils.isEmpty(token)) {
                            lastToken = token
                            onGetLastToken.invoke(lastToken)
                            context.copyToClipboard(lastToken)
                            Toast.makeText(
                                context,
                                "Скопировано в буфер обмена",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                            Logger.d(LOG_TAG, "getToken onSuccess token = $lastToken")
                        }
                    } catch (e: ApiException) {
                        Logger.e(this, "get hms token failed, $e")
                    } catch (e: Exception) {
                        Logger.e(this, "Get hms token error, $e")
                    }
                }
            }.start()
        }
    }

    fun trackToken(context: Context?) {
        getToken(context) { token ->
            Sendsay.trackHmsPushToken(token)
        }
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

    private fun showAlertDialogWithUrl(context: Context) {
        val url = "https://consumer.huawei.com/ru/mobileservices/appgallery/"
        val linkText = "Скачать AppGallery"

        // Construct the HTML message string
        val message = "Ссылка на приложение:\n<a href=\"$url\">$linkText</a>"

        val builder = MaterialAlertDialogBuilder(context)
        builder.setTitle("Пожалуйста, установите Huawei AppGallery!\nЧтобы работали пуш-уведомления.")
            .setMessage(Html.fromHtml(message, Html.FROM_HTML_MODE_COMPACT))
            .setCancelable(false)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }

        val alertDialog = builder.create()
        alertDialog.show()

        // IMPORTANT: Make the links clickable by setting the MovementMethod of the message TextView
        // This must be done AFTER the dialog is shown
        val msgTextView: TextView? = alertDialog.findViewById(android.R.id.message)
        msgTextView?.movementMethod = LinkMovementMethod.getInstance()
    }
}
