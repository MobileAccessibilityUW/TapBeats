package com.capstone.gamesongplayer;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.speech.tts.TextToSpeech;

/*
 * StudioBrowseView
 * The actual menu for the screen StudioBrowse.java.
 */
public class StudioBrowseView extends MenuView {

    // for keeping track of menu items
    private String[] flist; // list of song-related files in internal storage
    private int songNum; // number of songs in internal storage
    private int baseSong = 0; // the starting index for the current menu items. Updated when Next Page is pressed.
    private int pageTotal = 1; // total number of pages of items
    private int curPage = 1; // current menu page
    private final int SONGS_PER_PAGE = 3;

    // media players for speaking out song name and playing back songs
    private MediaPlayer mpsong = null;
    private MediaPlayer mpname = null;
    
    // screen dimensions
    private int width;
    private int height;

    /*
     * Constructor
     * Sets up menu items and draws menu buttons.
     */
    public StudioBrowseView(Context context, int w, int h, OptionalTextToSpeech m, MediaPlayer songmp, MediaPlayer namemp, Intent iBack)  
    {  
        super(context, m, iBack);
        screenName = "Browse Recorded Songs";
        // set color info
        statePressedColor = Color.rgb(255,118,49);
        stateNormalColor = Color.rgb(127,50,12);
        
        // Fill in menuItems.
        // This is with internal storage.
        // Save every file available at first, then programmatically rotate through menu options
        // when the user clicks "Next Page".
        flist = context.fileList();
        songNum = flist.length / 2; // half are songs and half are songnames
                                        // this includes the temp "song" and "songname" files (the files without numbers)
        menuItems = new ArrayList<String>();
        
        // get screen dimensions
        width = w;
        height = h;
        
        // fill in menu items
        int initialListCount = Math.min(songNum, SONGS_PER_PAGE);
        for (int i = 0; i < initialListCount; i++) { // SONGS_PER_PAGE songs on the page, unless the total # of songs is less than that
            menuItems.add("song" + i);
        }
        menuItems.add("Next Page"); // last item in menu is to flip to next page
        drawMenu(width, height);
        
        // calculate total number of pages needed
        pageTotal = (int) Math.ceil((double) songNum / SONGS_PER_PAGE);

        // initialize MediaPlayer
        mpname = namemp;
        mpsong = songmp;
    }
    
    /* 
     * getTotalPages()
     * Return the total number of pages for songs.
     */
    public int getTotalPages() {
        return pageTotal;
    }

    /*
     * changeScreen()
     * Given the index i of a menu item, change to the corresponding activity or flip to the
     * next page.
     */
    protected void changeScreen(int i){
        super.changeScreen(i);
        int index = baseSong + i; // the index according to menuItems (not the rectangles)
        
        // if a MediaPlayer is already playing, stop it
        if (mpname != null && mpsong.isPlaying()) {
            mpsong.stop();
            mpsong.reset();
        }
        if (mpname != null && mpname.isPlaying()) {
            mpname.stop();
            mpname.reset();
        }
        
        if (i == (menuItems.size() - 1)) { // last option is Next Page option
            menuSounds.play(select, (float)1.0, (float)1.0, 0, 0, (float)1.0); // audible feedback
            
            // shift songs over to the next page
            baseSong += SONGS_PER_PAGE; 
            // replace previous songs with the next page's songs
            menuItems.clear();
            if (curPage >= pageTotal) {
                // go to first page
                curPage = 1;
                baseSong = 0;
                int initialListCount = Math.min(songNum, SONGS_PER_PAGE);
                for (int j = 0; j < initialListCount; j++) { // SONGS_PER_PAGE songs on the page, unless the total # of songs is less than that
                    menuItems.add("song" + j);
                }
            } else {
                curPage++;
                int songsOnPage = Math.min(songNum - index, SONGS_PER_PAGE);
                for (int j = baseSong; j < baseSong + songsOnPage; j++) {
                    menuItems.add("song" + j);
                }
            }
            menuItems.add("Next Page");
            drawMenu(width, height);
            // tell user where they are
            tts.speak("Page " + curPage + " out of " + pageTotal, TextToSpeech.QUEUE_FLUSH, null);
        } else if (index >= songNum) { // if user selects a song whose index is out of bounds
            // TODO: handle error
            tts.speak("Not yet implemented", TextToSpeech.QUEUE_FLUSH, null);
        } else {
            // if the player selects a song, play back song
            FileInputStream f;
            try {
                f = context.openFileInput("song" + index);
                FileDescriptor fd = f.getFD();
                mpsong.reset();
                mpsong.setDataSource(fd);
                mpsong.prepare();
                mpsong.start();
            } catch (Exception err) {
                // TODO: handle error
                err.printStackTrace();
            }
        }
    }

    /*
     * sayMenuItems()
     * Given the index i of the menu item being interacted with, speak
     * the name of that menu item.
     */
    protected void sayMenuItems(int i) {
        // stop songname player
        if (mpname != null && mpname.isPlaying()) {
            mpname.stop();
            mpname.reset();
        }
        if (i == (menuItems.size() - 1)) {
            tts.speak("Next Page", TextToSpeech.QUEUE_FLUSH, null);	  
        } else if (baseSong + i >= songNum) { // if index is out of bounds
            tts.speak("No song here", TextToSpeech.QUEUE_FLUSH, null);
        } else {
            tts.stop();
            int index = baseSong + i; // find song's overall index (not menu index i)
            FileInputStream f;
            try {
                // speak song's name
                f = context.openFileInput("songname" + index);
                FileDescriptor fd = f.getFD();
                mpname.reset();
                mpname.setDataSource(fd);
                mpname.prepare();
                mpname.start();
            } catch (Exception err) {
                // TODO: handle error
                err.printStackTrace();
            }
        }
    }
    
    /*
     * goBackPage()
     * Moves to the previous menu screen. Where the screen moves is based
     * on the current screen.
     */
    @Override
    protected void goBackPage(){
        menuSounds.play(back, (float)1.0, (float)1.0, 0, 0, (float)1.0);
        if (curPage == 1) {
            context.startActivity(intentBack);
            ((Activity) context).overridePendingTransition(R.anim.animation_leave, R.anim.animation_enter);
        } else {
            // shift songs over to the previous page
            baseSong -= SONGS_PER_PAGE; 
            // replace songs with the previous page's songs
            menuItems.clear();
            
            curPage--;
            for (int j = baseSong; j < baseSong + SONGS_PER_PAGE; j++) {
                menuItems.add("song" + j);
            }
            menuItems.add("Next Page");
            drawMenu(width, height);
            // tell user where they are
            tts.speak("Page " + curPage + " out of " + pageTotal, TextToSpeech.QUEUE_FLUSH, null);
        }
    }
}
