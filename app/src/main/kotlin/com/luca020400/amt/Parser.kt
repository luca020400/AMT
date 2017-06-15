package com.luca020400.amt

import org.jsoup.Jsoup

internal class Parser(private val url: String, private val code: String) {

    fun parse(): Stop {
        val name: String
        val stops: ArrayList<StopData> = arrayListOf()

        val document = Jsoup.connect(url).data("CodiceFermata", code).get()

        val br = document.select("font")
        name = br[1].text()

        val trs = document.select("tr")
        trs.removeAt(0)
        trs
            .map { it.select("td") }
            .filter { it.size == 4 }
            .mapTo(stops) { StopData(it[0].text(), it[1].text(), it[2].text(), it[3].text()) }

        return Stop(code, name, stops)
    }
}
