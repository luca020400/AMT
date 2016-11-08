package com.luca020400.amt;

import java.io.Serializable;

class Stop implements Serializable {
    private String line;
    private String destination;
    private String schedule;
    private String remainingtime;

    Stop(String line, String destination, String schedule, String remainingtime) {
        this.line = line;
        this.destination = destination;
        this.schedule = schedule;
        this.remainingtime = remainingtime;
    }

    String getLine() {
        return line;
    }

    String getDestination() {
        return destination;
    }

    String getSchedule() {
        return schedule;
    }

    String getRemainingtime() {
        return remainingtime;
    }
}
