package com.capstone.gamesongplayer;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.speech.tts.TextToSpeech;

/*
 * QuickPlayView
 * The actual menu for the screen QuickPlay.java.
 */
public class QuickPlayView extends MenuView {

    /*
     * Constructor
     * Sets up menu items and draws menu buttons.
     */
    public QuickPlayView(Context context, int width, int height, OptionalTextToSpeech m, Intent iBack, String sName)  
    {  
        super(context, m, iBack, sName);
        
        // set color info
        statePressedColor = Color.rgb(165,73,82);
        stateNormalColor = Color.rgb(89,48,32);
        
        menuItems = new ArrayList<String>();
        menuItems.add("Free Play");
        menuItems.add("Memory Mode");
        menuItems.add("Concert Mode");
        menuItems.add("Studio Mode");
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
        case 0: // Free Play
        	if(appState.getInstructionState())
        		intent = new Intent(context, InstructionsScreen.class);
        	else
        		intent = new Intent(context, player.class);
            intent.putExtra("mode", "practice");
            context.startActivity(intent);
            break;
        case 1: // Memory Game
        	if(appState.getInstructionState()){
        		intent = new Intent(context, InstructionsScreen.class);
        	} else {
        		intent = new Intent(context, player.class);
        	}
            intent.putExtra("mode", "memory");
            context.startActivity(intent);
            break;
        case 2: // Concert Mode
        	if(appState.getInstructionState()){
        		intent = new Intent(context, InstructionsScreen.class);
        	} else {
        		intent = new Intent(context, SongSelect.class);
        	}   		
        	intent.putExtra("mode", "concert");
            context.startActivity(intent);
            break;
        case 3: // Studio Mode
	        intent = new Intent(context, StudioMode.class);
        	intent.putExtra("mode", "studio");
            context.startActivity(intent);
            break;
        }
    }
}
