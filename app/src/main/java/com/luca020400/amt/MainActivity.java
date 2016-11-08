package com.luca020400.amt;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.UiThread;
import android.support.annotation.WorkerThread;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {
    private EditText mEditText;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private StopAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main);

        mEditText = (EditText) findViewById(R.id.code);
        Button mButton = (Button) findViewById(R.id.button);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(
                R.color.colorPrimaryDark, R.color.colorAccent);

        // Setup RecyclerView
        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new StopAdapter(new CopyOnWriteArrayList<>());
        mRecyclerView.setAdapter(mAdapter);

        mButton.setOnClickListener(view -> {
            InputMethodManager inputManager =
                    (InputMethodManager) this.
                            getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(
                    this.getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
            new StopTask().execute();
        });
    }

    private class StopTask extends AsyncTask<Void, Void, List<Stop>> {
        private String code;

        @UiThread
        @Override
        protected void onPreExecute() {
            code = mEditText.getText().toString();
        }

        @WorkerThread
        @Override
        protected List<Stop> doInBackground(Void... voids) {
            String url = "http://www.amt.genova.it/amt/servizi/passaggi_i.php";
            List<Stop> stops = new LinkedList<>();

            try {
                stops.addAll(new Parser(url, code).parse());
            } catch (IOException e) {
                e.printStackTrace();
            }

            return stops;
        }

        @UiThread
        @Override
        protected void onPostExecute(List<Stop> stops) {
            if (stops.size() != 0) {
                mAdapter.clear();
                mAdapter.addAll(stops);
            }

            // Delay refreshing animation just for the show
            new Handler().postDelayed(() -> mSwipeRefreshLayout.setRefreshing(false), 300);
        }
    }

    @Override
    public void onRefresh() {
        new StopTask().execute();
    }
}
