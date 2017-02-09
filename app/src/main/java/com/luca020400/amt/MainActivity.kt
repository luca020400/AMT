package com.luca020400.amt

import android.app.SearchManager
import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.support.annotation.UiThread
import android.support.annotation.WorkerThread
import android.support.v4.view.MenuItemCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SearchView
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener {
    private var mCode: String? = null
    private var mAdapter: StopAdapter? = null
    private var doExpand = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.content_main)

        val mToolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(mToolbar)

        // Setup refresh listener which triggers new data loading
        swipe_refresh.setOnRefreshListener(this)
        // Color scheme of the refresh spinner
        swipe_refresh.setColorSchemeResources(
                R.color.colorPrimaryDark, R.color.colorAccent)

        // Setup RecyclerView
        recycler_view.setHasFixedSize(true)
        recycler_view.layoutManager = LinearLayoutManager(this)
        // Setup divider for RecyclerView items
        recycler_view.addItemDecoration(Divider(this))
        // Disable item animator to prevent view blinking when refreshing
        recycler_view.itemAnimator = null
        // Setup and initialize RecyclerView adapter
        mAdapter = StopAdapter()
        recycler_view.adapter = mAdapter

        val data = intent.data

        if (data != null) {
            mCode = data.getQueryParameter("CodiceFermata")
            if (mCode != null && mCode!!.length == 4) {
                setText(mCode)
                StopTask().execute()
                doExpand = false
            } else {
                Toast.makeText(applicationContext, R.string.malformed_url, Toast.LENGTH_SHORT).show()
                setText(null)
            }
        } else {
            setText(null)
        }
    }

    override fun onRefresh() {
        StopTask().execute()
    }

    private fun hideKeyboard() {
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.hideSoftInputFromWindow(currentFocus.windowToken,
                InputMethodManager.HIDE_NOT_ALWAYS)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        super.onPrepareOptionsMenu(menu)
        val item = menu.findItem(R.id.menu_search)
        if (doExpand) MenuItemCompat.expandActionView(item)
        val mSearchView = MenuItemCompat.getActionView(item) as SearchView

        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        mSearchView.maxWidth = Integer.MAX_VALUE

        mSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                if (query.length == 4) {
                    mCode = query
                    setText(mCode)
                    hideKeyboard()
                    StopTask().execute()
                    return true
                } else {
                    Toast.makeText(applicationContext, R.string.codice_corto, Toast.LENGTH_SHORT).show()
                    return false
                }
            }

            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }
        })
        return true
    }

    private fun setText(mText: String?) {
        if (mText == null) {
            empty_text.text = getString(R.string.status_no_results)
        } else {
            empty_text.text = String.format(getString(R.string.status_results), mText)
        }
    }

    private inner class StopTask : AsyncTask<Void, Void, List<Stop>>() {
        @UiThread
        override fun onPreExecute() {
            swipe_refresh.post { swipe_refresh.isRefreshing = true }
        }

        @WorkerThread
        override fun doInBackground(vararg voids: Void): List<Stop> {
            val url = "http://www.amt.genova.it/amt/servizi/passaggi_i.php"
            return Parser(url, mCode!!).parse()
        }

        @UiThread
        override fun onPostExecute(stops: List<Stop>) {
            if (!stops.isEmpty()) {
                mAdapter!!.clear()
                mAdapter!!.addAll(stops)
            } else {
                Toast.makeText(applicationContext, R.string.no_transiti, Toast.LENGTH_SHORT).show()
            }

            swipe_refresh.post { swipe_refresh.isRefreshing = false }
        }
    }
}
