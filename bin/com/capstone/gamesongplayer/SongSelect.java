package com.capstone.gamesongplayer;

import java.io.IOException;
import java.util.Arrays;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.KeyEvent;

/*
 * SongSelect
 * The song selection screen. Here, the user selects what song they want to play to
 * during Concert Mode or Studio Mode.
 */
public class SongSelect extends Screen {

    private String mode; // Concert mode or Studio mode?
    private int totalP = 0; // total # of pages

    @Override
    public void onCreate(Bundle savedInstanceState) {
        screenName = "Song Selection"; // for text to speech
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();

        // Change SongSelect menu based on the given mode
        mode = extras.getString("mode");
        
        // set previous screen
        GlobalVariableStates appState = ((GlobalVariableStates)getApplicationContext());
        if(appState.getInstructionState()){
            intentBack = new Intent(SongSelect.this, InstructionsScreen.class);
            intentBack.putExtra("mode", mode);
        } else {
            if(mode.equals("concert")) {
                intentBack = new Intent(SongSelect.this, QuickPlay.class);
            } else { // studio mode
                intentBack = new Intent(SongSelect.this, StudioMode.class);
            }
        }

        // load available songs
        // TODO: get ADDITIONAL song files from SD card? Or put everything on SD card?
        try {
            AssetManager mgr = getAssets();
            String[] songlist = mgr.list("songfiles");
            SongFile[] items = new SongFile[songlist.length];
            for (int i = 0; i < songlist.length; i++) {
                SongFile s = new SongFile("songfiles/" + songlist[i], this);
                items[i] = s;
            }
            // sort by title, then difficulty
            Arrays.sort(items);
            
            // load song select menu items
            SongView view = new SongView(mode, items, this, screenWidth, screenHeight, tts, intentBack);
            setContentView(view);
            totalP = view.getTotalPages();
        } catch (IOException e) {
            // TODO: handle errors
        }
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
        // Read out "Page 1 of y"
        tts.speak("Page 1 out of " + totalP, TextToSpeech.QUEUE_ADD, null);
    }  
}
