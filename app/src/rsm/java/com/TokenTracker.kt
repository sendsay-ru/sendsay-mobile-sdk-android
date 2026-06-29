import android.content.Context
import android.os.Build
import android.text.Html
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.sendsay.example.services.RuStorePushClientParams
import com.sendsay.sdk.Sendsay
import com.sendsay.sdk.util.Logger
import com.sendsay.sdk.util.copyToClipboard
import ru.rustore.sdk.core.exception.RuStoreException
import ru.rustore.sdk.core.feature.model.FeatureAvailabilityResult
import ru.rustore.sdk.pushclient.RuStorePushClient
import ru.rustore.sdk.pushclient.RuStorePushClient.checkPushAvailability
import ru.rustore.sdk.pushclient.messaging.model.TestNotificationPayload
import ru.rustore.sdk.pushclient.utils.resolveForPush

class TokenTracker {
    companion object {
        const val LOG_TAG = "TokenTracker"

        @Volatile
        var lastToken = "wait and try again"
    }


    @RequiresApi(Build.VERSION_CODES.N)
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

        RuStorePushClient.checkPushAvailability()
            .addOnSuccessListener { result ->
                Logger.i(LOG_TAG, "checkPushAvailability SUCCESS !")
                when (result) {
                    FeatureAvailabilityResult.Available -> {
                        Logger.i(LOG_TAG, "checkPushAvailability -> ! AVAILABLE !")
                    }

                    is FeatureAvailabilityResult.Unavailable -> {
                        Logger.i(LOG_TAG, "checkPushAvailability -> ...UNAVAILABLE...")
                        result.cause.resolveForPush(context)
                    }
                }
            }
            .addOnFailureListener { throwable ->
                showAlertDialogWithUrl(context)
                Logger.e(LOG_TAG, "checkPushAvailability onFailure", throwable)
            }

        return isInstalled
    }

    fun getToken(context: Context?, onGetLastToken: (String) -> Unit) {
        context?.let {
            // Проверяем установлен ли RuStore или другой VkCore на устройстве:
            if (checkPushAvailability(it)) object : Thread() {
                override fun run() {
                    try {
                        RuStorePushClient.getToken()
                            .addOnSuccessListener { token ->
                                // Check the token is empty.
                                if (!TextUtils.isEmpty(token)) {
                                    onGetLastToken.invoke(token)
                                    context.copyToClipboard(token)
                                    Toast.makeText(
                                        context,
                                        "Скопировано в буфер обмена",
                                        Toast.LENGTH_SHORT
                                    )
                                        .show()
                                }
                                Logger.d(LOG_TAG, "getToken onSuccess token = $token")
                            }
                            .addOnFailureListener { throwable ->
                                Toast.makeText(context, "Токен недоступен", Toast.LENGTH_SHORT)
                                    .show()
                                Logger.e(LOG_TAG, "getToken onFailure", throwable)
                            }
                    } catch (e: RuStoreException) {
                        Logger.e(this, "get rustore push token failed, $e")
                    }
                }
            }.start()
        }
    }

    fun trackToken(context: Context?) {
        getToken(context) { token ->
            Sendsay.trackRsmPushToken(token)
        }
    }


    fun testLocalPush(context: Context?) {
        context?.let {
            if (!checkPushAvailability(it)) {
                Toast.makeText(it, "Пуш недоступен, подробнее в логах", Toast.LENGTH_SHORT).show()
                return
            }

            if (!RuStorePushClientParams(context).getTestModeEnabled()) {
                Toast.makeText(it, "Требуется getTestModeEnabled = true", Toast.LENGTH_SHORT).show()
                return
            }

            val testNotificationPayload = TestNotificationPayload(
                title = "Заголовок для 44 в среду",
                body = "Тело сообщения для 44 в среду 24.06.2026 12.56.04",
                imgUrl = "https://static.rustore.ru/rustore-strapi/6/logo_color_30_px_2_fa2039288f.svg",
                data = mapOf(
                    "sendsay_read_url" to "https://read.sdc.test.sndsy.ru/0.gif/3212,,,44,,/20260624125624,",
                    "message" to "Сообщение для 44 в среду 24.06.2026 12.56.04",
                    "sendsay_click_url" to "https://pushapp",
                    "url" to "https://pushapp",
                    "url_params" to """{"xnpe_force_track":true}""",
                    "notification_id" to "1782294984",
                    "data" to """{"sendsay_seq_pass":"","sendsay_letter_id":"","sendsay_member_id":"44","sendsay_issue_id":"","sendsay_seq_id":"","sendsay_letter_luuid":"J26SN7Z1S5usNG7_IYn2vg"}""",
                    "source" to "xnpe_platform",
                    "consent_category_tracking" to "Statistics collection",
                    "has_tracking_consent" to "1"
                )
            )

            RuStorePushClient.sendTestNotification(testNotificationPayload)
                .addOnCompletionListener {
                    Logger.d(LOG_TAG, "Test Local Push Completed")
                }.addOnSuccessListener {
                    Toast.makeText(context, "Пуш отправлен!", Toast.LENGTH_SHORT).show()
                    Logger.d(LOG_TAG, "Test Local Push Sended")
                }.addOnFailureListener { throwable ->
                    Toast.makeText(context, "Пуш сломался =(", Toast.LENGTH_SHORT).show()
                    Logger.e(LOG_TAG, "Test Local Push onFailure", throwable)
                }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun showAlertDialogWithUrl(context: Context) {
        val url = "https://www.rustore.ru/instruction"
        val linkText = "Скачать RuStore"

        // Construct the HTML message string
        val message = "Ссылка на приложение:\n<a href=\"$url\">$linkText</a>"

        val builder = MaterialAlertDialogBuilder(context)
        builder.setTitle("Пожалуйста, установите RuStore!\nЧтобы работали пуш-уведомления.")
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
