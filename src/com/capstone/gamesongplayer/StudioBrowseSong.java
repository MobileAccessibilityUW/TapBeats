package com.capstone.gamesongplayer;

import java.util.ArrayList;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;

/* 
 * StudioBrowseSong
 * After the user chooses a song from the Studio Browse menu, this menu allows them to do
 * different things with the song, such as play or delete.
 */
public class StudioBrowseSong extends Screen {
    
    private MediaPlayer mpsong = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        screenName = "Studio Browse Song"; // for text to speech
        super.onCreate(savedInstanceState);
        
        // set previous screen
        intentBack = new Intent(StudioBrowseSong.this, StudioBrowse.class);
        
        // get song index
        Bundle extras = getIntent().getExtras();
        String song = extras.getString("songfile");

        mpsong = new MediaPlayer();
        
        // load menu items
        StudioBrowseSongView view = new StudioBrowseSongView(this, screenWidth, screenHeight, tts, intentBack, screenName, mpsong, song);
        menuSize = view.getMenuSize();
        setContentView(view); 
    }
    
    @Override
    public void onPause() {
        super.onPause();
        // stop media players
        if (mpsong != null && mpsong.isPlaying()) {
            mpsong.stop();
        }
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        // release media players
        if (mpsong != null) {
            mpsong.release();
        }
    }
}
