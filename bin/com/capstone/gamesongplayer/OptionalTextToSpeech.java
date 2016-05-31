package com.capstone.gamesongplayer;

import java.util.HashMap;
import android.content.Context;
import android.speech.tts.TextToSpeech;

/*
 * OptionalTextToSpeech
 * A wrapper class for TextToSpeech that checks if the user has allowed TTS before speaking.
 */
public class OptionalTextToSpeech extends TextToSpeech {
    
    private Context parentC;

    public OptionalTextToSpeech(Context context, OnInitListener listener) {
        super(context, listener);
        parentC = context;
    }
    
    @Override
    public int speak(String text, int queueMode, HashMap<String, String> params) {
        GlobalVariableStates appState = ((GlobalVariableStates)parentC.getApplicationContext());
        if(appState.getTTSState())
            return super.speak(text, queueMode, params);
        return 0;
    }
    
}
