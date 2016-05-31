package com.capstone.gamesongplayer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.SystemClock;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.widget.TextView;

/*
 * InformationView
 * Displays information to the user and handles gesture controls.
 */
public class InformationView extends TextView {
    // for detecting double taps
    private long firstDoubleTap;
    private boolean userDoubleTapped;
    
    //the x coordinate of the users first flick time
    private float flickTouchX;

    // screen to go to next after this screen
    private Intent nextScreen;
    
    // screen to go to previously to this screen
    private Intent backScreen;

    // parent context
    private Context parentC;
    
    //the y coordinate of the users first flick time
    private float flickTouchY;
    
    //the time of the first point the user has flicked their finger
    private long firstFlickTime;
    
    //the time of the second point the user has flicked their finger
    private long finalFlickTime;
    
    //see if user wants to go back a page
    private boolean goBackAPage;
    
    private boolean multiTouch;
    
    // menu sounds
    protected SoundPool menuSounds = null;
    protected int select = 0;
    protected int validate = 0;
    protected int back = 0;
    
    // vibrator
    protected OptionalVibrator v = null;

    /*
     * Constructor
     * Sets up sound effects, the vibrator, and variables needed for gestures.
     */  
    public InformationView (Context context, Intent intentNext, Intent intentBack, String screenName) {
        super(context);
        
        firstDoubleTap = (long) 0.0;
        userDoubleTapped = false;
        firstFlickTime = (long) 0.0;
        finalFlickTime = (long) 0.0;
		goBackAPage = false;
		multiTouch = false;
        
        // get next screen, previous screen and parent context
        nextScreen = intentNext;
        backScreen = intentBack;
        parentC = context;
        
        // load menu sounds
        menuSounds = new SoundPool(8, AudioManager.STREAM_MUSIC, 0);
        select = menuSounds.load(context, R.raw.menu_select, 1);
        back = menuSounds.load(context, R.raw.menu_back, 1);
        validate = menuSounds.load(context, R.raw.menu_validate, 1);

        // initialize vibrator
        v = new OptionalVibrator(context);
    }

    /*
     * onTouchEvent()
     * Handles controlling the menu through gestures.
     */
    @Override  
    public boolean onTouchEvent(MotionEvent event)  
    {  
        int action = event.getAction() & MotionEvent.ACTION_MASK;

        switch(action)  
        {  	         
        case MotionEvent.ACTION_DOWN: 
        	long systemTime = SystemClock.uptimeMillis() / 1000;
        	if(firstDoubleTap != (long) 0.0 && systemTime - firstDoubleTap > 1){
        		firstDoubleTap = SystemClock.uptimeMillis()/1000;
        	} else {
        		if(firstDoubleTap == (long) 0.0){
        			firstDoubleTap = SystemClock.uptimeMillis()/1000;
        		}else{
        			userDoubleTapped = true;
        		}
        	}
        	firstFlickTime = systemTime;
        	flickTouchX = event.getX();
            flickTouchY = event.getY();
        	break;
        case MotionEvent.ACTION_UP: 
        	multiTouch = false;
        	if (goBackAPage) {
        	    if (backScreen != null) {
            		goBackAPage = false;
            		menuSounds.play(back, (float)1.0, (float)1.0, 0, 0, (float)1.0);
            		parentC.startActivity(backScreen);
            		((Activity) parentC).overridePendingTransition(R.anim.animation_leave, R.anim.animation_enter);
        	    }
        	} else if (userDoubleTapped && (nextScreen != null)) {
        		userDoubleTapped = false;
        		changeScreen();
        	}
        	invalidate();
            break;
        case MotionEvent.ACTION_POINTER_DOWN:
        	multiTouch = true;
        	invalidate();
        	break;
        case MotionEvent.ACTION_POINTER_UP:
        	multiTouch = false;
        	invalidate();
        	break;
        case MotionEvent.ACTION_MOVE:
        	//checks to see if user wants to go back a page
        	finalFlickTime = SystemClock.uptimeMillis();
        	double distanceMoved = calculateDistance(event, flickTouchX, flickTouchY);
        	
        	if(multiTouch && (finalFlickTime - firstFlickTime <= 400) && distanceMoved > 50.0){
        		goBackAPage = true;
        		
        	} else {
        		firstFlickTime = finalFlickTime;
        		flickTouchX = event.getX();
        		flickTouchY = event.getY();
        	}
        }
        invalidate();
        return true;
    }
    
    private double calculateDistance(MotionEvent event, float firstX, float firstY){
    	double xDistPow = Math.pow((double)(event.getX()- firstX),2.0);
        double yDistPow = Math.pow((double)(event.getY() - firstY),2.0);
        return Math.sqrt(xDistPow + yDistPow);
    }

    /*
     * changeScreen()
     * Go to the next screen after a double tap.
     */
    private void changeScreen(){
        v.vibrate(40); // haptic feedback
        
        menuSounds.play(validate, (float)1.0, (float)1.0, 0, 0, (float)1.0); // audible feedback
        parentC.startActivity(nextScreen);
    }
}