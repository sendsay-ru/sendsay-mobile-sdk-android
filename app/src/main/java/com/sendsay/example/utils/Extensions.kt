package com.sendsay.example.utils

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import com.google.android.material.textfield.TextInputEditText
import java.io.Serializable

fun TextInputEditText.isValid(): Boolean {
    val isValid = !text.toString().isEmpty()
    error = if (isValid) {
        null
    } else {
        "Empty Field"
    }
    return isValid
}

fun TextInputEditText.isVaildUrl(): Boolean {
    val isEmpty = text.toString().isEmpty()
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