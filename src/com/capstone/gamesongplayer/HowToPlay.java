package com.capstone.gamesongplayer;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;

/*
 * HowToPlay
 * The menu screen for the HowToPlay screens. 
 * Learn how to play Career and QuickPlay.
 */
public class HowToPlay extends Screen {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        screenName = "How To Play"; // for text to speech
        super.onCreate(savedInstanceState);
        
        // set previous page
        intentBack = new Intent(this, MainScreen.class);

        // set menu items
        HowToPlayView view = new HowToPlayView(this, screenWidth, screenHeight, tts, intentBack, screenName);
        menuSize = view.getMenuSize();
        setContentView(view);
    }
}
