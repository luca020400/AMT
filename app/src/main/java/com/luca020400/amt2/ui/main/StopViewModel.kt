package com.luca020400.amt2.ui.main

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.luca020400.amt2.Constants
import com.luca020400.amt2.Constants.url
import com.luca020400.amt2.classes.Stop
import com.luca020400.amt2.classes.StopData
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class StopViewModel : ViewModel() {
    private val sTAG = this::class.java.simpleName

    private var stop = MutableLiveData<Stop>()

    fun getStop(): LiveData<Stop> {
        return stop
    }

    fun loadStops(code: String) {
        // Do an asynchronous operation to fetch stops.
        GlobalScope.launch {
            val document: Document

            try {
                document = Jsoup.connect(url).data(Constants.query, code).get()
                    .also { if (!it.hasText()) throw Throwable("Document is empty") }
            } catch (e: Throwable) {
                Log.e(sTAG, e.message, e)
                return@launch
            }

            this@StopViewModel.stop.postValue(Stop(
                code, document.select("font")[1].text(),
                document.select("tr")
                    .map { it.select("td") }
                    .filter { it.size == 4 }
                    .map { StopData(it[0].text(), it[1].text(), it[2].text(), it[3].text()) })
            )
        }
    }
}
