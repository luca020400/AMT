package com.luca020400.amt

import android.content.SearchRecentSuggestionsProvider

internal class StopSuggestionProvider : SearchRecentSuggestionsProvider() {
    init {
        setupSuggestions(AUTHORITY, MODE)
    }

    companion object {
        val AUTHORITY = "com.luca020400.amt.StopSuggestionProvider"
        val MODE = SearchRecentSuggestionsProvider.DATABASE_MODE_QUERIES or SearchRecentSuggestionsProvider.DATABASE_MODE_2LINES
    }
}
