package com.capstone.gamesongplayer;

import java.util.ArrayList;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.speech.tts.TextToSpeech;

/*
 * StudioView
 * The actual menu for the screen StudioMode.java.
 */
public class StudioView extends MenuView {
    
    /*
     * Constructor
     * Sets up menu items and draws menu buttons.
     */
    public StudioView(Context context, int width, int height, OptionalTextToSpeech m, Intent iBack, String sName)  
    {  
        super(context, m, iBack, sName);
        // set color info
        statePressedColor = Color.rgb(255,118,49);
        stateNormalColor = Color.rgb(127,50,12);
        
        menuItems = new ArrayList<String>();
        menuItems.add("Browse recorded songs");
        menuItems.add("Record new song");
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
        case 0:
            intent = new Intent(context, StudioBrowse.class);
            intent.putExtra("mode", "studio");
            context.startActivity(intent);
            break;
        case 1:
        	if(appState.getInstructionState()){
        		intent = new Intent(context, InstructionsScreen.class);
        		intent.putExtra("mode", "studio");
        	} else {
        		intent = new Intent(context, SongSelect.class);
        		intent.putExtra("mode", "studio");
        	}
    		context.startActivity(intent);
        	
            break;
        }
    }
}
