package com.luca020400.amt;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

class Parser {
    private final String url;
    private final String code;

    Parser(String url, String code) {
        this.url = url;
        this.code = code;
    }

    List<Stop> parse() throws IOException {
        List<Stop> stops = new LinkedList<>();

        Document document = Jsoup.connect(url).data("CodiceFermata", code).get();
        Elements trs = document.select("tr");
        trs.remove(0);

        for (Element tr : trs) {
            Elements tds = tr.select("td");
            if (tds.size() == 4) {
                stops.add(new Stop(tds.get(0).text(), tds.get(1).text(), tds.get(2).text(), tds.get(3).text()));
            }
        }
        return stops;
    }
}
