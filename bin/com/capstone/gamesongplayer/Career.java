package com.capstone.gamesongplayer;

import com.capstone.gamesongplayer.MainScreen;
import com.capstone.gamesongplayer.QuickPlayView;
import com.capstone.gamesongplayer.R;
import com.capstone.gamesongplayer.Screen;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.KeyEvent;

/*
 * Career
 * The menu screen for Career. Contains missions
 */
public class Career extends Screen {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        screenName = "Career Mode, Stage Selection"; // for text to speech
        super.onCreate(savedInstanceState);
        
        // set previous page
        GlobalVariableStates appState = ((GlobalVariableStates)getApplicationContext());
        if(!appState.getInstructionState()){
            intentBack = new Intent(this, MainScreen.class);
        } else {
            intentBack = new Intent(this, InstructionsScreen.class);
            intentBack.putExtra("mode", "career");
        }

        // set menu items
        CareerView view = new CareerView(this, screenWidth, screenHeight, tts, intentBack);
        setContentView(view);
        
        /*SharedPreferences preferences = getSharedPreferences("CareerVars", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.commit();*/
    }
}
