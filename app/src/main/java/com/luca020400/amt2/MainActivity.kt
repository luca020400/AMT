package com.luca020400.amt2

import android.app.SearchManager
import android.content.Intent
import android.os.Bundle
import android.provider.SearchRecentSuggestions
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.luca020400.amt2.classes.Stop
import com.luca020400.amt2.providers.StopSuggestionProvider
import com.luca020400.amt2.ui.main.StopAdapter
import com.luca020400.amt2.ui.main.StopViewModel
import kotlinx.android.synthetic.main.main_activity.*

class MainActivity : AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener,
    SearchView.OnQueryTextListener {

    private val mStopAdapter by lazy {
        StopAdapter()
    }

    private val mSuggestions by lazy {
        SearchRecentSuggestions(
            this,
            StopSuggestionProvider.AUTHORITY, StopSuggestionProvider.MODE
        )
    }

    private lateinit var code: String
    private lateinit var viewModel: StopViewModel

    private fun String?.isValidCode() = this != null && matches("\\d{4}".toRegex())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

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
            val linearLayoutManager = LinearLayoutManager(context)
            layoutManager = linearLayoutManager
            // Setup divider for the items
            val dividerItemDecoration = DividerItemDecoration(
                recycler_view.context,
                linearLayoutManager.orientation
            )
            addItemDecoration(dividerItemDecoration)
            // Setup and initialize the adapter
            adapter = mStopAdapter
        }

        // Setup SearchView
        with(search_view) {
            // Setup the searchable info
            val searchManager = getSystemService(context, SearchManager::class.java)
            setSearchableInfo(searchManager?.getSearchableInfo(componentName))
            // Setup query text listener
            setOnQueryTextListener(this@MainActivity)
            // Expand the view
            onActionViewExpanded()
        }

        viewModel = ViewModelProviders.of(this).get(StopViewModel::class.java)
        viewModel.getStop().observe(this, Observer<Stop> { stop ->
            // update UI
            if (stop.stops.isNotEmpty()) {
                mStopAdapter.addAll(stop.stops)
            } else {
                Toast.makeText(
                    this, getString(R.string.no_transiti, stop.code),
                    Toast.LENGTH_SHORT
                ).show()
            }

            mSuggestions.saveRecentQuery(stop.code, stop.name)
            with(empty_text) {
                text = getString(R.string.status_stop_name_code, stop.name, stop.code)
                isClickable = true
                setOnClickListener {
                    startActivity(
                        Intent.createChooser(
                            Utils.toLink(stop.code, getString(R.string.share_subject)),
                            getString(R.string.share_with)
                        )
                    )
                }
            }
            code = stop.code
            swipe_refresh.isRefreshing = false
        })

        onNewIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        if (::code.isInitialized && code.isValidCode()) {
            viewModel.loadStops(code)
        }
    }

    override fun onNewIntent(intent: Intent) {
        if (intent.action == Intent.ACTION_SEARCH) {
            code = intent.getStringExtra(SearchManager.QUERY)
            if (code.isValidCode()) {
                swipe_refresh.isRefreshing = true
                viewModel.loadStops(code)
                search_view.clearFocus()
            }
        } else if (intent.action == Intent.ACTION_VIEW) {
            intent.data?.let {
                code = it.getQueryParameter(Constants.query)!!
                if (code.isValidCode()) {
                    swipe_refresh.isRefreshing = true
                    viewModel.loadStops(code)
                    search_view.clearFocus()
                } else {
                    Toast.makeText(
                        this, R.string.malformed_url,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun onRefresh() {
        if (::code.isInitialized && code.isValidCode()) {
            swipe_refresh.isRefreshing = true
            viewModel.loadStops(code)
        } else {
            Toast.makeText(this, R.string.invalid_code, Toast.LENGTH_SHORT).show()
            swipe_refresh.isRefreshing = false
        }
    }

    override fun onQueryTextChange(code: String) = false

    override fun onQueryTextSubmit(code: String): Boolean {
        if (code.isValidCode()) {
            swipe_refresh.isRefreshing = true
            viewModel.loadStops(code)
        } else {
            Toast.makeText(this, R.string.codice_corto, Toast.LENGTH_SHORT).show()
        }
        return false
    }
}
