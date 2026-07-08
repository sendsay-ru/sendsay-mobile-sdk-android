package com.sendsay.example.utils

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.core.view.GestureDetectorCompat
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import java.io.Serializable

fun TextInputEditText.isValid(): Boolean {
    val isValid = !text.isNullOrEmpty()
    error = if (isValid) {
        null
    } else {
        "Empty Field"
    }
    return isValid
}

fun TextInputEditText.isVaildUrl(): Boolean {
    val isEmpty = text.isNullOrEmpty()
    val text = text ?: ""
    val isUrl = Patterns.WEB_URL.matcher(text).matches() && (text.startsWith("https://") || text.startsWith("http://"))
    error = when {
        isEmpty -> "Empty URL"
        !isUrl -> "Invalid Url"
        else -> null
    }
    return !isEmpty && isUrl
}

fun TextInputEditText.onTextChanged(callback: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            //
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            //
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            val text = s.toString()
            callback(text)
            isValid()
        }
    })
}

fun HashMap<String, Any>.asJson(): String {
    var string = "{\n"
    this.toList().forEachIndexed { index, pair ->
        string += "\t ${pair.first}: ${pair.second}"
        if (index != this.size - 1) string += ",\n"
    }
    return "$string\n}"
}

inline fun <reified T : Serializable> Bundle.serializable(key: String): T? = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> getSerializable(key, T::class.java)
    else -> @Suppress("DEPRECATION") getSerializable(key) as? T
}

inline fun <reified T : Serializable> Intent.serializable(key: String): T? = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> getSerializableExtra(key, T::class.java)
    else -> @Suppress("DEPRECATION") getSerializableExtra(key) as? T
}

fun RecyclerView.smoothSnapToPosition(position: Int, snapMode: Int = LinearSmoothScroller.SNAP_TO_END) {
    val smoothScroller = object : LinearSmoothScroller(this.context) {
        override fun getVerticalSnapPreference(): Int = snapMode
        override fun getHorizontalSnapPreference(): Int = snapMode
    }
    smoothScroller.targetPosition = position
    layoutManager?.startSmoothScroll(smoothScroller)
}

fun shareText(context: Context, textToShare: String) {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, textToShare)
        type = "text/plain"
    }

    // Wraps intent in a chooser to ensure it presents as a bottom sheet
    val shareIntent = Intent.createChooser(sendIntent, "Отправить через")
    context.startActivity(shareIntent)
}

fun RecyclerView.addOnItemLongClickListener(onLongClick: (childView: android.view.View, position: Int) -> Unit) {

    // 1. Initialize the GestureDetector with an OnGestureListener
    val gestureDetector =
        GestureDetectorCompat(this.context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onLongPress(e: MotionEvent) {
                // Find the child view under the coordinates of the motion event
                val childView = findChildViewUnder(e.x, e.y)
                if (childView != null) {
                    val position = getChildAdapterPosition(childView)
                    if (position != RecyclerView.NO_POSITION) {
                        onLongClick(childView, position)
                    }
                }
            }
        })

    // 2. Intercept the touch events and pass them to the gesture detector
    this.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
        override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
            gestureDetector.onTouchEvent(e)
            return false // Allow other touch behaviors (like scrolling) to continue
        }

        override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}
        override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
    })
}
