package com.luca020400.amt

import org.jsoup.Jsoup

internal class Parser(private val url: String, private val code: String) {

    fun parse(): List<Stop> {
        val stops: MutableList<Stop> = mutableListOf()

        val document = Jsoup.connect(url).data("CodiceFermata", code).get()
        val trs = document.select("tr")
        trs.removeAt(0)
        trs
                .map { it.select("td") }
                .filter { it.size == 4 }
                .mapTo(stops) { Stop(it[0].text(), it[1].text(), it[2].text(), it[3].text()) }

        return stops
    }
}
