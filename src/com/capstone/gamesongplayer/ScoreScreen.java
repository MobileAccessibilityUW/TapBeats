package com.capstone.gamesongplayer;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.view.KeyEvent;

/*
 * ScoreScreen
 * The screen that displays score statistics after the user finishes a song in
 * Concert Mode. 
 */
public class ScoreScreen extends Screen {
    
    // spoken information to player
    private String spokentext;
    
    // letter grade for player score
    private String eval;
    
    String screenName;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        
        screenName = "Score"; // for text to speech

        // Get score stats from previous game activity
        int maxCombo = extras.getInt("maxCombo");
        int score = extras.getInt("score");
        int perfectScore = extras.getInt("perfectScore");
        int totalHitNotes = extras.getInt("totalHitNotes");
        int totalNotes = extras.getInt("totalNotes");
        int accuracy = extras.getInt("accuracy");
        eval = extras.getString("eval");
        boolean stage = extras.getBoolean("stage");

        // define next page
        Intent intent;
        if (stage) {
            intent = new Intent(this, Career.class);
        } else {
            intent = new Intent(this, MainScreen.class);
        }
        
        // Create the score stats text
        InformationView tv = new InformationView(this, intent, null, screenName);
        
        String writtenLetter = eval.toUpperCase();
        String spokenLetter = eval.toUpperCase();
        if (spokenLetter.equals("A")) {
            spokenLetter = "ei"; // so it sounds right when read
        }

        String basetext = "Score: " + score + " out of " + (perfectScore * totalNotes) + ".\n" +
        "Max Combo: " + maxCombo + ".\n" +
        "Accuracy: " + totalHitNotes + " out of " + totalNotes + " (" + accuracy + "%)" + ".\n\n" +
        "Double tap screen to return to main menu.";

        // We need two versions of the text: written and spoken.
        String scoretext = "You got a \"" + writtenLetter + "\".\n" + basetext;
        spokentext = "You got a \"" + spokenLetter + "\".\n" + basetext;

        // Display the text
        tv.setTextSize(25);
        tv.setGravity(Gravity.CENTER);
        tv.setText(scoretext);
        setContentView(tv);
    }

    /*
     * onKeyDown()
     * Intercept pressing of the Android Back button and do something.
     * This lets us "control" the history stack.
     */
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // do nothing
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /*
     * onInit()
     * Is called when text to speech finishes initializing.
     * Any speaking that should be done once the screen loads should be
     * placed here.
     */
    @Override
    public void onInit(int status) {  
        tts.stop();
        
        // Have an announcer comment on the player's score
        int evalid = getResources().getIdentifier(eval, "raw", getPackageName());
        tts.addSpeech("eval", getPackageName(), evalid);
        tts.speak("eval", TextToSpeech.QUEUE_FLUSH, null);
        
        // speak out title
        tts.speak(screenName, TextToSpeech.QUEUE_ADD, null);
        
        // speak out score stats
        tts.speak(spokentext, TextToSpeech.QUEUE_ADD, null);
    }
}
