package ru.sendsay.sdk.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.FrameLayout
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import ru.sendsay.sdk.Sendsay
import ru.sendsay.sdk.R
import ru.sendsay.sdk.models.MessageItem
import ru.sendsay.sdk.services.OnIntegrationStoppedCallback
import ru.sendsay.sdk.util.ConversionUtils
import ru.sendsay.sdk.util.Logger

internal class AppInboxDetailActivity : AppCompatActivity(), OnIntegrationStoppedCallback {
    companion object {
        public val MESSAGE_ID = "MessageID"
        fun buildIntent(context: Context, item: MessageItem): Intent {
            return Intent(context, AppInboxDetailActivity::class.java).apply {
                putExtra(MESSAGE_ID, item.id)
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.message_inbox_detail_activity)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val messageId = intent.getStringExtra(MESSAGE_ID)
        if (messageId == null) {
            finish()
            return
        }
        Sendsay.fetchAppInboxItem(messageId) { message ->
            supportActionBar?.title = message?.content?.title
                    ?: getString(R.string.sendsay_inbox_defaultTitle)
        }
        Sendsay.getComponent()?.sendsayConfiguration?.appInboxDetailImageInset?.let {
            try {
                val detailContainer = findViewById<FrameLayout>(R.id.container)
                val layoutParams = detailContainer.layoutParams as RelativeLayout.LayoutParams
                layoutParams.topMargin = ConversionUtils.dpToPx(it)
                detailContainer.layoutParams = layoutParams
            } catch (e: Exception) {
                Logger.e(this, """
                    App Inbox detail screen changed in layout, unable to apply `appInboxDetailImageInset`
                """.trimIndent())
            }
        }
        val detailFragment = Sendsay.getAppInboxDetailFragment(this, messageId)
        if (detailFragment == null) {
            finish()
            return
        }
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.container, detailFragment)
            .commit()
        Sendsay.deintegration.registerForIntegrationStopped(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onIntegrationStopped() {
        finish()
    }

    override fun onDestroy() {
        Sendsay.deintegration.unregisterForIntegrationStopped(this)
        super.onDestroy()
    }
}
