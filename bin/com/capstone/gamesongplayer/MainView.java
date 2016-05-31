package com.capstone.gamesongplayer;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.speech.tts.TextToSpeech;

/*
 * MainView
 * The actual menu for the screen MainScreen.java.
 */
public class MainView extends MenuView {

    /*
     * Constructor
     * Sets up menu items and draws menu buttons.
     */
    public MainView(Context context, int width, int height, OptionalTextToSpeech m, Intent iBack)  
    {  
        super(context, m, iBack);
        currentView = 0;
        screenName =  "Main Menu";
        
        // set color info
        statePressedColor = Color.rgb(113,153,144);
        stateNormalColor = Color.rgb(36,71,53);
        
        menuItems = new ArrayList<String>();
        menuItems.add("Career");
        menuItems.add("Quick Play");
        menuItems.add("How To Play");
        menuItems.add("Options");
        drawMenu(width, height);
    }

    /*
     * changeScreen()
     * Given the index i of a menu item, change to the corresponding activity.
     */
    protected void changeScreen(int i){
        super.changeScreen(i);
        menuSounds.play(validate, (float)1.0, (float)1.0, 0, 0, (float)1.0); // audible feedback
        Intent intent;
        GlobalVariableStates appState = ((GlobalVariableStates)context.getApplicationContext());
        
        switch (i) {
        case 0: // Concert Mode
        	if(appState.getInstructionState())
        		intent = new Intent(context, InstructionsScreen.class);
        	else
        		intent = new Intent(context, Career.class);
            intent.putExtra("mode", "career");
            context.startActivity(intent);
            break;
        case 1: // Studio Mode
            intent = new Intent(context, QuickPlay.class);
            context.startActivity(intent);
            break;
        case 2: // Practice
        	intent = new Intent(context, HowToPlay.class);   		
        	intent.putExtra("mode", "practice");
            context.startActivity(intent);
            break;
        case 3: // Options
            intent = new Intent(context, Options.class);
            context.startActivity(intent);
            break;
        }
    }
}
