package com.monstrous.canyonracer;

import com.badlogic.gdx.utils.Array;

import java.util.Comparator;

public class LeaderBoard {

    private int maxEntries;
    public Array<LeaderBoardEntry> entries;

    static class EntryComparator implements Comparator<LeaderBoardEntry> {

        @Override
        public int compare(LeaderBoardEntry o1, LeaderBoardEntry o2) {
            return o1.time - o2.time;
        }
    }

    public LeaderBoard(int maxEntries) {
        this.maxEntries = maxEntries;
        entries = new Array<>();
    }

    public void add(String name, int attemptNr, boolean finished, String timeString, int time){
        LeaderBoardEntry entry = new LeaderBoardEntry(name, attemptNr, finished, timeString, time);
        entries.add(entry);
        //entries.sort( new EntryComparator() );
        while(entries.size > maxEntries)
            entries.removeIndex(0);
    }
}
