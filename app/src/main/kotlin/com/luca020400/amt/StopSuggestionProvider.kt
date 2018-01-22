package com.luca020400.amt

import android.content.SearchRecentSuggestionsProvider

internal class StopSuggestionProvider : SearchRecentSuggestionsProvider() {
    init {
        setupSuggestions(AUTHORITY, MODE)
    }

    companion object {
        const val AUTHORITY = "com.luca020400.amt.StopSuggestionProvider"
        const val MODE = DATABASE_MODE_QUERIES or DATABASE_MODE_2LINES
    }
}
