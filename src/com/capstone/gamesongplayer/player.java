package com.capstone.gamesongplayer;

import java.io.*;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.MotionEvent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.Paint.Style;

/*
 * Player
 * This is the main gameplay screen, where the user presses buttons to play instruments
 * along with a song.
 * The player class represents the activity, and the GameView sub-class draws the actual buttons
 * and provides their functionality.
 */
public class player extends Activity implements OptionalTextToSpeech.OnInitListener {

    // for scoring
    private static final int PERFECT_THRESHOLD = 200; // in ms; player needs to be within this to get Perfect
    private static final int PERFECT_SCORE = 300; // points earned for getting a Perfect
    private static final int EXCELLENT_THRESHOLD = 350; // in ms; player needs to be within this to get Excellent
    private static final int EXCELLENT_SCORE = 100; // points earned for getting an Excellent
    private int score = 0; // initial score

    // for keeping track of Combo information
    private int curCombo = 0; // current streak
    private int maxCombo = 0; // maximum streak for this game

    // keep track of pause time
    private long pauseTime = 0;
    private long pauseStart;

    private boolean gameIsCurrentlyPaused;
    
    private Intent intentBack; // previous page
    
    // initialize stuff to keep track of notes
    private ArrayList<int[]> songNotesArray = null; // notes for player to press
    private ArrayList<int[]> playNotesArray = null; // notes for player to listen to
    private static final int WINDOW_SIZE = 5; // the size of the score slot window
    private int windowStart = 0;
    private int windowEnd = WINDOW_SIZE;

    private long baseTime; // time the song starts playing
    private long curTime = 0; // time the player presses an instrument
    
    private boolean pausePlayNotes = false; // use to stop playing of notes in AsyncTask

    // text to speech
    private OptionalTextToSpeech tts;

    // song player
    private MediaPlayer songPlayer = null;

    // SongFile representing the song that will be played
    private SongFile s = null;
    
    // to keep track of simon says notes in Memory Mode
    private ArrayList<Integer> memoryNotesArray = null;
    private int currentMemoryPresses = 0; // what level of simon says play the player is on
    private boolean disableButtonPresses = false;
    private boolean playerTurn = false;

    // instruments and sound effects
    private SoundPool instrumentSounds = null;
    private int soundOne = 0;
    private int soundTwo = 0;
    private int soundThree = 0;
    private int soundFour = 0;
    private int soundError = 0;
    private int crowdRoar = 0;
    // streamIDs for instruments and sound effects
    private int streamOne = 0;
    private int streamTwo = 0;
    private int streamThree = 0;
    private int streamFour = 0;
    private int streamError = 0;
    private int streamCrowd = 0;
    
    // menu sounds
    protected SoundPool menuSounds = null;
    protected int select = 0;
    protected int validate = 0;
    protected int back = 0;
    protected int negativebeep = 0;
    protected int success = 0;

    // vibrator
    private OptionalVibrator v = null;

    private String mode; // which mode is being played (Concert or Studio)

    // for recording in studio mode
    private MediaRecorder recorder = null;
    private ContentValues cv = null;
    private String songfileName; // name of the song file
    private int songNum = 0; // if this is a recorded song to be saved, the number to assign to it to give it a unique filename

    // dimensions of screen
    private int height;
    private int width;
    
    // variables to keep track of career stage progress
    private boolean stage = false; // true if in Career Mode
    private int stageNum = 0;
    // STAGE 1
    private int stage1Presses = 0;
    // STAGE 2
    private int stage2Rounds = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // initialize text to speech
        tts = new OptionalTextToSpeech(this,
                this  // TextToSpeech.OnInitListener
        );

        // gets rid of title
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        // gets rid of notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                WindowManager.LayoutParams.FLAG_FULLSCREEN );

        // gets resolution of screen
        Display display = getWindowManager().getDefaultDisplay(); 
        width = display.getWidth();
        height = display.getHeight();

        // set the view
        GameView view = new GameView(this, width, height);
        setContentView(view); 

        // initialize vibrator
        v = new OptionalVibrator(this);

        // initialize instruments and sound effects
        instrumentSounds = new SoundPool(6, AudioManager.STREAM_MUSIC, 0);
        soundOne = instrumentSounds.load(this, R.raw.hihat, 1);
        soundTwo = instrumentSounds.load(this, R.raw.cymbal, 1);
        soundThree = instrumentSounds.load(this, R.raw.snare, 1);
        soundFour = instrumentSounds.load(this, R.raw.kickdrum, 1);
        soundError = instrumentSounds.load(this, R.raw.negativebeep, 1);
        crowdRoar = instrumentSounds.load(this, R.raw.crowdroar, 1);
        
        // load menu sounds
        menuSounds = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
        select = menuSounds.load(this, R.raw.menu_select, 1);
        back = menuSounds.load(this, R.raw.menu_back, 1);
        validate = menuSounds.load(this, R.raw.menu_validate, 1);
        negativebeep = menuSounds.load(this, R.raw.negativebeep, 1);
        success = menuSounds.load(this, R.raw.success, 1);

        // get mode
        Bundle extras = getIntent().getExtras();
        mode = extras.getString("mode");
        stage = extras.getBoolean("stage");
        if (stage) {
            stageNum = extras.getInt("stageNum");
        }
        
        // set previous page
        GlobalVariableStates appState = ((GlobalVariableStates)getApplicationContext());
        if (stage) {
            if(appState.getInstructionState())
                intentBack = new Intent(this, InstructionsScreen.class);
            else
                intentBack = new Intent(this, Career.class);
        } else if (mode.equals("practice") || mode.equals("memory")) {
            if(appState.getInstructionState())
                intentBack = new Intent(this, InstructionsScreen.class);
            else
                intentBack = new Intent(this, QuickPlay.class);
        } else {
            intentBack = new Intent(this, SongSelect.class);
        }
        intentBack.putExtra("mode", mode);
        if (stage) {
            intentBack.putExtra("stageNum", stageNum);
            intentBack.putExtra("stage", stage);
        }

        if (mode.equals("memory")) {
            memoryNotesArray = new ArrayList<Integer>();
        } else if (!mode.equals("practice")) { // if it's not practice mode, load a song
            try {
                // load songfile
                if (stage) {
                    if (stageNum == 3)
                        songfileName = "songfiles/classic_rock_easy_short.ss";
                } else {
                    songfileName = extras.getString("song");
                }
                s = new SongFile(songfileName, this);

                // initialize song notes
                s.parseSongNotes();
                songNotesArray = s.getNotesArray();
                s.parsePlayNotes();
                playNotesArray = s.getPlayArray();
            } catch (IOException e) {
                // TODO: handle errors
                e.printStackTrace();
            }

            // initialize song player
            String songName = s.getMusicFileName();
            
            // load song media file
            int songid = getResources().getIdentifier(songName, "raw", getPackageName());
            songPlayer = MediaPlayer.create(this, songid);
            songPlayer.setOnCompletionListener(new myCompletionListener()); // do something when song finishes

            // if in studio mode, start recorder
            if (mode.equals("studio")) {
                recorder = new MediaRecorder();
                cv = new ContentValues(3);
                cv.put(MediaStore.MediaColumns.TITLE, "Song");
                cv.put(MediaStore.MediaColumns.DATE_ADDED, System.currentTimeMillis());
                cv.put(MediaStore.MediaColumns.MIME_TYPE, "audio/mpeg");

                try {
                    String[] fList = fileList();
                    songNum = fList.length / 2; // half of the files are songs and half are songnames
                    // songNum = the # of song-songname pairs

                    FileOutputStream f = openFileOutput("song" + songNum, Context.MODE_PRIVATE);
                    FileDescriptor path = f.getFD();
                    recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                    recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                    recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                    recorder.setOutputFile(path);
                    recorder.prepare();
                    recorder.start();
                } catch (IOException e) {
                    // TODO: handle errors
                    e.printStackTrace();
                }
            }
        }
        // TODO: create onErrorListener for MediaPlayer
    }

    @Override
    public void onRestart() {
        super.onRestart();
        onCreate(null); // on restart, do same thing as onCreate
    }

    @Override
    public void onStart() {
        super.onStart();
        // if it's not practice mode, start the song and scoring
        if (!mode.equals("practice") && !mode.equals("memory")) {
            songPlayer.start();
            baseTime = System.currentTimeMillis();
        }
        if (!mode.equals("practice")) {
            // initialize game notes player
            PlayerHelperTask playerTask = new PlayerHelperTask();
            playerTask.execute(this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        tts.stop();
        instrumentSounds.release();
        releaseSongPlayer();
        v.cancel();
        if (recorder != null && mode.equals("studio")) {
            recorder.stop();
            recorder.release();
            recorder = null;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (tts != null) {
            tts.shutdown(); // DO NOT REMOVE THIS
            // needed to make StudioSave work properly, or else the TTS during Studio Song Naming gets cut off
        }
        instrumentSounds.release();
        releaseSongPlayer();
        v.cancel();
        if (recorder != null && mode.equals("studio")) {
            recorder.stop();
            recorder.release();
            recorder = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Shutdown text to speech
        if (tts != null) {
            tts.shutdown();
        }
    }

    /*
     * releaseSongPlayer()
     * Stop the MediaPlayer and release its resources.
     */
    public void releaseSongPlayer() {
        if (songPlayer != null) {
            if (songPlayer.isPlaying()) {
                songPlayer.stop();
            }
            songPlayer.release();
            songPlayer = null;
        }
    }

    /*
     * playSound()
     * Given an integer note, play the corresponding instrument or sound effect.
     * Also, give haptic feedback.
     */
    public void playSound(int note) throws IllegalStateException {
        v.vibrate(40);
        switch (note) {
        case 0: 
            break;
        case 1:
            streamOne = instrumentSounds.play(soundOne, (float)1.0, (float)1.0, 0, 0, (float)1.0);
            break;
        case 2:
            streamTwo = instrumentSounds.play(soundTwo, (float)1.0, (float)1.0, 0, 0, (float)1.0);
            break;
        case 3:
            streamThree = instrumentSounds.play(soundThree, (float)1.0, (float)1.0, 0, 0, (float)1.0);
            break;
        case 4:
            streamFour = instrumentSounds.play(soundFour, (float)1.0, (float)1.0, 0, 0, (float)1.0);
            break;
        case 5:
            streamError = instrumentSounds.play(soundError, (float)1.0, (float)1.0, 0, 0, (float)1.0);
            break;
        case 6:
            streamCrowd = instrumentSounds.play(crowdRoar, (float)1.0, (float)1.0, 0, 0, (float)1.0);
            break;
        }
    }

    /* 
     * playNotes()
     * Automatically plays the song as it's supposed to be played by the player.
     * Useful for a demo mode.
     */
    public void playNotes() {
        ArrayList<int[]> notes = s.getNotesArray();

        // play notes. This for loop processes one line at a time.
        int nextTime = 0;
        int interval = 0;
        int curTime = notes.get(0)[1];
        SystemClock.sleep(curTime); // wait until it's time to play the next note
        playSound(notes.get(1)[0]);

        for (int i = 1; i < notes.size(); i++) {
            nextTime = notes.get(i)[1];
            interval = nextTime - curTime;
            SystemClock.sleep(interval);
            playSound(notes.get(i)[0]);	
            curTime = nextTime;

        }
    }
    
    /* 
     * addNewMemoryNote()
     * Randomly picks the next note for the player to memorize in Memory Mode.
     */
    public void addNewMemoryNote() {
        Random randomGenerator = new Random();
        int randomInt = randomGenerator.nextInt(4); // generate a # between 0 and 3.
        randomInt++; // add one to make it between 1 and 4.
        memoryNotesArray.add(randomInt);
    }
    
    /*
     * playMemoryRound()
     * Play all memory game notes so far.
     */
    public void playMemoryRound() {
        disableButtonPresses = true; // user cannot press anything while sound is going
        addNewMemoryNote();
        for (int i = 0; i < memoryNotesArray.size(); i++) {
            pausableSleep(500);
            playSound(memoryNotesArray.get(i));
        }
        // enable button presses so user can mimic
        disableButtonPresses = false;
        playerTurn = true;
    }

    /*
     * getTotalHitNotes()
     * Returns the number of notes correctly played by the player.
     */
    public int getTotalHitNotes() {
        int count = 0;
        for (int i = 0; i < songNotesArray.size(); i++) {
            if (songNotesArray.get(i)[2] == 1) {
                count++;
            }
        }
        return count;
    }

    /*
     * scoreAndPlay()
     * This function scores the player's note when they press a button for an instrument,
     * by taking into account how far off they were from the target time for that note.
     * This function also plays the instrument sound for the user.
     */
    public boolean scoreAndPlay(int instrument) {

        // hit indicates whether or not the note played by the user was correct
        boolean hit = false;

        if (!songPlayer.isPlaying()) {
            // mark how long the song has been paused
            pauseTime += (System.currentTimeMillis() - pauseStart);
        }

        // calculate net time since song started (song runtime excluding pause time)
        curTime = (System.currentTimeMillis() - baseTime) - pauseTime;

        // move window until current time is contained within it
        while ((songNotesArray.size() > windowEnd) && songNotesArray.get(windowEnd)[1] + PERFECT_THRESHOLD < curTime) {
            windowStart += 1;
            windowEnd += 1;
        }

        // check to make sure window isn't out of the array, and adjust if so
        if (songNotesArray.size() <= windowEnd) {
            windowEnd = songNotesArray.size() - 1; // last index in array
            windowStart = windowEnd - WINDOW_SIZE;
        }
        long diff = -1; // the time difference between the user pressing the note and the correct time

        // for the width of the window
        for (int i = windowStart; i <= windowEnd; i++) {
            // calculate the difference between the time the player played the note and the "correct" time
            // of the note currently being looked at
            diff = Math.abs(songNotesArray.get(i)[1] - curTime);
            // if we haven't found the correct note yet, and the player got a PERFECT
            if (!hit && diff < PERFECT_THRESHOLD && songNotesArray.get(i)[2] == 0 && (songNotesArray.get(i)[0] == instrument)) { // if the right note hasn't been hit yet
                hit = true;
                // close enough! score
                score += PERFECT_SCORE;
                int[] t = songNotesArray.get(i);
                t[2] = 1;
                songNotesArray.set(i, t); // mark note as hit
                if (i != 0 && songNotesArray.get(i-1)[2] == 0) { // if the entry before it wasn't hit
                    curCombo = 0; // start combo over
                }
                curCombo++;
                // otherwise, if we haven't found the correct note yet, and the player got an EXCELLENT
            } else if (!hit && diff < EXCELLENT_THRESHOLD && songNotesArray.get(i)[2] == 0 && (songNotesArray.get(i)[0] == instrument)) {
                hit = true;
                score += EXCELLENT_SCORE;
                int[] t = songNotesArray.get(i);
                t[2] = 1;
                songNotesArray.set(i, t); // mark note as hit
                if (i != 0 && songNotesArray.get(i-1)[2] == 0) { // if the entry before it wasn't hit
                    curCombo = 0; // start combo over
                }
                curCombo++;
            } 
        }

        // play instrument sound no matter what, even if it was in error
        playSound(instrument);
        if (!hit){ // player missed
            playSound(5);
            maxCombo = Math.max(curCombo, maxCombo);
            curCombo = 0;
        } else {
            maxCombo = Math.max(curCombo, maxCombo);
            if (curCombo == 3) { // if combo is large enough
                // crowd cheers!
                playSound(6);
            }
        }
        return hit;
    }

    /* 
     * pauseSounds()
     * This function pauses the game music and any sound effects that 
     * are currently playing.
     */
    private void pauseSounds() {
        // pause the music
        pauseStart = System.currentTimeMillis();
        if (songPlayer != null && songPlayer.isPlaying()) {
            songPlayer.pause();
        }

        // pause instruments and sound effects
        if (instrumentSounds != null) {
            instrumentSounds.pause(streamOne);
            instrumentSounds.pause(streamTwo);
            instrumentSounds.pause(streamThree);
            instrumentSounds.pause(streamFour);
            instrumentSounds.pause(streamError);
            instrumentSounds.pause(streamCrowd);
        }

        // TODO pause recorder if in Studio mode
    }

    /*
     * unpauseSounds()
     * This function resumes the game music and any sound effects that
     * were paused.
     */
    private void unpauseSounds() {
        // resume the music
        if (songPlayer != null && !songPlayer.isPlaying()) {
            songPlayer.start();
        }

        // resume instruments and sound effects
        if (instrumentSounds != null) {
            instrumentSounds.resume(streamOne);
            instrumentSounds.resume(streamTwo);
            instrumentSounds.resume(streamThree);
            instrumentSounds.resume(streamFour);
            instrumentSounds.resume(streamError);
            instrumentSounds.resume(streamCrowd);
        }
    }

    /*
     * pausableSleep()
     * Sleep the given amount of time in small increments, so that sleeping can be interrupted
     * if the game is paused.
     */
    private boolean pausableSleep(long interval) {
        long iTime = interval;
        while (iTime > 0) {
            if (!gameIsCurrentlyPaused) {
                long increment = 50;
                if (iTime < 50) {
                    increment = iTime;
                }
                SystemClock.sleep(increment);
                iTime -= increment;
            } else {
                return false; // return false if exiting early
            }
        }
        return true;
    }
    
    /* 
     * announcerEval()
     * Return a letter grade based on the player's accuracy.
     */
    public String announcerEval(int accuracy) {
        if (accuracy >= 90) {
            return "a";
        } else if (accuracy >= 80) {
            return "b";
        } else if (accuracy >= 70) {
            return "c";
        } else if (accuracy >= 60) {
            return "d";
        } else {
            return "e";
        }
    }

    /*
     * myCompletionListener
     * MediaPlayer listener classes
     * onCompletion() is called when the song is done playing.
     * When done playing, move out of game mode to the next screen.
     */
    public class myCompletionListener implements MediaPlayer.OnCompletionListener {

        @Override
        public void onCompletion(MediaPlayer mp) {
            // if in Concert mode, switch to the score results
            if (mode.equals("concert")) {
                Intent intent = new Intent(player.this, ScoreScreen.class);
                intent.putExtra("stage", stage);
                if (stage) {
                    intent.putExtra("stageNum", stageNum);
                }
                intent.putExtra("maxCombo", maxCombo);
                int accuracy = (int) Math.round((double) getTotalHitNotes() / s.getTotalNotes() * 100);
                intent.putExtra("accuracy", accuracy);
                intent.putExtra("score", score);
                intent.putExtra("perfectScore", PERFECT_SCORE);
                intent.putExtra("totalHitNotes", getTotalHitNotes());
                intent.putExtra("totalNotes", s.getTotalNotes());
                String eval = announcerEval(accuracy);
                intent.putExtra("eval", eval);
                if (stage && stageNum == 3 && (eval.equals("a") || eval.equals("b"))) {
                    unlockNextStage();
                    SystemClock.sleep(1500);
                }
                startActivity(intent);
                // if in Studio mode, switch to the studio results
            } else if (mode.equals("studio")) {
                Intent intent = new Intent(player.this, StudioResults.class);
                intent.putExtra("song", songfileName);
                intent.putExtra("n", songNum);
                startActivity(intent);
            }
        }

    }

    // TODO: Fully implement this error handler
    public class myOnErrorListener implements MediaPlayer.OnErrorListener {
        public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {
            Log.e("GameSongPlayer", "MediaPlayer.onError: what=" + what + " extra=" + extra);
            // can't call releasePlayer(cymbalPlayer);
            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.release();
                mediaPlayer = null;
            }
            return true;
        }
    }

    /*
     * onKeyDown()
     * Intercept pressing of the Android Back button and do something.
     * This lets us "control" the history stack.
     */
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            startActivity(intentBack);
            overridePendingTransition(R.anim.animation_leave, R.anim.animation_enter);
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
        tts.setLanguage(Locale.US);
        if (mode.equals("practice")) {
            GlobalVariableStates appState = ((GlobalVariableStates)getApplicationContext());
            if(appState.getInstructionState())
                tts.speak("You may start tapping the screen.", TextToSpeech.QUEUE_FLUSH, null);
        }
    }
    
    /*
     * unlockNextStage()
     * Call when the user achieves some condition to unlock the next stage. This will only work
     * if the user is currently playing the last available stage.
     */
    private void unlockNextStage() {
        // stage complete! make a success sound
        menuSounds.play(success, (float)1.0, (float)1.0, 0, 0, (float)1.0);
        tts.speak("Stage Complete!", TextToSpeech.QUEUE_FLUSH, null);
        SharedPreferences preferences = getSharedPreferences("CareerVars", Context.MODE_PRIVATE);
        int unlockedStages = preferences.getInt("unlocked", 1);
        SharedPreferences.Editor editor = preferences.edit();
        if ((stageNum == unlockedStages) && stageNum < 3) { // 3 is the maximum # of stages currently
            unlockedStages++;
            tts.speak("You unlocked a new stage!", TextToSpeech.QUEUE_ADD, null);
        }
        editor.putInt("unlocked", unlockedStages);
        editor.commit();
        tts.speak("Automatically returning to the Stage Selection Screen.", TextToSpeech.QUEUE_ADD, null);
        SystemClock.sleep(4600);
        Intent intent = new Intent(this, Career.class);
        startActivity(intent);
        overridePendingTransition(R.anim.animation_leave, R.anim.animation_enter);
    }

    /*
     * GameView
     * The view for playing the music game
     */
    public class GameView extends View {
        // for gestures and menu draw
        //keeps track of each button's state
        private int[] buttonState;
        
        // keeps track of which buttons are being pressed
        private int buttonOneIndex;
        private int buttonTwoIndex;

        //array of regions for all buttons
        private Region[] region;

        //array of rectangles for all buttons
        private Rect[] buttonRect; 

        //array of paint for each button
        private Paint[] paint;

        //pressed state if the user is currently pressing a button
        private final int STATE_PRESSED = 1;  

        //normal state if the user is not currently pressing the button
        private final int STATE_NORMAL = 2; 
        private final int BUTTON_NUM_ON_SCREEN = 4; // 4 possible instruments

        private Context context;
        private boolean multiTouch;

        //the x coordinate of the users first flick time
        private float flickTouchX;

        //the y coordinate of the users first flick time
        private float flickTouchY;

        //the time of the first point the user has flicked their finger
        private long firstFlickTime;

        //the time of the second point the user has flicked their finger
        private long finalFlickTime;

        //users first tap for double tapping
        private long firstDoubleTap;

        //booleans for pausing/restarting/leaving game
        private boolean pauseTheGame;
        private boolean startOrLeaveGame;

        //when game is paused, user can do either of these
        private boolean choseDoubleTap;
        private boolean goBackAPage;
        
        // if MOVE action already caused a sound to emit, don't play sound on tap.
        private boolean moveSound;

        /*
         * Constructor
         * Draws game buttons.
         */
        public GameView(Context c, int width, int height)  
        {  
            super(c);
            context = c;
            firstFlickTime = (long) 0.0;
            finalFlickTime = (long) 0.0;

            // draw game buttons
            buttonRect = new Rect[BUTTON_NUM_ON_SCREEN];
            paint = new Paint[BUTTON_NUM_ON_SCREEN];
            buttonState = new int[BUTTON_NUM_ON_SCREEN];
            region = new Region[BUTTON_NUM_ON_SCREEN];

            multiTouch = false;
            startOrLeaveGame = false;
            gameIsCurrentlyPaused = false;
            pauseTheGame = false;
            choseDoubleTap = false;
            goBackAPage = false;
            moveSound = false; 

            firstDoubleTap = (long) 0.0;

            // Rect(left, top, right, bottom)
            buttonRect[0] = new Rect(10,10,width/2 - 10, height/2 -10);
            buttonRect[1] = new Rect(10,height/2 + 10,width/2 - 10, height -10);	     
            buttonRect[2] = new Rect(width/2 + 10,10,width - 10, height/2 -10);
            buttonRect[3] = new Rect(width/2 + 10,height/2 + 10,width - 10, height -10);

            for (int i = 0; i < BUTTON_NUM_ON_SCREEN; i++){
                buttonState[i] = STATE_NORMAL;
                paint[i] = new Paint();
                paint[i].setColor(0xff00ff00);
                region[i] = new Region(buttonRect[i]);
            }    
            
            buttonOneIndex = 0;
            buttonTwoIndex = 0;
        }  

        /*
         * onTouchEvent()
         * Handles controlling the gameplay through gestures.
         */
        @Override  
        public boolean onTouchEvent(MotionEvent event)  
        {  
            int action = event.getAction() & MotionEvent.ACTION_MASK;

            switch(action)  
            {  	         
            case MotionEvent.ACTION_DOWN: 
                //checks to see if the user double taps while the game is paused
                long systemTime = SystemClock.uptimeMillis() / 1000;
                firstFlickTime = systemTime;
                if(startOrLeaveGame && firstDoubleTap != (long) 0.0 && systemTime - firstDoubleTap > 1){
                    firstDoubleTap = SystemClock.uptimeMillis()/1000;
                } else if(startOrLeaveGame){
                    if(firstDoubleTap == (long) 0.0){
                        firstDoubleTap = SystemClock.uptimeMillis()/1000;
                    }else{
                        choseDoubleTap = true;
                    }
                }
                flickTouchX = event.getX();
                flickTouchY = event.getY();

                // if the game isn't paused, buttons function normally
                if (!gameIsCurrentlyPaused && !disableButtonPresses) {
                    for(int i = 0; i < BUTTON_NUM_ON_SCREEN; i++){
                        // if rectangle i contains the touch and isn't the same rectangle already being pressed
                        if(region[i].contains((int)event.getX(), (int)event.getY())) {
                            buttonState[i] = STATE_PRESSED;
                            buttonOneIndex = i;
                        }
                    }
                    invalidate();
                }
                break;

            case MotionEvent.ACTION_UP: 
                //user decides to go back page
            	multiTouch = false;
                if (goBackAPage) {
                    goBackAPage = false;
                    goBackPage();
                } else if (choseDoubleTap) { // user wants to resume playing
                    startTheGameAgain();
                } else if (pauseTheGame) { // user decided to pause the game
                    pauseTheGame();
                } else if (!gameIsCurrentlyPaused && !disableButtonPresses && !moveSound) {
                    if (mode.equals("concert")) {
                        scoreAndPlay(buttonOneIndex+1);
                    } else if (mode.equals("memory")) {
                        playSound(buttonOneIndex+1);
                        if (memoryNotesArray.get(currentMemoryPresses) == buttonOneIndex+1) {
                            currentMemoryPresses++; // correct press, so progress through the round
                            if (currentMemoryPresses >= memoryNotesArray.size()) { // last press in the round, start new round
                                currentMemoryPresses = 0;
                                if (stage) {
                                    stage2Rounds++;
                                    if (stage2Rounds == 10) {
                                        unlockNextStage();
                                        // don't toggle playerTurn, because the stage is over
                                    } else if (stage2Rounds < 10) {
                                        playerTurn = false;
                                    }
                                } else {
                                    playerTurn = false;
                                }
                            } 
                        } else { // user messed up!
                            disableButtonPresses = true;
                            // play Error noises
                            SystemClock.sleep(500);
                            playSound(5);
                            
                            // start over
                            memoryNotesArray.clear();
                            stage2Rounds = 0;
                            currentMemoryPresses = 0;
                            playerTurn = false;
                        }
                    } else {
                        playSound(buttonOneIndex+1); // studio mode
                        if (stage && stageNum == 1) {
                            stage1Presses++;
                            if (stage1Presses == 20) {
                                // unlock stage 2
                                unlockNextStage();
                            }
                        }
                    }
                }
                moveSound = false;

                //set all button states to normal
                for (int i = 0; i < BUTTON_NUM_ON_SCREEN; i++)
                    buttonState[i] = STATE_NORMAL;
                invalidate();
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                // if the game isn't paused, buttons function normally
                // or, don't use in practice mode
            	multiTouch = true;
               /* if (!gameIsCurrentlyPaused && !mode.equals("memory")) {
                    for(int i = 0; i < BUTTON_NUM_ON_SCREEN; i++){
                        // if rectangle i contains the touch and isn't the same rectangle already being pressed
                        if(region[i].contains((int)event.getX(1), (int)event.getY(1))) {
                            buttonState[i] = STATE_PRESSED;
                            buttonTwoIndex = i;
                        }
                    }
                    
                }*/
                invalidate();
                break;
            case MotionEvent.ACTION_POINTER_UP:
                /*if (!gameIsCurrentlyPaused && !mode.equals("memory")) {
                    if (mode.equals("concert")) {
                        scoreAndPlay(buttonTwoIndex+1);
                    } else {
                        playSound(buttonTwoIndex+1); // studio mode
                    }
                }
                for(int i = 0; i < BUTTON_NUM_ON_SCREEN; i++)
                    buttonState[i] = STATE_NORMAL;*/
            	multiTouch = false;
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                //checks to see if user wants to go back a page
                finalFlickTime = SystemClock.uptimeMillis();
                double distanceMoved = calculateDistance(event, flickTouchX, flickTouchY);
                if(multiTouch && startOrLeaveGame && (finalFlickTime - firstFlickTime <= 400) && distanceMoved > 30.0){
                    goBackAPage = true;
                } else if(multiTouch && !startOrLeaveGame && (finalFlickTime - firstFlickTime <= 400) && distanceMoved > 30.0){
                    //if in practice mode user only has to swipe once to go back 
                    if(mode.equals("practice"))
                        goBackAPage = true;
                    else //otherwise pause the game
                        pauseTheGame = true;
                } else {
                    firstFlickTime = finalFlickTime;
                    flickTouchX = event.getX();
                    flickTouchY = event.getY();
                }
                if (!gameIsCurrentlyPaused && !disableButtonPresses) {
                    for(int i = 0; i < BUTTON_NUM_ON_SCREEN; i++){
                        // if rectangle i contains the touch and isn't the same rectangle already being pressed
                        if(region[i].contains((int)event.getX(), (int)event.getY())) {
                            // note if this is the first time the MOVE event is happening on this square
                            boolean firstTime = true;
                            if (buttonState[i] == STATE_PRESSED) {
                                firstTime = false;
                            }
                            buttonState[i] = STATE_PRESSED;
                            buttonOneIndex = i;
                            if (!mode.equals("memory") && firstTime) {
                                moveSound = true;
                                if (mode.equals("concert")) {
                                    scoreAndPlay(buttonOneIndex+1);
                                } else {
                                    playSound(buttonOneIndex+1); // studio mode
                                    if (stage && stageNum == 1) {
                                        stage1Presses++;
                                        if (stage1Presses == 20) {
                                            // unlock stage 2
                                            unlockNextStage();
                                        }
                                    }
                                }
                            }
                        } else {
                        	 buttonState[i] = STATE_NORMAL;
                        }
                    }
                    
                }
                invalidate();
            }  
            return true;  
        }

        private void goBackPage() {
            menuSounds.play(back, (float)1.0, (float)1.0, 0, 0, (float)1.0);
            context.startActivity(intentBack);
            overridePendingTransition(R.anim.animation_leave, R.anim.animation_enter);
        } 

        
        private double calculateDistance(MotionEvent event, float firstX, float firstY){
        	double xDistPow = Math.pow((double)(event.getX()- firstX),2.0);
            double yDistPow = Math.pow((double)(event.getY() - firstY),2.0);
            return Math.sqrt(xDistPow + yDistPow);
        }
        /*
         * startTheGameAgain()
         * Resume gameplay.
         */
        private void startTheGameAgain() {
            startOrLeaveGame = false;
            gameIsCurrentlyPaused = false;

            // stop text-to-speech if needed
            if (tts.isSpeaking()) {
                tts.stop();
            }

            // make a noise indicating the resume
            menuSounds.play(validate, (float)1.0, (float)1.0, 0, 0, (float)1.0);

            choseDoubleTap = false;

            // resume sounds and music
            unpauseSounds();

            // stop counting pause time
            pauseTime += (System.currentTimeMillis() - pauseStart);
        }

        /*
         * pauseTheGame()
         * Pause gameplay.
         */
        private void pauseTheGame(){
            gameIsCurrentlyPaused = true;

            // Tell the user the game is paused using TTS
            String speak = "Game paused. Double tap to resume the game, or swipe to return to main menu.";
            tts.speak(speak, TextToSpeech.QUEUE_FLUSH, null);

            // stop sounds and music, start counting pause time
            pauseSounds();

            // make a noise indicating the pause
            menuSounds.play(select, (float)1.0, (float)1.0, 0, 0, (float)1.0);

            startOrLeaveGame = true;
            pauseTheGame = false;
        }

        /*
         * onDraw()
         * This function is called when invalidate() happens.
         * Updates the appearance of menu buttons depending on their state.
         */
        @Override  
        public void onDraw(Canvas canvas)  
        {  
            for(int i = 0; i < BUTTON_NUM_ON_SCREEN; i++){
            	
                switch(buttonState[i])  
                { 
                case STATE_PRESSED:  
                	paint[i].setStyle(Style.FILL);
                    paint[i].setColor(Color.rgb(0,100,0));
                    canvas.drawRect(buttonRect[i], paint[i]);
                    paint[i].setStyle(Style.STROKE);
                    paint[i].setColor(Color.rgb(255,252,22));
                    canvas.drawRect(buttonRect[i], paint[i]);
                    break;  
                case STATE_NORMAL:  
                	paint[i].setStyle(Style.STROKE);
                    paint[i].setColor(Color.rgb(255,252,22));
                    canvas.drawRect(buttonRect[i], paint[i]);
                    break;  
                } 

                paint[i].setTextAlign(Paint.Align.CENTER);
                paint[i].setStyle(Style.FILL);
                paint[i].setTextSize((float) (((height/4)-20) * 0.20));
            }
            canvas.drawText("Hihat", (float)width/4, (float)(height/4), paint[0]);
            canvas.drawText("Cymbal", (float)width/4, (float)3*(height/4), paint[1]);
            canvas.drawText("Snare", (float)3*width/4, (float)(height/4), paint[2]);
            canvas.drawText("Kickdrum", (float)3*width/4, (float)3*(height/4), paint[3]);
        } 
    }
    
    /*
     * PlayerHelperTask
     * This class plays the notes that the player should hear and then repeat.
     */
    public class PlayerHelperTask extends AsyncTask<Context, Integer, String>
    {
        @Override
        protected String doInBackground( Context... params ) 
        {
            int nextTime = 0;
            boolean exitEarly = false;

            int i = 0;
            while (true) {
                if (!gameIsCurrentlyPaused) { // if game is not paused
                    if (mode.equals("concert")) {
                        // wait until it's time to play the next note
                        long nowTime = (System.currentTimeMillis() - baseTime) - pauseTime;
                        nextTime = playNotesArray.get(i)[1];
                        
                        if (nextTime - nowTime > 0) { // if you need to wait
                            // if game is paused while waiting, exit early
                            exitEarly = !pausableSleep(nextTime - nowTime);
                        }
                        if (!exitEarly) {
                            playSound(playNotesArray.get(i)[0]);
                            i++;
                            if (i >= playNotesArray.size()) {
                                break;
                            }
                        }
                    } else if (mode.equals("memory")) {
                        if (!playerTurn) {
                            pausableSleep(500);
                            playMemoryRound();
                        }
                    }
                }
            }
            return null;
        }
    }   
}