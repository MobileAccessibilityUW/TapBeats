package com.capstone.gamesongplayer;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.Paint.Style;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.SystemClock;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.util.Log;

/* 
 * MenuView
 * This is the class all of our program's menu views extend.
 * Contains gesture recognition and menu drawing functionality common to
 * all menu views.
 */
public class MenuView extends View {
    // gestures and menu draw
	//set to true if user decides to double tap
    private boolean registerDoubleTap;
    
    //first time the user touches the screen after the user decided to double tap
    private long firstEventTimeSec; 
    
    //selection time of what button the user wants to click/go to that page
    private long selectionTimeSec;
    
    //keeps track of each button's state
    private int[] buttonState;
    
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
    
    //keeps track of the last button pressed
    private boolean [] track;
    
    //x coordinate of where the user pressed down on the screen
    private float lastTouchX;
    
    //y coordinate of where the user pressed down on the screen
    private float lastTouchY;
    
    //the x coordinate of the users first flick time
    private float flickTouchX;
    
    //the y coordinate of the users first flick time
    private float flickTouchY;
    
    //the time of the first point the user has flicked their finger
    private long firstFlickTime;
    
    //the time of the second point the user has flicked their finger
    private long finalFlickTime;
    
    //the screen the user has selected
    private int goToThisScreen;
    
    //see if user wants to go back a page
    private boolean goBackAPage;
    
    // define next and previous page
    protected Intent intentNext;
    protected Intent intentBack;
    
    //this is for when stream line gets turned off in options
    //and then the next onDown will be a selection, not doubletap 
    protected boolean streamLineOff;
    
    //user made selection page to go to
    private boolean madeSelection;
    
    //which menu you are in
    protected int currentView;

    // menu sounds
    protected SoundPool menuSounds = null;
    protected int select = 0;
    protected int validate = 0;
    protected int back = 0;
    protected int negativebeep = 0;
    protected int success = 0;
    
    private boolean multiTouch;
    // vibrator
    protected OptionalVibrator v = null;
    
    // text to speech
    protected OptionalTextToSpeech tts; 

    // parent context
    protected Context context;
    
    // the mode for song select (Concert or Tutorial)
    protected String modeName;
    
    // array of menu items 
    protected ArrayList<String> menuItems;
    protected int menuSize;

    //height and width of screen
    private int height;
    private int width;
    
    // color information
    protected int statePressedColor;
    protected int stateNormalColor;
    
    private long firstDoubleTap;
    private boolean userDoubleTapped;
    
    protected String screenName;
    
    /*
     * Constructor
     * Sets up sound effects, the vibrator, and variables needed for gestures.
     */
    public MenuView(Context c, OptionalTextToSpeech m, Intent iBack)  
    {  
        super(c);
        
        // get context and text to speech from parent activity
        tts = m;
        
        context = c;
        
        // set back screen
        intentBack = iBack;

        // Initialize gesture and time variables
        registerDoubleTap = false;
        firstEventTimeSec = (long) 0.0;
        selectionTimeSec = (long) 0.0;
        
        firstFlickTime = (long) 0.0;
        finalFlickTime = (long) 0.0;
        
        goToThisScreen = -1;
        multiTouch = false;
        goBackAPage = false;
        streamLineOff = false;
        madeSelection = false;
        
        firstDoubleTap = (long) 0.0;
        userDoubleTapped = false;

        // load menu sounds
        menuSounds = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
        select = menuSounds.load(context, R.raw.menu_select, 1);
        back = menuSounds.load(context, R.raw.menu_back, 1);
        validate = menuSounds.load(context, R.raw.menu_validate, 1);
        negativebeep = menuSounds.load(context, R.raw.negativebeep, 1);
        success = menuSounds.load(context, R.raw.success, 1);

        // initialize vibrator
        v = new OptionalVibrator(context);
        
    }  
    
    /* 
     * drawMenu()
     * Given the screen's width and height and while taking into account
     * the number of menu items, draws evenly-spaced, evenly-sized menu buttons.
     * This function MUST be called by the extending subclass ONLY AFTER
     * it has set up menu items.
     */
    public void drawMenu(int width, int height) {
    	this.height = height;
    	this.width = width;
        menuSize = menuItems.size();
        buttonRect = new Rect[menuSize];
        paint = new Paint[menuSize];
        buttonState = new int[menuSize];
        region = new Region[menuSize];
        track = new boolean[menuSize];
        
        for (int i = 0; i < menuSize; i++) {
            buttonRect[i] = new Rect(10, ((height / menuSize) * i) + 10, 
                    width - 10, ((height / menuSize) * (i + 1)) - 10);
        }

        for (int i = 0; i < menuSize; i++) {
            buttonState[i] = STATE_NORMAL;
            paint[i] = new Paint();
            paint[i].setColor(0xff00ff00);
            region[i] = new Region(buttonRect[i]);
            track[i] = false;
        }   
    }

    /*
     * onTouchEvent()
     * Handles controlling the menu through gestures.
     */
    @Override  
    public boolean onTouchEvent(MotionEvent event)  
    {  
    	GlobalVariableStates appState = ((GlobalVariableStates)context.getApplicationContext());
        int action = event.getAction() & MotionEvent.ACTION_MASK;
        int x = (int)event.getX();
        int y = (int)event.getY();
        int rectNum = 0;
        boolean contains = false;
        for(int i = 0; i < menuSize; i++) {
            if (region[i].contains(x, y)) {
                rectNum = i;
                contains = true;
                break;
            }
        }
        Log.e("SongSelect", "entered onTouchEvent: action is = " + action);
        switch(action)  
        {            
        case MotionEvent.ACTION_DOWN: 
        	firstFlickTime = SystemClock.uptimeMillis();
            lastTouchX = event.getX();
            lastTouchY = event.getY();
            flickTouchX = lastTouchX;
            flickTouchY = lastTouchY;
            long systemTimeSec = SystemClock.uptimeMillis()/1000;
            if((systemTimeSec - selectionTimeSec) >= 2 || 
                    (firstEventTimeSec != (long) 0.0 && ((systemTimeSec - firstEventTimeSec) > 1))){
                registerDoubleTap = false;
                firstEventTimeSec = (long) 0.0;
            }
            //This handles the case for double tap
            if(registerDoubleTap){
                //if its the first tap after a selection, get its time
                if(firstEventTimeSec == (long) 0.0){
                    firstEventTimeSec = systemTimeSec;
                } else {
                	
                	//if its the second tap after the selection,  get the time
                    //right now in seconds and subtract the first time to get the difference
                    //so if the user double taps within a certain time period
                    //it will select the user option.
                    for(int i = 0; i < menuSize; i++){
                        if(track[i]){
                            madeSelection = true;
                            track[i] = false;
                            goToThisScreen = i;
                        }
                    } 
                }    
            } else {
            	//user makes a selection when they press down on screen
                if (contains) {
                    buttonState[rectNum] = STATE_PRESSED;
                    checkVibrationAndTTS(rectNum);
                    track[rectNum] = true;
                }
            }
            invalidate();
            break;

        case MotionEvent.ACTION_UP:  
            //isMultiTouch = false;
            // change all back to normal
        	multiTouch = false;
            for(int i = 0; i < menuSize; i++)
                buttonState[i] = STATE_NORMAL;
            
            //set registerDoubleTap to be true when the user lifts up 
            //their finger cause then you know they are done using the slide rule
            //or just made a single tap on the screen for their selection
        	//streamline has to be false for stream line case
        	if(!appState.getStreamLineState() && !streamLineOff)
        		registerDoubleTap = true;
        	else
        		streamLineOff = false;
    		
        	//user decides to go back a page
        	if(userDoubleTapped){
        		userDoubleTapped = false;
        	} else if(goBackAPage){
        		goBackAPage = false;
        		goBackPage();
        	} else if(madeSelection){
                madeSelection = false;
                registerDoubleTap = false;
                changeScreen(goToThisScreen);
            }
            
            
            selectionTimeSec = SystemClock.uptimeMillis()/1000;
            break;
        case MotionEvent.ACTION_POINTER_1_DOWN:
        	multiTouch = true;
        	tts.stop();
        	long systemTime = SystemClock.uptimeMillis() / 1000;
        	if(firstDoubleTap != (long) 0.0 && systemTime - firstDoubleTap > 1){
        		firstDoubleTap = SystemClock.uptimeMillis()/1000;
        	} else {
        		if(firstDoubleTap == (long) 0.0){
        			firstDoubleTap = SystemClock.uptimeMillis()/1000;
        		}else{
        			tts.speak(screenName, TextToSpeech.QUEUE_FLUSH, null);
        			userDoubleTapped = true;
        		}
        	}
        	invalidate();
        	break;
        case MotionEvent.ACTION_POINTER_1_UP:
        	multiTouch = false;
        	invalidate();
        	break;
        case MotionEvent.ACTION_MOVE:
        	
        	//see if user wants to go back a page by keeping track of flick events
        	//if its less than a certain time and the user moved their finger a certain
        	//distance, go back a page && Math.abs(event.getX() - flickTouchX) > 30
			//&& Math.abs(flickTouchY - event.getY()) < 120
        	double distanceMoved = calculateDistance(event, lastTouchX, lastTouchY);
        	
        	finalFlickTime = SystemClock.uptimeMillis();
        	if(multiTouch && distanceMoved > 50.0 && (finalFlickTime - firstFlickTime <= 400) ){
        		goBackAPage = true;
        	} else {
        		firstFlickTime = finalFlickTime;
        		flickTouchX = event.getX();
        		flickTouchY = event.getY();
        	}
        	
        	//get distance moved just in case user decided not to double tap after menu selection
        	//if user decides not to double tap, set everything back to before user made selection
            if(firstEventTimeSec != (long) 0.0 && distanceMoved > 20.0
                    || (madeSelection && distanceMoved > 20.0) && !appState.getStreamLineState()){
                firstEventTimeSec = (long)0.0;
                registerDoubleTap = false;
                madeSelection = false;
            }
            
            //this is the slide rule. User is sliding their finger on the screen to see what menu selection
            //they want to make
            if(!registerDoubleTap){
                if (contains) {
                    buttonState[rectNum] = STATE_PRESSED;
                    if(!track[rectNum]){
                        track[rectNum] = true;
                        checkVibrationAndTTS(rectNum);
                    }
                } 
                for(int i = 0; i < menuSize; i++) {
                    if (i != rectNum) { // clear all the rectangles except the one you're on
                        track[i] = false;
                        buttonState[i] = STATE_NORMAL;
                    }
                }
            }
            break;
        }  
        invalidate();
        return true;  
    } 
    
    /*
     * goBackPage()
     * Moves to the previous menu screen. Where the screen moves is based
     * on the current screen.
     */
    protected void goBackPage(){
        if (intentBack != null) {
        	menuSounds.play(back, (float)1.0, (float)1.0, 0, 0, (float)1.0);
            context.startActivity(intentBack);
            ((Activity) context).overridePendingTransition(R.anim.animation_leave, R.anim.animation_enter);
        }
    }
    
    /*
     * calculateDistance()
     * Returns the distance the user has moved their finger based on 
     * Pythagorean theorem.
     */
    private double calculateDistance(MotionEvent event, float firstX, float firstY){
    	double xDistPow = Math.pow((double)(event.getX()- firstX),2.0);
        double yDistPow = Math.pow((double)(event.getY() - firstY),2.0);
        return Math.sqrt(xDistPow + yDistPow);
    }
    
    /*
     * onDraw()
     * This function is called when invalidate() happens.
     * Updates the appearance of menu buttons depending on their state.
     */
    @Override  
    public void onDraw(Canvas canvas)  
    {  
    	
        for(int i = 0; i < menuSize; i++){
            switch(buttonState[i])  
            { 
            case STATE_PRESSED:
            	paint[i].setColor(statePressedColor);
            	paint[i].setStyle(Style.STROKE);
            	canvas.drawRect(buttonRect[i], paint[i]);
                break;  
            case STATE_NORMAL:  
            	paint[i].setStyle(Style.STROKE);
            	if (stateNormalColor == -1) {
            	    setColorForOptionsView(paint[i], i);
            	} else {
            	    paint[i].setColor(stateNormalColor);
            	}
                canvas.drawRect(buttonRect[i], paint[i]);
                break;  
            } 
            double temp = ((height/menuSize)-20)*.45;
            paint[i].setTextAlign(Paint.Align.CENTER);
            paint[i].setStyle(Style.FILL);
            paint[i].setTextSize((float) (((height/4)-20) * 0.20));
            canvas.drawText(menuItems.get(i), (float)width/2, (float)((height/menuSize)* (i+1)-10 - temp), paint[i]);
        
        }
    } 
    
    /*
     * setColorForOptionsView()
     * Method for setting the colors for only the options view.
     */
    protected void setColorForOptionsView(Paint p, int button){
		// content of method is filled out in OptionsView.java
        // placed here for visibility
    }

    /*
     * checkVibrationAndTTS()
     * Given the index of the menu item being interacted with,
     * vibrates or speaks the name of the menu item if the options
     * indicate that they are enabled.
     */
    private void checkVibrationAndTTS(int rectNum){
        // Get Options state
        GlobalVariableStates appState = ((GlobalVariableStates)context.getApplicationContext());
        v.vibrate(20);
        sayMenuItems(rectNum);
        if(appState.getStreamLineState()){
        	goToThisScreen = rectNum;
        	madeSelection = true;
        }
    }

    /*
     * changeScreen()
     * Given the index i of the menu item selected, do something.
     */
    protected void changeScreen(int i){
        v.vibrate(40); // haptic feedback
        
        // The rest of the function needs to be implemented by the subclass.
        // Defined here for visibility to onTouchEvent.
    }

    /* 
     * sayMenuItems()
     * Given the index i of the menu item being interacted with, speak
     * the name of that menu item.
     * This may be overridden by subclasses if needed.
     */
    protected void sayMenuItems(int i) {
        tts.speak(menuItems.get(i),TextToSpeech.QUEUE_FLUSH,null);
    }
}