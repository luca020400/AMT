package com.luca020400.amt;

import java.io.Serializable;

class Stop implements Serializable {
    private String line;
    private String destination;
    private String schedule;
    private String remainingtime;

    public Stop(String line, String destination, String schedule, String remainingtime) {
        this.line = line;
        this.destination = destination;
        this.schedule = schedule;
        this.remainingtime = remainingtime;
    }

    public String getLine() {
        return line;
    }

    public void setLine(String line) {
        this.line = line;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getSchedule() {
        return schedule;
    }

    public void setSchedule(String schedule) {
        this.schedule = schedule;
    }

    public String getRemainingtime() {
        return remainingtime;
    }

    public void setRemainingtime(String remainingtime) {
        this.remainingtime = remainingtime;
    }
}
