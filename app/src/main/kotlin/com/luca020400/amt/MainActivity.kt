package com.luca020400.amt

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.provider.SearchRecentSuggestions
import android.support.annotation.UiThread
import android.support.annotation.WorkerThread
import android.support.v4.view.MenuItemCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SearchView
import android.telephony.TelephonyManager
import android.view.Menu
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener,
        SearchView.OnQueryTextListener {
    private val stopAdapter by lazy {
        StopAdapter(arrayListOf<StopData>())
    }

    private val telephonyManager by lazy {
        getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    }

    private val suggestions by lazy {
        SearchRecentSuggestions(this,
                StopSuggestionProvider.AUTHORITY, StopSuggestionProvider.MODE)
    }

    private var mCode: String? = null
    private var doExpand = true

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
        recycler_view.itemAnimator = null
        // Setup and initialize RecyclerView adapter
        recycler_view.adapter = stopAdapter

        val data = intent.data
        if (data != null) {
            val code = data.getQueryParameter("CodiceFermata")
            if (is_code_valid(code)) {
                mCode = code
                StopTask().execute()
                doExpand = false
            } else {
                Toast.makeText(applicationContext, R.string.malformed_url,
                        Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        val code = intent.getStringExtra(SearchManager.QUERY)
        if (is_code_valid(code)) {
            mCode = code
            StopTask().execute()
        }
    }

    override fun onRefresh() {
        if (is_code_valid(mCode)) {
            StopTask().execute()
        } else {
            swipe_refresh.post { swipe_refresh.isRefreshing = false }
            Toast.makeText(applicationContext, R.string.invalid_code, Toast.LENGTH_SHORT).show()
        }
    }

    private fun is_code_valid(code: String?): Boolean {
        return code != null && code.length == 4 && code.toIntOrNull() != null
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)

        val item = menu.findItem(R.id.menu_search)
        if (doExpand) MenuItemCompat.expandActionView(item)
        val mSearchView = MenuItemCompat.getActionView(item) as SearchView

        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        mSearchView.maxWidth = Integer.MAX_VALUE
        mSearchView.setOnQueryTextListener(this)

        return true
    }

    override fun onQueryTextChange(code: String?): Boolean {
        return false
    }

    override fun onQueryTextSubmit(code: String?): Boolean {
        if (is_code_valid(code)) {
            mCode = code
            StopTask().execute()
        } else {
            Toast.makeText(applicationContext, R.string.codice_corto, Toast.LENGTH_SHORT).show()
        }
        return false
    }

    private fun hasPhoneAbility(): Boolean {
        return telephonyManager.phoneType != TelephonyManager.PHONE_TYPE_NONE
    }

    fun buy_ticket(v: View) {
        if (hasPhoneAbility()) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle(R.string.buy_ticket_title)
                    .setMessage(R.string.buy_ticket_summary)
                    .setPositiveButton(android.R.string.ok, { _, _ ->
                        val uri = Uri.parse("smsto:4850209")
                        val intent = Intent(Intent.ACTION_SENDTO, uri)
                        intent.putExtra("sms_body", "AMT")
                        startActivity(intent)
                    })
                    .setNegativeButton(android.R.string.cancel, { _, _ -> })
                    .create()
                    .show()
        }
    }

    private inner class StopTask : AsyncTask<Void, Void, Stop>() {
        @UiThread
        override fun onPreExecute() {
            swipe_refresh.post { swipe_refresh.isRefreshing = true }
        }

        @WorkerThread
        override fun doInBackground(vararg voids: Void): Stop {
            return Parser(Constants.url, mCode!!).parse()
        }

        @UiThread
        override fun onPostExecute(stop: Stop) {
            if (!stop.name.isNullOrBlank() && !stop.stops.isEmpty()) {
                stopAdapter.clear()
                stopAdapter.addAll(stop.stops)

                suggestions.saveRecentQuery(stop.code, stop.name)
                empty_text.text = getString(R.string.status_stop_name_code, stop.name, stop.code)
                empty_text.setOnClickListener {
                    startActivity(Intent.createChooser(Utils()
                            .toLink(stop.code, applicationContext), getString(R.string.share_with)))
                }
            } else {
                Toast.makeText(applicationContext, getString(R.string.no_transiti, stop.code),
                        Toast.LENGTH_SHORT).show()
            }

            swipe_refresh.post { swipe_refresh.isRefreshing = false }
        }
    }
}
