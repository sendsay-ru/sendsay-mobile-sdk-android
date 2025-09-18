package ru.sendsay.sdk.shadows

import android.media.Ringtone
import ru.sendsay.sdk.runcatching.SendsayExceptionThrowing
import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements

@Implements(Ringtone::class)
class OnlyDefaultShadowRingtone : ShadowRingtone() {

    @Implementation
    override fun play() {
        if (withUri?.toString() != "content://settings/system/notification_sound") {
            throw SendsayExceptionThrowing.TestPurposeException()
        }
        super.play()
    }
}
