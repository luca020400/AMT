package com.luca020400.amt2

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.luca020400.amt2.ui.main.StopFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, StopFragment.newInstance())
                .commitNow()
        }
    }

}
