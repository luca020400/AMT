package com.luca020400.amt

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.provider.SearchRecentSuggestions
import android.support.annotation.UiThread
import android.support.annotation.WorkerThread
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SearchView
import android.view.Menu
import android.widget.Toast
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener,
        SearchView.OnQueryTextListener {
    private val mStopAdapter by lazy {
        StopAdapter(arrayListOf<StopData>())
    }

    private val mSuggestions by lazy {
        SearchRecentSuggestions(this,
                StopSuggestionProvider.AUTHORITY, StopSuggestionProvider.MODE)
    }

    private var mCode: String? = null
    private var mShouldExpand = true

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

        val data = intent.data
        if (data != null) {
            val code = data.getQueryParameter("CodiceFermata")
            if (is_code_valid(code)) {
                StopTask().execute(code)
                mShouldExpand = false
            } else {
                Toast.makeText(this, R.string.malformed_url,
                        Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        val code = intent.getStringExtra(SearchManager.QUERY)
        if (is_code_valid(code)) {
            StopTask().execute(code)
        }
    }

    override fun onRefresh() {
        if (is_code_valid(mCode)) {
            StopTask().execute(mCode)
        } else {
            swipe_refresh.postDelayed({ swipe_refresh.isRefreshing = false }, 250)
            Toast.makeText(this, R.string.invalid_code, Toast.LENGTH_SHORT).show()
        }
    }

    private fun is_code_valid(code: String?): Boolean {
        return code != null && code.length == 4 && code.toIntOrNull() != null
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)

        val item = menu.findItem(R.id.menu_search)
        if (mShouldExpand) item.expandActionView()
        val searchView = item.actionView as SearchView

        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        searchView.maxWidth = Integer.MAX_VALUE
        searchView.setOnQueryTextListener(this)

        return true
    }

    override fun onQueryTextChange(code: String?): Boolean {
        return false
    }

    override fun onQueryTextSubmit(code: String?): Boolean {
        if (is_code_valid(code)) {
            StopTask().execute(code)
        } else {
            Toast.makeText(this, R.string.codice_corto, Toast.LENGTH_SHORT).show()
        }
        return false
    }

    private inner class StopTask : AsyncTask<String, Void, Stop>() {
        @UiThread
        override fun onPreExecute() {
            swipe_refresh.post { swipe_refresh.isRefreshing = true }
        }

        @WorkerThread
        override fun doInBackground(vararg strings: String): Stop {
            return Parser(Constants.url, strings[0]).parse()
        }

        @UiThread
        override fun onPostExecute(stop: Stop) {
            if (!stop.name.isNullOrBlank() && stop.stops.isNotEmpty()) {
                mStopAdapter.addAll(stop.stops)

                mSuggestions.saveRecentQuery(stop.code, stop.name)
                empty_text.text = getString(R.string.status_stop_name_code, stop.name, stop.code)
                empty_text.isClickable = true
                empty_text.setOnClickListener {
                    startActivity(Intent.createChooser(
                            Utils().toLink(stop.code, getString(R.string.share_subject)),
                            getString(R.string.share_with))
                    )
                }

                mCode = stop.code
            } else {
                empty_text.isClickable = empty_text.hasOnClickListeners()
                Toast.makeText(this@MainActivity, getString(R.string.no_transiti, stop.code),
                        Toast.LENGTH_SHORT).show()
            }

            swipe_refresh.post { swipe_refresh.isRefreshing = false }
        }
    }
}
