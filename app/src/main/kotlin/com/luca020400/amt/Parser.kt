package com.luca020400.amt

import android.util.Log
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

internal class Parser(val url: String, val code: String) {
    private val TAG = this.javaClass.simpleName

    fun parse(): Stop {
        val name: String
        val stops = arrayListOf<StopData>()
        val document: Document?

        try {
            document = Jsoup.connect(url).data("CodiceFermata", code).get()
                    .takeIf { it -> it.hasText() }
            if (document == null) {
                throw Throwable("Document is empty")
            }
        } catch (e: Throwable) {
            Log.e(TAG, e.localizedMessage, e)
            return Stop(code, null, stops)
        }

        val brs = document.select("font")
        name = brs[1].text()

        val trs = document.select("tr")
        trs.removeAt(0)
        trs
                .map { it.select("td") }
                .filter { it.size == 4 }
                .mapTo(stops) { StopData(it[0].text(), it[1].text(), it[2].text(), it[3].text()) }

        return Stop(code, name, stops)
    }
}
