package com.monstrous.canyonracer.gui;

import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.monstrous.canyonracer.LeaderBoard;
import com.monstrous.canyonracer.LeaderBoardEntry;

public class LeaderBoardTable extends Table {

    private Skin skin;
    private LeaderBoard leaderBoard;

    public LeaderBoardTable(String title, Skin skin, LeaderBoard leaderBoard) {
        this.skin = skin;
        this.leaderBoard = leaderBoard;
        rebuild();
    }

    public void rebuild() {

        row();
        for(LeaderBoardEntry entry : leaderBoard.entries){
            //add(new Label(entry.name, skin)).width(30).left();
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
