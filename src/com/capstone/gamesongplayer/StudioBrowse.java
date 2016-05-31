package com.capstone.gamesongplayer;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.KeyEvent;

/*
 * StudioBrowse
 * Browse songs previously recorded in Studio Mode. Users can play back these songs
 * using the menu.
 */
public class StudioBrowse extends Screen {

    // Media players
    private MediaPlayer mpname = null; // for speaking out song name
    
    private int totalP = 0; // total # of pages

    @Override
    public void onCreate(Bundle savedInstanceState) {
        screenName = "Browse Recorded Songs"; // for text to speech
        super.onCreate(savedInstanceState);
        
        // set previous screen
        intentBack = new Intent(StudioBrowse.this, StudioMode.class);
        
        // get media players from view so we can release them later
        mpname = new MediaPlayer();
        
        // Set menu
        StudioBrowseView view = new StudioBrowseView(this, screenWidth, screenHeight, tts, mpname, intentBack, screenName);
        menuSize = view.getMenuSize();
        setContentView(view); 
        totalP = view.getTotalPages();
    }
    
    @Override
    public void onPause() {
        super.onPause();
        // stop media players
        if (mpname != null && mpname.isPlaying()) {
            mpname.stop();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // release media players
        if (mpname != null) {
            mpname.release();
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
