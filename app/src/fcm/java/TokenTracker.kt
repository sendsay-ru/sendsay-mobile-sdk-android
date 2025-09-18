import android.content.Context
import ru.sendsay.sdk.Sendsay
import com.google.firebase.messaging.FirebaseMessaging

class TokenTracker {
    fun trackToken(context: Context?) {
        FirebaseMessaging.getInstance().token.addOnSuccessListener {
            Sendsay.trackPushToken(it)
        }
    }
}
