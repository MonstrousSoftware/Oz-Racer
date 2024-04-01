package com.monstrous.canyonracer.gui;

import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.monstrous.canyonracer.LeaderBoard;
import com.monstrous.canyonracer.LeaderBoardEntry;

// Shows times of all attempts.
// (Not persistent and not an online leaderboard).

public class LeaderBoardTable extends Table {

    private final Skin skin;
    private final LeaderBoard leaderBoard;

    public LeaderBoardTable(Skin skin, LeaderBoard leaderBoard) {
        this.skin = skin;
        this.leaderBoard = leaderBoard;
        rebuild();
    }

    public void rebuild() {

        row();
        for(LeaderBoardEntry entry : leaderBoard.entries){
            add(new Label("Attempt #"+ entry.attemptNr, skin,"small")).left();
            if(entry.finished)
                add(new Label(entry.timeString, skin, "small")).pad(5, 20, 0, 0);
            else
                add(new Label("DNF", skin, "small")).pad(5, 20, 0, 0);
            row();
        }
        pack();

    }
}
