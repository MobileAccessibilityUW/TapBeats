package com.capstone.gamesongplayer;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.KeyEvent;

/*
 * StudioResults
 * The screen after the user finishes recording a song. Here, the
 * user can decide what to do with their recording -- either play it back,
 * save it, redo the recording, or go back to the main menu without doing anything.
 */
public class StudioResults extends Screen {
    
    private MediaPlayer mp = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        screenName = "Studio Results"; // for text to speech
        super.onCreate(savedInstanceState);

        // get song file name and song number, in case user wants to redo their recording
        Bundle e = getIntent().getExtras();
        String songfileName = e.getString("song");
        int songNum = e.getInt("n");
        
        // initialize media player for playing back recording
        mp = new MediaPlayer();
        
        // set menu items
        StudioResultsView view = new StudioResultsView(songfileName, songNum, this, 
                screenWidth, screenHeight, tts, mp, intentBack);
        setContentView(view);
    }
    
    @Override
    public void onPause() {
        super.onPause();
        // stop media player
        if (mp != null && mp.isPlaying()) {
            mp.stop();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // release media player
        if (mp != null) {
            mp.release();
        }
    }

    /*
     * onKeyDown()
     * Intercept pressing of the Android Back button and do something.
     * This lets us "control" the history stack.
     */
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
