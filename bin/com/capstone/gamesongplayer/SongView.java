package com.capstone.gamesongplayer;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.speech.tts.TextToSpeech;

/*
 * SongView
 * The actual menu for the screen SongSelect.java.
 */
public class SongView extends MenuView {
    // for keeping track of song items
    private static SongFile[] songs;
    private int baseSong = 0; // the starting index for the current menu items. Updated when Next Page is pressed.
    private int pageTotal = 1; // total number of pages of items
    private int curPage = 1; // current menu page
    private final int SONGS_PER_PAGE = 3; // number of songs to display on each page
    
    // screen dimensions
    private int width;
    private int height;
    
    /*
     * Constructor
     * Sets up menu items and draws menu buttons.
     */
    public SongView(String mode, SongFile[] items, Context context, int w, int h, OptionalTextToSpeech m, Intent iBack)  
    {  
        super(context, m, iBack);
        screenName = "Song Selection";
        // get mode and full song list from parent activity
        modeName = mode;
        songs = items;
        menuItems = new ArrayList<String>();
        
        // set color info
        if (modeName.equals("concert")) {
            statePressedColor = Color.rgb(145,134,111);
            stateNormalColor = Color.rgb(66,49,35);
        } else {
            statePressedColor = Color.rgb(255,118,49);
            stateNormalColor = Color.rgb(127,50,12);
        }
        
        // get screen dimensions
        width = w;
        height = h;
        
        // fill in menu item names
        int initialListCount = Math.min(songs.length, SONGS_PER_PAGE);
        for (int i = 0; i < initialListCount; i++) { // SONGS_PER_PAGE songs on the page, unless the total # of songs is less than that
            menuItems.add(songs[i].getTitle() + ", " + songs[i].getDifficulty());
        }
        menuItems.add("Next Page"); // last item in menu is to flip to next page
        drawMenu(width, height);

        // calculate total number of pages needed
        pageTotal = (int) Math.ceil((double) songs.length / SONGS_PER_PAGE);
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
        
        if (i == (menuItems.size() - 1)) { // Next Page option
            menuSounds.play(select, (float)1.0, (float)1.0, 0, 0, (float)1.0); // audible feedback
            
            // shift songs over to the next page
            baseSong += SONGS_PER_PAGE; 
            // replace previous songs with the next page's songs
            menuItems.clear();
            if (curPage >= pageTotal) {
                curPage = 1;
                baseSong = 0;
                // first page of songs
                int initialListCount = Math.min(songs.length, SONGS_PER_PAGE);
                for (int j = 0; j < initialListCount; j++) { // SONGS_PER_PAGE songs on the page, unless the total # of songs is less than that
                    menuItems.add(songs[j].getTitle() + ", " + songs[j].getDifficulty());
                }
            } else {
                curPage++;
                int songsOnPage = Math.min(songs.length - index, SONGS_PER_PAGE);
                for (int j = baseSong; j < baseSong + songsOnPage; j++) {
                    menuItems.add(songs[j].getTitle() + ", " + songs[j].getDifficulty());
                }
            }
            menuItems.add("Next Page");
            drawMenu(width, height);
            // tell user where they are
            tts.speak("Page " + curPage + " out of " + pageTotal, TextToSpeech.QUEUE_FLUSH, null);
        } else if (index >= songs.length) { // if user selects a song whose index is out of bounds
            // TODO: handle error
            tts.speak("Not yet implemented", TextToSpeech.QUEUE_FLUSH, null);
        } else {
            // if the player selects a song, move to game mode
            Intent intent = new Intent(context, player.class);
            intent.putExtra("song", songs[index].getSongFileName());
            intent.putExtra("mode", modeName);
            menuSounds.play(validate, (float)1.0, (float)1.0, 0, 0, (float)1.0); // audible feedback
            context.startActivity(intent);
        }
    }

    /*
     * sayMenuItems()
     * Given the index i of the menu item being interacted with, speak
     * the name of that menu item.
     */
    protected void sayMenuItems(int i) {
        if (i == (menuItems.size() - 1)) {
            tts.speak("Next Page", TextToSpeech.QUEUE_FLUSH, null);	  
        } else if (baseSong + i >= songs.length) { // if index is out of bounds
            tts.speak("No song here", TextToSpeech.QUEUE_FLUSH, null);
        } else { // speak song's name
            int index = baseSong + i;
            tts.speak(songs[index].getTitle() + ", " + songs[index].getDifficulty(),TextToSpeech.QUEUE_FLUSH,null);
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
