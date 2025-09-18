package ru.sendsay.sdk.view

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import ru.sendsay.sdk.Sendsay
import ru.sendsay.sdk.R
import ru.sendsay.sdk.services.OnIntegrationStoppedCallback

class AppInboxListActivity : AppCompatActivity(), OnIntegrationStoppedCallback {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.message_inbox_list_activity)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.title = getString(R.string.sendsay_inbox_title)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val listFragment = Sendsay.getAppInboxListFragment(this)
        if (listFragment == null) {
            finish()
            return
        }
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.container, listFragment)
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
