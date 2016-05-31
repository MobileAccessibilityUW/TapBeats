package com.capstone.gamesongplayer;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;

/*
 * StudioMode
 * The menu screen for StudioMode. Choose to browse previously recorded song or
 * record a new song.
 */
public class StudioMode extends Screen {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        screenName = "Studio Menu"; // for text to speech
        super.onCreate(savedInstanceState);

        // set previous screen
        intentBack = new Intent(StudioMode.this, QuickPlay.class);
        
        // set menu items
        StudioView view = new StudioView(this, screenWidth, screenHeight, tts, intentBack, screenName);
        menuSize = view.getMenuSize();
        setContentView(view);
    }
}
