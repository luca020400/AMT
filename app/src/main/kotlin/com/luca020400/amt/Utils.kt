package com.luca020400.amt

import android.content.Intent

internal object Utils {
    fun toLink(code: String, here: String) = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_SUBJECT, here)
        putExtra(Intent.EXTRA_TEXT, Constants.url + "?" + Constants.query + "=" + code)
        type = "text/plain"
    }
}
