package com.luca020400.amt;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import java.util.LinkedList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

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
        List<Stop> CVDataList = new LinkedList<>();
        mAdapter = new StopAdapter(CVDataList);
        mRecyclerView.setAdapter(mAdapter);

        mButton.setOnClickListener(view -> {
            InputMethodManager inputManager =
                    (InputMethodManager) this.
                            getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(
                    this.getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
            new Handler().post(Codice);
        });
    }

    private final Runnable Codice = () -> {
        String url = "http://www.amt.genova.it/amt/servizi/passaggi_i.php";

        new AsyncHttpClient().get(url, new RequestParams("CodiceFermata", mEditText.getText().toString()), new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                mAdapter.clear();
                mAdapter.addAll(new Parser(responseString).parse());
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
    };

    @Override
    public void onRefresh() {
        new Handler().post(Codice);
    }
}
