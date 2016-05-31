package com.capstone.gamesongplayer;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import java.util.Locale;

/* 
 * Screen
 * This is the class all of our program's screens extend.
 * Contains TTS and display set-up, as well as overridden Activity functions
 * common to all screens.
 */
public class Screen extends Activity implements OptionalTextToSpeech.OnInitListener {
    protected OptionalTextToSpeech tts;
    protected int screenWidth;
    protected int screenHeight; 
    protected String screenName = ""; // title if screen that text to speech will read
    
    // define previous page
    protected Intent intentBack = null;
    
    // menu sounds
    protected SoundPool menuSounds = null;
    protected int select = 0;
    protected int validate = 0;
    protected int back = 0;
    protected int negativebeep = 0;
    protected int success = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // initialize text to speech
        tts = new OptionalTextToSpeech(this,
                this  // TextToSpeech.OnInitListener
        );
        tts.setLanguage(Locale.US);
        
        //gets rid of title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        //gets rid of notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                WindowManager.LayoutParams.FLAG_FULLSCREEN );
        
        // load menu sounds
        menuSounds = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
        select = menuSounds.load(this, R.raw.menu_select, 1);
        back = menuSounds.load(this, R.raw.menu_back, 1);
        validate = menuSounds.load(this, R.raw.menu_validate, 1);
        negativebeep = menuSounds.load(this, R.raw.negativebeep, 1);
        success = menuSounds.load(this, R.raw.success, 1);
        
        //gets resolution of screen
        Display display = getWindowManager().getDefaultDisplay(); 
        screenWidth = display.getWidth();
        screenHeight = display.getHeight();
    }
    
    @Override
    public void onPause() {
        super.onPause();
        // Stop text to speech
        if (tts != null) {
            tts.stop();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Shutdown text to speech
        if (tts != null) {
            tts.shutdown();
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
        // speak the name of the screen the user is currently on
        tts.stop();
        tts.speak(screenName, TextToSpeech.QUEUE_FLUSH, null);
    }  
    
    /*
     * onKeyDown()
     * Intercept pressing of the Android Back button and do something.
     * This lets us "control" the history stack.
     */
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && intentBack != null) {
            menuSounds.play(back, (float)1.0, (float)1.0, 0, 0, (float)1.0);
            startActivity(intentBack);
            overridePendingTransition(R.anim.animation_leave, R.anim.animation_enter);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
