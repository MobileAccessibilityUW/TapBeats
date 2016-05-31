package com.capstone.gamesongplayer;

import com.capstone.gamesongplayer.MainScreen;
import com.capstone.gamesongplayer.QuickPlayView;
import com.capstone.gamesongplayer.R;
import com.capstone.gamesongplayer.Screen;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;

/*
 * QuickPlay
 * The menu screen that contains all the possible gameplay modes, such as 
 * Concert, Free Play, etc.
 */
public class QuickPlay extends Screen {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        screenName = "Quick Play"; // for text to speech
        super.onCreate(savedInstanceState);
        
        // set previous screen
        intentBack = new Intent(QuickPlay.this, MainScreen.class);

        // set menu items
        QuickPlayView view = new QuickPlayView(this, screenWidth, screenHeight, tts, intentBack);
        setContentView(view);
    }
}
