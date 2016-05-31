package com.capstone.gamesongplayer;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;

/* 
 * Options
 * The options screen. From here, the user can enable or disable certain
 * things related to the application (for example, whether vibration or
 * text-to-speech is enabled).
 */
public class Options extends Screen {

    public void onCreate(Bundle savedInstanceState) {
        screenName = "Options"; // for text to speech
        super.onCreate(savedInstanceState);
        
        // set previous screen
        intentBack = new Intent(Options.this, MainScreen.class);

        // load menu items
        OptionsView view = new OptionsView(this, screenWidth, screenHeight, tts, intentBack);
        setContentView(view); 
    }
}
