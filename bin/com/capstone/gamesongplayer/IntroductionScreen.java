package com.capstone.gamesongplayer;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.view.KeyEvent;

/*
 * InstructionsScreen
 * The Instructions Screen. Depending on the given mode, provides
 * the user with instructions for that mode.
 */
public class IntroductionScreen extends Screen {
    
    // information for screen to tell user
    private String information;
    
    // the mode the game is in
    private String mode;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        GlobalVariableStates appState = ((GlobalVariableStates)getApplicationContext());
        
        if (!appState.getInstructionState()) {
            // if instructions are not set, go straight to the main screen
            startActivity(new Intent(this, MainScreen.class));
        }
        
        // define screen title
        screenName = "Welcome to Tap Beats!";
        
        Intent intentNext = new Intent(this, MainScreen.class);
        information = "Keep listening for instructions " +
            		"on how to navigate the menus, or double tap to continue. " +
            		"As you slide your finger around the screen, the phone " +
            		"will speak out the menu items that are available. Then " +
            		"if you double tap, the menu item that was just spoken will " +
            		"be selected. Use two fingers and swipe in any direction to go back a screen. " +
            		"Have fun! Double tap to continue.";
        
        // setup
        super.onCreate(savedInstanceState);
        
        // load interface
        InformationView view = new InformationView(this, intentNext, intentBack, screenName);
        view.setTextSize(25);
        view.setGravity(Gravity.CENTER);
        view.setText(information);
        setContentView(view); 
    }
    
    /*
     * onInit()
     * Is called when text to speech finishes initializing.
     * Any speaking that should be done once the screen loads should be
     * placed here.
     */
    @Override
    public void onInit(int status) {
        super.onInit(status);
        
        // speak instructions to the user
        tts.speak(information, TextToSpeech.QUEUE_ADD, null);
    }
}
