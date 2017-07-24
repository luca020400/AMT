package com.luca020400.amt

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.provider.SearchRecentSuggestions
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SearchView
import android.widget.Toast
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener,
        SearchView.OnQueryTextListener {
    private val mStopAdapter by lazy {
        StopAdapter()
    }

    private val mSuggestions by lazy {
        SearchRecentSuggestions(this,
                StopSuggestionProvider.AUTHORITY, StopSuggestionProvider.MODE)
    }

    private var mCode: String? = null

    fun String?.isValidCode() = this != null && matches("\\d{4}".toRegex())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.content_main)

        setSupportActionBar(toolbar)

        // Setup refresh listener which triggers new data loading
        swipe_refresh.setOnRefreshListener(this)
        // Color scheme of the refresh spinner
        swipe_refresh.setColorSchemeResources(
                R.color.colorPrimaryDark, R.color.colorAccent)

        // Setup RecyclerView
        recycler_view.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(this)
        recycler_view.layoutManager = layoutManager
        // Setup divider for RecyclerView items
        val dividerItemDecoration = DividerItemDecoration(recycler_view.context,
                layoutManager.orientation)
        recycler_view.addItemDecoration(dividerItemDecoration)
        // Disable item animator to prevent view blinking when refreshing
        recycler_view.itemAnimator.changeDuration = 0
        // Setup and initialize RecyclerView adapter
        recycler_view.adapter = mStopAdapter

        // Setup SearchView
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        search_view.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        search_view.maxWidth = Integer.MAX_VALUE
        search_view.setOnQueryTextListener(this)
        search_view.onActionViewExpanded()

        handleViewIntent(intent)
    }

    fun handleViewIntent(intent: Intent) {
        intent.data?.let {
            val code = it.getQueryParameter("CodiceFermata")
            if (code.isValidCode()) {
                StopTask().execute(code)
                search_view.clearFocus()
            } else {
                Toast.makeText(this, R.string.malformed_url,
                        Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        if (intent.action == Intent.ACTION_SEARCH) {
            val code = intent.getStringExtra(SearchManager.QUERY)
            if (code.isValidCode()) {
                StopTask().execute(code)
            }
        } else if (intent.action == Intent.ACTION_VIEW) {
            handleViewIntent(intent)
        }
    }

    override fun onRefresh() {
        if (mCode.isValidCode()) {
            StopTask().execute(mCode)
        } else {
            swipe_refresh.postDelayed({ swipe_refresh.isRefreshing = false }, 250)
            Toast.makeText(this, R.string.invalid_code, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onQueryTextChange(code: String): Boolean {
        return false
    }

    override fun onQueryTextSubmit(code: String): Boolean {
        if (code.isValidCode()) {
            StopTask().execute(code)
        } else {
            Toast.makeText(this, R.string.codice_corto, Toast.LENGTH_SHORT).show()
        }
        return false
    }

    private inner class StopTask : AsyncTask<String, Void, Stop>() {
        override fun onPreExecute() {
            swipe_refresh.post { swipe_refresh.isRefreshing = true }
        }

        override fun doInBackground(vararg strings: String): Stop {
            return Parser(Constants.url, strings[0]).parse()
        }

        override fun onPostExecute(stop: Stop) {
            if (!stop.name.isNullOrBlank() && stop.stops.isNotEmpty()) {
                mStopAdapter.addAll(stop.stops)

                mSuggestions.saveRecentQuery(stop.code, stop.name)
                empty_text.text = getString(R.string.status_stop_name_code, stop.name, stop.code)
                empty_text.isClickable = true
                empty_text.setOnClickListener {
                    startActivity(Intent.createChooser(
                            Utils.toLink(stop.code, getString(R.string.share_subject)),
                            getString(R.string.share_with))
                    )
                }
            } else {
                Toast.makeText(this@MainActivity, getString(R.string.no_transiti, stop.code),
                        Toast.LENGTH_SHORT).show()
            }

            mCode = stop.code
            swipe_refresh.post { swipe_refresh.isRefreshing = false }
        }
    }
}
