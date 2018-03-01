package com.luca020400.amt

import android.util.Log
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

internal class Parser(private val url: String, private val code: String) {
    private val sTAG = "Parser"

    fun parse(): Stop {
        val document: Document

        try {
            document = Jsoup.connect(url).data(Constants.query, code).get()
            if (!document.hasText()) {
                throw Throwable("Document is empty")
            }
        } catch (e: Throwable) {
            Log.e(sTAG, e.message, e)
            return Stop(code, null, listOf())
        }

        val name = document.select("font")[1].text()

        return Stop(code, name, document.select("tr")
                .map { it.select("td") }
                .filter { it.size == 4 }
                .map { StopData(it[0].text(), it[1].text(), it[2].text(), it[3].text()) })
    }
}
