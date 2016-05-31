package com.capstone.gamesongplayer;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.speech.tts.TextToSpeech;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.util.ArrayList;

/*
 * StudioResultsView
 * The actual menu for the screen StudioResults.java.
 */
public class StudioResultsView extends MenuView {
    
    // media players for playing back recorded song
    MediaPlayer mp = null;
    
    private String songfileName; // name of song file
    private int songNum; // # assigned to recorded song

    /*
     * Constructor
     * Sets up menu items and draws menu buttons.
     */
    public StudioResultsView(String songname, int n, Context context, int width, int height, OptionalTextToSpeech m, MediaPlayer player, Intent iBack)  
    {  
        super(context, m, iBack);
        screenName = "Studio Results";
        
        // get name of song file and assigned number from parent activity
        songfileName = songname;
        songNum = n;
        
        mp = player;
        
        menuItems = new ArrayList<String>();
        menuItems.add("Playback Recording");
        menuItems.add("Save Recording");
        menuItems.add("Redo Recording");
        menuItems.add("Back to Main Menu");
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
        
        // stop song playback if needed
        if (mp != null && mp.isPlaying()) {
            mp.stop();
        }
        
        switch (i) {
            case 0: // Replay
                FileInputStream f;
                try {
                    f = context.openFileInput("song" + songNum);
                    FileDescriptor fd = f.getFD();
                    f.read();
                    // initialize MediaPlayer for playing back recording
                    mp.reset();
                    mp.setDataSource(fd);
                    mp.prepare();
                    mp.start();
                } catch (Exception err) {
                    // TODO: handle error
                    err.printStackTrace();
                }
                break;
            case 1: // Save
                intent = new Intent(context, StudioSave.class);
                intent.putExtra("mode", "studio");
                intent.putExtra("n", songNum);
                context.startActivity(intent);
                break;
            case 2: // Redo
                intent = new Intent(context, player.class);
                intent.putExtra("mode", "studio");
                intent.putExtra("song", songfileName);
                context.startActivity(intent);
                break;
            case 3: // Back to Main Menu
                intent = new Intent(context, MainScreen.class);
                context.startActivity(intent);
                break;
        }
    }
}
