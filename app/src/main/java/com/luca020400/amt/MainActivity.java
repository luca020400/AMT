package com.luca020400.amt;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.UiThread;
import android.support.annotation.WorkerThread;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {
    private final String TAG = MainActivity.class.getSimpleName();

    private String mListQuery;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private StopAdapter mAdapter;
    private TextView mStatusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main);

        mStatusText = (TextView) findViewById(R.id.empty_text);

        // Setup SwipeRefreshLayout
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        // Setup refresh listener which triggers new data loading
        mSwipeRefreshLayout.setOnRefreshListener(this);
        // Color scheme of the refresh spinner
        mSwipeRefreshLayout.setColorSchemeResources(
                R.color.colorPrimaryDark, R.color.colorAccent);

        // Setup RecyclerView
        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        // Setup divider for RecyclerView items
        mRecyclerView.addItemDecoration(new Divider(this));
        // Setup item animator
        mRecyclerView.setItemAnimator(null);    // Disable to prevent view blinking when refreshing
        // Setup and initialize RecyclerView adapter
        mAdapter = new StopAdapter(new CopyOnWriteArrayList<>());
        mRecyclerView.setAdapter(mAdapter);

        setText(null, true);
    }

    @Override
    public void onRefresh() {
        new StopTask().execute();
    }

    private void hideKeyboard() {
        InputMethodManager inputManager =
                (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        View focusedView = getCurrentFocus();
        if (focusedView != null) {
            inputManager.hideSoftInputFromWindow(focusedView.getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    private class StopTask extends AsyncTask<Void, Void, List<Stop>> {
        private String code;

        @UiThread
        @Override
        protected void onPreExecute() {
            mSwipeRefreshLayout.post(() -> mSwipeRefreshLayout.setRefreshing(true));
            code = mListQuery;
        }

        @WorkerThread
        @Override
        protected List<Stop> doInBackground(Void... voids) {
            String url = "http://www.amt.genova.it/amt/servizi/passaggi_i.php";
            List<Stop> stops = new LinkedList<>();

            try {
                stops.addAll(new Parser(url, code).parse());
            } catch (IOException e) {
                Log.e(TAG, "", e);
            }

            return stops;
        }

        @UiThread
        @Override
        protected void onPostExecute(List<Stop> stops) {
            if (stops.size() != 0) {
                mAdapter.clear();
                mAdapter.addAll(stops);
            } else {
                Toast.makeText(getApplicationContext(), R.string.no_transiti, Toast.LENGTH_SHORT).show();
            }

            // Delay refreshing animation just for the show
            new Handler().postDelayed(() -> mSwipeRefreshLayout.setRefreshing(false), 300);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu mMenu) {
        getMenuInflater().inflate(R.menu.main, mMenu);
        setupSearchView(mMenu.findItem(R.id.menu_search));
        return true;
    }

    private void setupSearchView(MenuItem mItem) {
        SearchView mSearchView = (SearchView) MenuItemCompat.getActionView(mItem);
        if (mSearchView == null) {
            return;
        }

        mSearchView.setInputType(InputType.TYPE_CLASS_NUMBER);
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String mQuery) {
                mListQuery = mQuery;
                setText(mQuery, true);
                hideKeyboard();
                new StopTask().execute();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String mNewText) {
                if (!mNewText.isEmpty()) {
                    setText(mNewText, false);
                }
                return true;
            }
        });

    }

    private void setText(String mText, boolean isDone) {
        if (mText == null || mAdapter.getItemCount() == 0) {
            mStatusText.setText(getString(R.string.status_no_results));
        } else {
            mStatusText.setText(String.format(getString(isDone ?
                    R.string.status_results : R.string.status_results_hint), mText));
        }
    }

}
