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
import com.sendsay.example.BuildConfig
import com.sendsay.example.LogCollector
import com.sendsay.sdk.util.copyToClipboard
import com.sendsay.sdk.util.findActivity
import kotlin.time.Duration

class TokenTracker {
    companion object {
        const val LOG_TAG = "TokenTracker"

        // Obtain the app ID from the agconnect-service.json file.
        const val APP_ID = "117576633"

        // Set tokenScope to HMS.
        const val TOKEN_SCOPE = "HMS"

        @Volatile
        var lastToken = "wait and try again"

//        @Volatile
//        var localLogger = RollerList<String>(100)
    }

@RequiresApi(Build.VERSION_CODES.N)
fun checkPushAvailability(context: Context): Boolean {
        val isInstalled = try {
            context.packageManager.getPackageInfo("com.huawei.hwid", 0)
            true
        } catch (e: Exception) {
            val result = false
            showAlertDialogWithUrl(context)
            result
        }

        Logger.d(LOG_TAG, "HMS Core installed = $isInstalled")

        return isInstalled
    }

    fun getToken(context: Context?, onGetLastToken: (String) -> Unit) {
        context?.let {
//            it.findActivity()!!.runOnUiThread {
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

                        LogCollector.info(
                            BuildConfig.FLAVOR,
                            "getToken onSuccess token = $lastToken"
                        )
                    } catch (e: ApiException) {
                        Logger.e(this, "get hms token failed, $e")
                        LogCollector.error(
                            BuildConfig.FLAVOR,
                            e.stackTraceToString() ?: "Unknown Token error"
                        )
                    } catch (e: Exception) {
                        Logger.e(this, "Get hms token error, $e")
                        LogCollector.error(
                            BuildConfig.FLAVOR,
                            e.stackTraceToString() ?: "Unknown Token error"
                        )
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

    @RequiresApi(Build.VERSION_CODES.N)
    private fun showAlertDialogWithUrl(context: Context) {
//        val url = "https://consumer.huawei.com/ru/mobileservices/appgallery/"
        val url = "https://appgallery.huawei.com/#/app/C10132067"
        val linkText = "Скачать HMS Core"

        // Construct the HTML message string
        val message = "Ссылка на приложение:\n<a href=\"$url\">$linkText</a>"

        val builder = MaterialAlertDialogBuilder(context)
        builder.setTitle("Пожалуйста, установите Huawei HMS Core!\nЧтобы работали пуш-уведомления.")
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

class RollerList<T>(private val maxSize: Int) {
    private val list = mutableListOf<T>()

    fun add(element: T) {
        if (list.size >= maxSize) {
            list.removeAt(0) // Remove the oldest element
        }
        list.add(element)
    }

    fun toList(): List<T> = list
}