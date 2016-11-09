package com.luca020400.amt;

class Stop {
    private final String line;
    private final String destination;
    private final String schedule;
    private final String remainingtime;

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
