package com.luca020400.amt2.ui.main

import android.app.SearchManager
import android.content.Intent
import android.os.Bundle
import android.provider.SearchRecentSuggestions
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.luca020400.amt2.R
import com.luca020400.amt2.providers.StopSuggestionProvider
import com.luca020400.amt2.Utils
import com.luca020400.amt2.classes.Stop
import kotlinx.android.synthetic.main.main_fragment.*

class StopFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener,
    SearchView.OnQueryTextListener {

    private val mStopAdapter by lazy {
        StopAdapter()
    }

    private val mSuggestions by lazy {
        SearchRecentSuggestions(
            context,
            StopSuggestionProvider.AUTHORITY, StopSuggestionProvider.MODE
        )
    }

    private lateinit var code: String
    private lateinit var viewModel: StopViewModel

    private fun String.isValidCode() = matches("\\d{4}".toRegex())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // Setup SwipeRefreshLayout
        with(swipe_refresh) {
            // Setup refresh listener which triggers new data loading
            setOnRefreshListener(this@StopFragment)
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
            setSearchableInfo(searchManager?.getSearchableInfo(activity?.componentName))
            // Setup query text listener
            setOnQueryTextListener(this@StopFragment)
            // Expand the view
            onActionViewExpanded()
        }

        viewModel = activity?.run {
            ViewModelProviders.of(this).get(StopViewModel::class.java)
        } ?: throw Exception("Invalid Activity")
        viewModel.getStop().observe(this, Observer<Stop> { stop ->
            // update UI
            if (stop.stops.isNotEmpty()) {
                mStopAdapter.addAll(stop.stops)

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
            } else {
                Toast.makeText(
                    context, getString(R.string.no_transiti, stop.code),
                    Toast.LENGTH_SHORT
                ).show()
            }

            code = stop.code
            with(swipe_refresh) { post { isRefreshing = false } }
        })
    }

    override fun onRefresh() {
        if (::code.isInitialized && code.isValidCode()) {
            with(swipe_refresh) { post { isRefreshing = true } }
            viewModel.loadStops(code)
        } else {
            with(swipe_refresh) { postDelayed({ isRefreshing = false }, 250) }
            Toast.makeText(context, R.string.invalid_code, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onQueryTextChange(code: String) = false

    override fun onQueryTextSubmit(code: String): Boolean {
        if (code.isValidCode()) {
            with(swipe_refresh) { post { isRefreshing = true } }
            viewModel.loadStops(code)
        } else {
            Toast.makeText(context, R.string.codice_corto, Toast.LENGTH_SHORT).show()
        }
        return false
    }
}
