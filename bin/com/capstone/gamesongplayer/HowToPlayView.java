package com.capstone.gamesongplayer;

import java.util.ArrayList;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.speech.tts.TextToSpeech;

/*
 * HowToPlayView
 * The actual menu for the screen HowToPlay.java.
 */
public class HowToPlayView extends MenuView {
    
    /*
     * Constructor
     * Sets up menu items and draws menu buttons.
     */
    public HowToPlayView(Context context, int width, int height, OptionalTextToSpeech m, Intent iBack)  
    {  
        super(context, m, iBack);
        screenName = "How To Play";
        // set color info
        statePressedColor = Color.rgb(165,73,82);
        stateNormalColor = Color.rgb(89,48,32);
        
        menuItems = new ArrayList<String>();
        menuItems.add("Career");
        menuItems.add("Quick Play");
        drawMenu(width, height);
    }  

    /*
     * changeScreen()
     * Given the index i of a menu item, change to the corresponding activity.
     */
    protected void changeScreen(int i){
        super.changeScreen(i);
        menuSounds.play(validate, (float)1.0, (float)1.0, 0, 0, (float)1.0); // audible feedback
        Intent intent = new Intent(context, InstructionsScreen.class);
        switch (i) {
        case 0:
        	intent.putExtra("mode", "howtoplaycareer");
            context.startActivity(intent);
            break;
        case 1:
        	intent.putExtra("mode", "howtoplayquickplay");
    		context.startActivity(intent);
            break;
        }
    }
}
