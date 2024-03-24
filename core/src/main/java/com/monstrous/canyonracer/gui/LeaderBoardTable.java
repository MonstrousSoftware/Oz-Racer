package com.monstrous.canyonracer.gui;

import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.monstrous.canyonracer.LeaderBoard;
import com.monstrous.canyonracer.LeaderBoardEntry;
import com.monstrous.canyonracer.screens.GameScreen;

public class LeaderBoardTable extends Table {

    private Skin skin;
    private GameScreen screen;
    private LeaderBoard leaderBoard;

    public LeaderBoardTable(String title, Skin skin, LeaderBoard leaderBoard) {
        //super(title, skin);
        this.skin = skin;
        this.leaderBoard = leaderBoard;
        rebuild();
    }

    public void rebuild() {

        //add(new Label("Best Times:", skin)).colspan(3).width(250);
        row();
        for(LeaderBoardEntry entry : leaderBoard.entries){
            //add(new Label(entry.name, skin)).width(30).left();
            add(new Label("Attempt #"+ entry.attemptNr, skin)).left();
            if(entry.finished)
                add(new Label(entry.timeString, skin)).pad(5, 20, 0, 0);
            else
                add(new Label("DNF", skin)).pad(5, 20, 0, 0);
            row();
        }
        pack();

    }
}
