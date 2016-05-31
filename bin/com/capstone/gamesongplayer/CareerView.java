package com.capstone.gamesongplayer;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.speech.tts.TextToSpeech;

/*
 * CareerView
 * The actual menu for the screen Career.java.
 */
public class CareerView extends MenuView {
    // for keeping track of stages
    private static int STAGE_COUNT = 3;
    private int unlockedStages = 1;
    private int baseStage = 0; // the starting index for the current menu items. Updated when Next Page is pressed.
    private int pageTotal = 1; // total number of pages of items
    private int curPage = 1; // current menu page
    private final int STAGES_PER_PAGE = 3; // number of songs to display on each page
    
    // screen dimensions
    private int width;
    private int height;
    
    // preferences
    SharedPreferences preferences = null;
    
    /*
     * Constructor
     * Sets up menu items and draws menu buttons.
     */
    public CareerView(Context context, int w, int h, OptionalTextToSpeech m, Intent iBack)  
    {  
        super(context, m, iBack);
        screenName = "Career Mode, Stage Selection";
        // set color info
        statePressedColor = Color.rgb(145,134,111);
        stateNormalColor = Color.rgb(66,49,35);
        
        preferences = context.getSharedPreferences("CareerVars", Context.MODE_PRIVATE);
        unlockedStages = preferences.getInt("unlocked", 1);
        
        menuItems = new ArrayList<String>();
        
        // get screen dimensions
        width = w;
        height = h;
        
        // fill in menu item names
        int initialListCount = Math.min(STAGE_COUNT, STAGES_PER_PAGE);
        for (int i = 0; i < initialListCount; i++) { // STAGES_PER_PAGE stages on the page, unless the total # of stages is less than that
            if ((i + 1) <= unlockedStages) {
                menuItems.add("Stage " + (i + 1));
            } else {
                menuItems.add("Stage Locked");
            }
        }
        menuItems.add("Next Page"); // last item in menu is to flip to next page
        drawMenu(width, height);

        // calculate total number of pages needed
        pageTotal = (int) Math.ceil((double) STAGE_COUNT / STAGES_PER_PAGE);
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
        int index = baseStage + i; // the index according to menuItems (not the rectangles)
        
        if (i == (menuItems.size() - 1)) { // Next Page option
            menuSounds.play(select, (float)1.0, (float)1.0, 0, 0, (float)1.0); // audible feedback
            
            // shift songs over to the next page
            baseStage += STAGES_PER_PAGE; 
            // replace previous songs with the next page's songs
            menuItems.clear();
            if (curPage >= pageTotal) {
                curPage = 1;
                baseStage = 0;
                // first page of songs
                int initialListCount = Math.min(STAGE_COUNT, STAGES_PER_PAGE);
                for (int j = 0; j < initialListCount; j++) { // STAGES_PER_PAGE stages on the page, unless the total # of stages is less than that
                    if ((j + 1) <= unlockedStages) {
                        menuItems.add("Stage " + (j + 1));
                    } else {
                        menuItems.add("Stage Locked");
                    }
                }
            } else {
                curPage++;
                int stagesOnPage = Math.min(STAGE_COUNT - index, STAGES_PER_PAGE);
                for (int j = baseStage; j < baseStage + stagesOnPage; j++) {
                    if ((j + 1) <= unlockedStages) {
                        menuItems.add("Stage " + (j + 1));
                    } else {
                        menuItems.add("Stage Locked");
                    }
                }
            }
            menuItems.add("Next Page");
            drawMenu(width, height);
            // tell user where they are
            tts.speak("Page " + curPage + " out of " + pageTotal, TextToSpeech.QUEUE_FLUSH, null);
        } else if (index >= STAGE_COUNT) { // if user selects a stage whose index is out of bounds
            // TODO: handle error
            tts.speak("Not yet implemented", TextToSpeech.QUEUE_FLUSH, null);
        } else {
            // if the player selects a stage, move to stage instructions
            Intent intent = new Intent(context, InstructionsScreen.class);
            
            if (index < unlockedStages) {
                intent.putExtra("stage", true);
                intent.putExtra("stageNum", index + 1);
                if (index + 1 == 1) {
                    intent.putExtra("mode", "practice");
                } else if (index + 1 == 2) {
                    intent.putExtra("mode", "memory");
                } else {
                    intent.putExtra("mode", "concert");
                }
                menuSounds.play(validate, (float)1.0, (float)1.0, 0, 0, (float)1.0); // audible feedback
                context.startActivity(intent);
            } else {
                menuSounds.play(negativebeep, (float)1.0, (float)1.0, 0, 0, (float)1.0);
            }
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
        } else if (baseStage + i >= STAGE_COUNT) { // if index is out of bounds
            tts.speak("No stage here", TextToSpeech.QUEUE_FLUSH, null);
        } else { // speak stages's name
            int index = baseStage + i;
            if ((index + 1) <= unlockedStages) {
                tts.speak("Stage " + (index + 1),TextToSpeech.QUEUE_FLUSH,null);
            } else {
                tts.speak("Stage Locked", TextToSpeech.QUEUE_FLUSH, null);
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
            baseStage -= STAGES_PER_PAGE; 
            // replace songs with the previous page's songs
            menuItems.clear();
            
            curPage--;
            for (int j = baseStage; j < baseStage + STAGES_PER_PAGE; j++) {
                if ((j + 1) <= unlockedStages) {
                    menuItems.add("Stage " + (j + 1));
                } else {
                    menuItems.add("Stage Locked");
                }
            }
            menuItems.add("Next Page");
            drawMenu(width, height);
            // tell user where they are
            tts.speak("Page " + curPage + " out of " + pageTotal, TextToSpeech.QUEUE_FLUSH, null);
        }
    }
}
