package com.monstrous.canyonracer;

public class LeaderBoardEntry {
    public String   name;
    public int      attemptNr;
    public boolean  finished;
    public String   timeString;
    public int      time;

    public LeaderBoardEntry(String name, int attemptNr, boolean finished, String timeString, int time) {
        this.name = name;
        this.attemptNr = attemptNr;
        this.finished = finished;
        this.timeString = timeString;
        this.time = time;
    }
}
