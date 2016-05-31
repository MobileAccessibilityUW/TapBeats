package com.capstone.gamesongplayer;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.view.KeyEvent;

/*
 * InstructionsScreen
 * The Instructions Screen. Depending on the given mode, provides
 * the user with instructions for that mode.
 */
public class InstructionsScreen extends Screen {
    
    // information for screen to tell user
    private String information;
    
    // the mode the game is in
    private String mode;
    
    // whether or not the game is in career mode
    private boolean stage;
    
    // the stage number, if in Career mode
    private int stageNum;
    
    // the screen to go to next
    Intent intentNext;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Bundle extras = getIntent().getExtras();
        mode = extras.getString("mode");
        stage = extras.getBoolean("stage");
        
        // define screen title
        screenName = "Instructions, ";
        if (stage) {
            stageNum = extras.getInt("stageNum");
            screenName = screenName + "Stage " + stageNum;
        } else {
            screenName = screenName + mode + " mode";
        }
        
        // specify screen before and after this one, as well
        // as the information to tell the user, based on mode
        if (stage) {
            intentNext = new Intent(this, player.class);
            intentBack = new Intent(this, Career.class);
            if (stageNum == 1) {
                information = "You need to practice for your upcoming audition with a local band. " + 
                        "On the next screen, practice tapping the buttons in the corners of the screen to play " + 
                        "instrument sounds. Tap at least 20 times to pass this stage. Double tap to continue.";
            } else if (stageNum == 2) {
                information = "For your audition, one of the band members wants to see how well you can keep up " +
                        "with them. He'll play some notes first, then you have to copy him. " + 
                        "Successfully copy him 10 times in a row to pass this stage. Double tap to continue.";
            } else if (stageNum == 3) {
                information = "The band is giving you a chance to perform in their opening song tonight! " +
                        "You and the other drummer will take turns playing during the song. " + 
                        "Finish the song with a B or higher rating to pass this stage. Double tap to continue.";
            }
        } else if (mode.equals("career")) {
            intentNext = new Intent(this, Career.class);
            intentBack = new Intent(this, MainScreen.class);
            information = "You're going to begin your journey to become a rock star! " + 
                    "Play through each Stage to unlock more of your story and achieve your dream! " + 
                    "Double tap to continue.";
        } else if (mode.equals("howtoplaycareer")) {
            intentNext = null;
            intentBack = new Intent(this, HowToPlay.class);
            information = "In Career Mode, you finish each stage, you unlock more stages to play. " +
                    "The order of the stages are meant to help you become familiar with the game.";
        } else if (mode.equals("howtoplayquickplay")) {
            intentNext = null;
            intentBack = new Intent(this, HowToPlay.class);
            information = "Quick Play allows you to play the different modes of gameplay you encounter " + 
                    "in Career Mode. Free Play is easiest and helps you become familiar with the game screen. " + 
                    "Memory Game is second easiest. Concert Mode is most challenging. Studio Mode is an extra " + 
                    "mode that lets you record your own music.";
        } else if (mode.equals("practice")) {
            intentNext = new Intent(this, player.class);
            intentBack = new Intent(this, QuickPlay.class);
            information = "Practice makes perfect! This mode lets you " +
            		"play the buttons in the corners of the screen however you want, without " +
            		"any music. Double tap to continue.";
        } else if (mode.equals("memory")){
        	intentNext = new Intent(this, player.class); 
            intentBack = new Intent(this, QuickPlay.class);
            information = "Through this memory game, get a better sense " +
            		"of how each instrument sounds by listening to a pattern and playing the pattern back. " +
            		"Double tap to continue.";
        } else if (mode.equals("concert")) {
            intentNext = new Intent(this, SongSelect.class);
            intentBack = new Intent(this, QuickPlay.class);
            information = "Can you stay on beat? I'll play a rhythm, " +
            		"then you repeat after me using the buttons in the " +
            		"corners of the screen! Double tap to continue.";
        } else { // mode is studio
            intentNext = new Intent(this, SongSelect.class);
            intentBack = new Intent(this, StudioMode.class);
            intentBack.putExtra("mode", "studio");
            information = "Record your own song! As the music plays, " +
            		"use the buttons in the corners of the screen to " +
            		"play any beat you want! Double tap to continue.";
        }
        if (intentNext != null) {
            intentNext.putExtra("mode", mode);
            if (stage) {
                intentNext.putExtra("stageNum", stageNum);
                intentNext.putExtra("stage", stage);
            }
        }
        
        // setup
        super.onCreate(savedInstanceState);
        
        // load interface
        InformationView view = new InformationView(this, intentNext, intentBack, screenName);
        view.setTextSize(25);
        view.setGravity(Gravity.CENTER);
        view.setText(information);
        setContentView(view); 
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
        
        // speak instructions to the user
        tts.speak(information, TextToSpeech.QUEUE_ADD, null);
    }
}
