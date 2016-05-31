package com.capstone.gamesongplayer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.speech.tts.TextToSpeech;

import java.util.ArrayList;

/*
 * OptionsView
 * The actual menu for the screen Options.java.
 */
public class OptionsView extends MenuView {

    /*
     * Constructor
     * Sets up menu items and draws menu buttons.
     */
    public OptionsView(Context context, int width, int height, OptionalTextToSpeech m, Intent iBack)  
    {  
        super(context, m, iBack);
        screenName = "Options";
        // set color info
        statePressedColor = Color.rgb(255,250,240);
        stateNormalColor = -1;
        
        menuItems = new ArrayList<String>();
        menuItems.add("TTS Option");
        menuItems.add("Vibration Option");
        menuItems.add("Instructions Option");
        menuItems.add("Streamlined Mode");
        drawMenu(width, height);
    }  

    /*
     * setColorForOptionsView()
     * Method for setting the colors for only the options view.
     */
    protected void setColorForOptionsView(Paint p, int button){
    	GlobalVariableStates appState = ((GlobalVariableStates)context.getApplicationContext());
        
    	switch(button){
    	case 0:
    		if(appState.getTTSState())
    			p.setColor(Color.GREEN);//rgb(0,64,17));
    		else if (!appState.getTTSState())
    			p.setColor(Color.rgb(229,27,0));
    		break;
    	case 1:
    		if(appState.getVibrationState())
    			p.setColor(Color.GREEN);
        	else if (!appState.getVibrationState())
        		p.setColor(Color.rgb(229,27,0));
    		break;
    	case 2:
    		if(appState.getInstructionState())
    			p.setColor(Color.GREEN);
        	else if (!appState.getInstructionState())
        		p.setColor(Color.rgb(229,27,0));
    		break;
    	case 3: 
    		if(appState.getStreamLineState())
    			p.setColor(Color.GREEN);
    		else if(!appState.getStreamLineState())
    			p.setColor(Color.rgb(229,27,0));
    		break;
    	}
    }
    
    /*
     * changeScreen()
     * Given the index i of a menu item, set the corresponding option.
     */
    protected void changeScreen(int i){
        super.changeScreen(i);
        menuSounds.play(validate, (float)1.0, (float)1.0, 0, 0, (float)1.0); // audible feedback
        GlobalVariableStates appState = ((GlobalVariableStates)context.getApplicationContext());
        switch (i) {
        case 0:
            appState.setTTSState(!appState.getTTSState());
            break;
        case 1:
            appState.setVibrationState(!appState.getVibrationState());
            break;
        case 2:
        	appState.setInstructionState(!appState.getInstructionState());
            break;
        case 3:
        	if(appState.getStreamLineState())
        		streamLineOff = true;
        	appState.setStreamLineState(!appState.getStreamLineState());
            break;
        }
    }

    /*
     * sayMenuItems()
     * Given the index i of an option, speak out loud instructions for that option.
     */
    protected void sayMenuItems(int i) {
        GlobalVariableStates appState = ((GlobalVariableStates)context.getApplicationContext());
        switch (i)   {
        case 0:
            if(appState.getTTSState()){
                tts.speak("Turn off text to speech for menu selections",TextToSpeech.QUEUE_FLUSH,null);
            } else {
                tts.speak("Turn on text to speech for menu selections",TextToSpeech.QUEUE_FLUSH,null);
            }
            break;
        case 1:
            if(appState.getVibrationState()){
                tts.speak("Turn off vibration for menu selections",TextToSpeech.QUEUE_FLUSH,null);
            } else {
                tts.speak("Turn on vibration for menu selections",TextToSpeech.QUEUE_FLUSH,null);
            }
            break;
        case 2:
        	if(appState.getInstructionState()){
                tts.speak("Turn off instruction screens",TextToSpeech.QUEUE_FLUSH,null);
            } else {
                tts.speak("Turn on instruction screens",TextToSpeech.QUEUE_FLUSH,null);
            }
            break;
        case 3:
        	if(appState.getStreamLineState()){
        		tts.speak("Turn off stream line state for menu selections",TextToSpeech.QUEUE_FLUSH,null);
        	}else{
        		tts.speak("Turn on stream line state for menu selections",TextToSpeech.QUEUE_FLUSH,null);
        	}
        	break;
        }
    }
}
