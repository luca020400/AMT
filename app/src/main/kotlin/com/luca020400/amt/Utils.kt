package com.luca020400.amt

import android.content.Context
import android.content.Intent

class Utils {
    fun toLink(code: String, context: Context): Intent {
        val shareIntent = Intent()
        shareIntent.action = Intent.ACTION_SEND
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.share_subject))
        shareIntent.putExtra(Intent.EXTRA_TEXT, Constants.url + "?CodiceFermata=" + code)
        shareIntent.type = "text/plain"
        return shareIntent
    }
}