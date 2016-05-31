package com.capstone.gamesongplayer;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.speech.tts.TextToSpeech;

/*
 * GlobalVariableStates
 * This class keeps track of whether or not text to speech and vibration are
 * enabled in the application.
 */
public class GlobalVariableStates extends Application {

    // preferences
    SharedPreferences preferences = null;
    
    /* Overall preferences */
    private boolean getPreferences(String what, boolean def) {
        preferences = getSharedPreferences("GameVars", Context.MODE_PRIVATE);
        return preferences.getBoolean(what, def);
    }
    
    private void setPreferences(String what, boolean val) {
        preferences = getSharedPreferences("GameVars", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(what, val);
        editor.commit();
    }

    /*
     * Text to Speech
     */
    public boolean getTTSState(){
        return getPreferences("TTSstate", true);
    }
    public void setTTSState(boolean stateChange){
        setPreferences("TTSstate", stateChange);
    }

    /*
     * Vibration
     */
    public boolean getVibrationState(){
        return getPreferences("VibrationState", true);
    }
    public void setVibrationState(boolean stateChange){
        setPreferences("VibrationState", stateChange);
    }
    
    /*
     * Streamlined Mode
     * Turning this mode on enables the user to use the regular Android interface
     * method, instead of Slide Rule.
     */
    public boolean getStreamLineState(){
        return getPreferences("StreamLineState", false);
    }
    public void setStreamLineState(boolean stateChange){
    	setPreferences("StreamLineState", stateChange);
    }
    
    /*
     * Instructions
     */
    public boolean getInstructionState(){
        return getPreferences("InstructionState", true);
    }
    public void setInstructionState(boolean stateChange){
        setPreferences("InstructionState", stateChange);
    }
}
