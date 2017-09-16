package com.luca020400.amt

import android.annotation.SuppressLint
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

    private fun String?.isValidCode() = this != null && matches("\\d{4}".toRegex())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.content_main)

        setSupportActionBar(toolbar)

        // Setup SwipeRefreshLayout
        with(swipe_refresh) {
            // Setup refresh listener which triggers new data loading
            setOnRefreshListener(this@MainActivity)
            // Color scheme of the refresh spinner
            setColorSchemeResources(R.color.colorPrimaryDark, R.color.colorAccent)
        }

        // Setup RecyclerView
        with(recycler_view) {
            // Setup the layout manager
            val linearLayoutManager = LinearLayoutManager(this@MainActivity)
            layoutManager = linearLayoutManager
            // Setup divider for the items
            val dividerItemDecoration = DividerItemDecoration(recycler_view.context,
                    linearLayoutManager.orientation)
            addItemDecoration(dividerItemDecoration)
            // Setup and initialize the adapter
            adapter = mStopAdapter
        }

        // Setup SearchView
        with(search_view) {
            // Setup the searchable info
            val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
            setSearchableInfo(searchManager.getSearchableInfo(componentName))
            // Set max width to MAX
            maxWidth = Integer.MAX_VALUE
            // Setup query text listener
            setOnQueryTextListener(this@MainActivity)
            // Expand the view
            onActionViewExpanded()
        }

        onNewIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        if (intent.action == Intent.ACTION_SEARCH) {
            val code = intent.getStringExtra(SearchManager.QUERY)
            if (code.isValidCode()) {
                StopTask().execute(code)
            }
        } else if (intent.action == Intent.ACTION_VIEW) {
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
    }

    override fun onRefresh() {
        if (mCode.isValidCode()) {
            StopTask().execute(mCode)
        } else {
            swipe_refresh.postDelayed({ swipe_refresh.isRefreshing = false }, 250)
            Toast.makeText(this, R.string.invalid_code, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onQueryTextChange(code: String) = false

    override fun onQueryTextSubmit(code: String): Boolean {
        if (code.isValidCode()) {
            StopTask().execute(code)
        } else {
            Toast.makeText(this, R.string.codice_corto, Toast.LENGTH_SHORT).show()
        }
        return false
    }

    @SuppressLint("StaticFieldLeak")
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
                with(empty_text) {
                    text = getString(R.string.status_stop_name_code, stop.name, stop.code)
                    isClickable = true
                    setOnClickListener {
                        startActivity(Intent.createChooser(
                                Utils.toLink(stop.code, getString(R.string.share_subject)),
                                getString(R.string.share_with))
                        )
                    }
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
