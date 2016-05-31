package com.capstone.gamesongplayer;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.SystemClock;
import android.speech.tts.TextToSpeech;
import android.widget.Toast;

/*
 * StudioBrowseSongView
 * The actual menu for the screen StudioBrowseView.java.
 */
public class StudioBrowseSongView extends MenuView {

    private String songfile;
    private MediaPlayer mpsong = null;
    
    /*
     * Constructor
     * Sets up menu items and draws menu buttons.
     */
    public StudioBrowseSongView(Context context, int width, int height, OptionalTextToSpeech m, Intent iBack, String sName, MediaPlayer songmp, String song)  
    {  
        super(context, m, iBack, sName);
        currentView = 0;
        
        songfile = song;
        mpsong = songmp;
        
        // set color info
        statePressedColor = Color.rgb(113,153,144);
        stateNormalColor = Color.rgb(36,71,53);
        
        menuItems = new ArrayList<String>();
        menuItems.add("Play");
        menuItems.add("Stop");
        menuItems.add("Delete");
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
        
        switch (i) {
        case 0:  
            FileInputStream f;
            try {
                f = context.openFileInput(songfile);
                FileDescriptor fd = f.getFD();
                mpsong.reset();
                mpsong.setDataSource(fd);
                mpsong.prepare();
                mpsong.start();
            } catch (Exception err) {
                // TODO: handle error
                err.printStackTrace();
            }
            break;
        case 1:
            // if a MediaPlayer is already playing, stop it
            if (mpsong != null && mpsong.isPlaying()) {
                mpsong.stop();
                mpsong.reset();
            }
            break;
        case 2:
            context.deleteFile(songfile);
            String songName = songfile.substring(0, 4) + "name" + songfile.substring(4);
            Toast.makeText(context, songName, Toast.LENGTH_LONG).show();
            context.deleteFile(songName);
            tts.speak(songfile + " deleted! Now returning to Studio Browse.", TextToSpeech.QUEUE_FLUSH, null);
            SystemClock.sleep(4000);
            intent = new Intent(context, StudioBrowse.class);
            context.startActivity(intent);
            break;
        }
    }
}
