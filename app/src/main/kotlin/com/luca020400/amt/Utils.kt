package com.luca020400.amt

import android.content.Intent

internal object Utils {
    fun toLink(code: String, here: String): Intent {
        val shareIntent = Intent()
        shareIntent.action = Intent.ACTION_SEND
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, here)
        shareIntent.putExtra(Intent.EXTRA_TEXT, Constants.url + "?CodiceFermata=" + code)
        shareIntent.type = "text/plain"
        return shareIntent
    }
}
