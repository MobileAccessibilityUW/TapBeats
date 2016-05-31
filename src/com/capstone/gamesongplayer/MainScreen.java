package com.capstone.gamesongplayer;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;

/* 
 * MainScreen
 * The main menu screen. From here, the user can access a tutorial, the options, 
 * the main gameplay mode and studio mode.
 */
public class MainScreen extends Screen {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        screenName = "Main Menu"; // for text to speech
        super.onCreate(savedInstanceState);
        
        // set previous screen
        GlobalVariableStates appState = ((GlobalVariableStates)getApplicationContext());
        if(appState.getInstructionState()) {
            intentBack = new Intent(this, IntroductionScreen.class);
        }
        
        // load menu items
        MainView view = new MainView(this, screenWidth, screenHeight, tts, intentBack, screenName);
        menuSize = view.getMenuSize();
        setContentView(view); 
    }
}
