package com.capstone.gamesongplayer;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.util.HashMap;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;

/*
 * StudioSave
 * The studio recording save screen. Here, the user names and saves their recording.
 */
public class StudioSave extends Screen implements OptionalTextToSpeech.OnUtteranceCompletedListener {
    // text to speech options
    private HashMap<String, String> myHashAlarm = new HashMap<String, String>();
    
    // recorder for recording song name
    private MediaRecorder recorder = null;
    
    // the unique # assigned to the recorded song
    private int songNum;
    
    // information for screen to tell user
    private String information;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        screenName = "Save Recorded Song"; // for text to speech
        super.onCreate(savedInstanceState);
        
        // get # assigned to the recorded song
        // the same number will be used for the song name
        Bundle e = getIntent().getExtras();
        songNum = e.getInt("n");
        
        // assign an ID to the text to speech utterance that prompts the user to name their song
        //myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "start file naming");
        
        // define next page
        Intent intent = new Intent(this, MainScreen.class);
        
        // Create the score stats text
        InformationView tv = new InformationView(this, intent, null, screenName);
        information = "Speak what you want to name your song after the beep. " +
                "Double tap the screen when you're done naming.";
        tv.setTextSize(25);
        tv.setGravity(Gravity.CENTER);
        tv.setText(information);
        setContentView(tv);

    }
    
    @Override
    public void onPause() {
        super.onPause();
        // shutdown recorder
        if (recorder != null) {
            recorder.stop();
            recorder.release();
            recorder = null;
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
        super.onInit(status);
        
        myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "start file naming");
        tts.setOnUtteranceCompletedListener(this);
      
        // speak instructions to the user
        tts.speak(information, TextToSpeech.QUEUE_ADD, myHashAlarm);
    }

    /* 
     * onUtteranceCompleted()
     * When speaking instructions completes, start recording song name.
     */
    @Override
    public void onUtteranceCompleted(String uttId) {
        if (uttId.equals("start file naming")) {
            // make a beep
            new ToneGenerator(AudioManager.STREAM_DTMF, 
                    ToneGenerator.MAX_VOLUME).startTone(ToneGenerator.TONE_DTMF_4, 500);
            
            // start recording
            try {   
                recorder = new MediaRecorder();
                ContentValues cv = new ContentValues(3);
                cv.put(MediaStore.MediaColumns.TITLE, "Song");
                cv.put(MediaStore.MediaColumns.DATE_ADDED, System.currentTimeMillis());
                cv.put(MediaStore.MediaColumns.MIME_TYPE, "audio/mpeg");
                FileOutputStream f = openFileOutput("songname" + songNum, Context.MODE_PRIVATE);
                FileDescriptor path = f.getFD();
                recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                recorder.setOutputFile(path);
                recorder.prepare();
                recorder.start();
            } catch (Exception e) {
                // TODO: handle error
            }
        } 
    }
}
