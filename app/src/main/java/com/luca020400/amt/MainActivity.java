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
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {
    private final String TAG = MainActivity.class.getSimpleName();

    private EditText mEditText;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private StopAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main);

        mEditText = (EditText) findViewById(R.id.code);
        mEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                hideKeyboard();
                new StopTask().execute();
                return true;
            }
            return false;
        });

        Button mButton = (Button) findViewById(R.id.button);
        mButton.setOnClickListener(view -> {
            hideKeyboard();
            new StopTask().execute();
        });

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
    }

    private class StopTask extends AsyncTask<Void, Void, List<Stop>> {
        private String code;

        @UiThread
        @Override
        protected void onPreExecute() {
            mSwipeRefreshLayout.post(() -> mSwipeRefreshLayout.setRefreshing(true));
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
}
