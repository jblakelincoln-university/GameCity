package com.example.gamecity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Toast;

import com.scenelibrary.classes.AccelerometerManager;
import com.scenelibrary.classes.Colour;
import com.scenelibrary.classes.Globals;
import com.scenelibrary.classes.NfcHelper;
import com.scenelibrary.classes.Scene;
import com.scenelibrary.classes.Objects.ButtonObject;
import com.scenelibrary.classes.Objects.ImageObject;
import com.scenelibrary.classes.Objects.TextObject;

class vec2{
	double x = 0;
			double y = 0;
	public vec2(){
	}
}
public class SceneMain extends Scene{

   
   //Physics   
   vec2 acceleration = new vec2();
   vec2 velocity = new vec2();
   vec2 ballOffset = new vec2(); // Ball's position from the centre
   vec2 ballBase = new vec2(); // Middle of platform (where the ball should be positioned)
   
   double ballHeight = 0; // Height will be 0 whilst on the platform, otherwise decreasing
   double points = 0;
   double boundingRad = 0; // Radius of the platform
   
   private Handler handler = new Handler();
   private Runnable runnable = new Runnable() {

	   private void initialiseScene(){
		   imageBall.getElement().setAlpha(1.f);
			imageBall.setImage(R.drawable.ball);
			imageBall.setAbsScaleX(Globals.screenDimensions.x/10);
			velocity.x = 0;
			velocity.y = 0;
			ballHeight = 0;
			ballBase.x = imagePlatform.getElement().getX();
			ballBase.y = imagePlatform.getElement().getY();
			ballBase.x += imagePlatform.getWidth()/2;
			ballBase.y += imagePlatform.getHeight()/2;
			ballBase.x -= imageBall.getWidth()/2;
			ballBase.y -= imageBall.getHeight()/2;
			ballOffset.x = 0;
			ballOffset.y = 0;
			
			boundingRad = (imagePlatform.getWidth()/2);
			textTimer.getElement().setAlpha(0.f);
			addElementToView(textTimer);	   
	   }
	   
	// Update points flash if it's in view - will become more lower and more transparent over time
	   private void pointsDisplay(){
	       if (pointsIndicator.getElement().getAlpha() > 0.f) {
	    	   pointsIndicator.getElement().setAlpha(Math.abs(1.f-(pointsIndicatorOffset/255.f)));
	    	   // This division isn't just for comedic purposes
	    	   pointsIndicator.setPadding(0, (int)(pointsIndicatorOffset/(Globals.screenDimensions.y/(Globals.screenDimensions.y/1.5f))), 0, 0);
	    	   pointsIndicatorOffset+=3;
	       }
	   }
	   
	   private void screenFreeze(){
		// Exit loop if the screen is being held and freeze time hasn't run out
	       if (timerScale > 5 && timerScale < 255 && ballHeight == 0){
	    	   timerScale+=0.7f;
	    	   textTimer.setTextSize(timerScale/3.f);
	    	   textTimer.getElement().setAlpha(Math.abs(1.f-(timerScale/255.f)));
	    	   return;
	       }
	       else if (timerScale >= 255){ // Decrease points if freeze didn't result in a correct answer
	    		   timerScale = 5;
	    		   if (!correctAnswerPicked)
	    			   updatePoints(-50);
	       }
	       correctAnswerPicked = false;
	   }
	   
	   private void updatePhysics(){
		// Remove ball from platform if it has gone off the edge
	       if (!boundingCircle(ballOffset.x, ballOffset.y, 0, 0, imageBall.getWidth()/6, imagePlatform.getWidth()/2))
	       {
	    	   ballHeight++;
	    	   imageBall.getElement().setAlpha(Math.abs(1.f-((int)ballHeight*2.5f/255.f)));
	       }
	       
	       // If ball is still on the platform, update the velocity. Otherwise, decrease it for falling effect
	       if (ballHeight == 0) {
		       double accX = AccelerometerManager.getX();
		       double accY = AccelerometerManager.getY();
		
		       accX *= 1.f-(accX/Globals.screenDimensions.x);
		       accY *= 1.f-(accY/Globals.screenDimensions.y);
		       
		       velocity.x += accX/66;
		       velocity.y += accY/66;
	       }
	       else if (imageBall.getWidth() > 5){
	    	   imageBall.setAbsScaleX(imageBall.getWidth()-1);
	    	   
	    	   velocity.x -= velocity.x/(50-Math.min(ballHeight, 20));
	    	   velocity.y -= velocity.y/(50-Math.min(ballHeight, 20));
	       }
	       // Once the ball has fallen for so long, reset it.
	       if (ballHeight > 100)
	       {
	    	   ballBase.x = 0;
	    	   updatePoints(-150);
	       }
	       
	       
	       //Update ball
	       ballOffset.x -= velocity.x;
	       ballOffset.y += velocity.y;
	       imageBall.getElement().setPadding((int)((ballBase.x) + ballOffset.x), (int)((ballBase.y) + ballOffset.y), 0, 0);
	       //handler.postDelayed(this, 16);
	   }
	   private boolean boundingCircle(double x1, double y1, double x2, double y2, double rad1, double rad2)
	   {
		   double distanceX = x2 - x1;
		   double distanceY = y2 - y1;
		   double mag = Math.sqrt((distanceX * distanceX) + (distanceY * distanceY));
		   return mag < rad1 + rad2;
	   }
	   
     @Override
     public void run() {
    	 // Don't run game loop if help screen is open
    	 if (helpIndex < 7){
    		 // If NFC is enabled, show the first help screen.
    		 if (helpIndex == -1 && NfcHelper.mNfcAdapter != null && NfcHelper.mNfcAdapter.isEnabled()){
    			 helpNext();
    		 }
    		 return;
    	 }
    	 
    	 // Don't run loop if screen isn't initialised
       if (ballBase.x == 0 && imagePlatform.getElement().getX() == 0)
    	   return;
       // Set up game objects on first loop run.
       else if (ballBase.x == 0)
			initialiseScene();
       
       pointsDisplay();
       
       screenFreeze();
       
       
       updatePhysics();
     }
   
   };
   
   boolean correctAnswerPicked = false;
   
   private void helpNext(){
	   
	   textHelp.setText(helpText[++helpIndex]);
	   
	   if (helpIndex == 0){
		   buttonLeft.getElement().setAlpha(0.f);
		   addElementToView(buttonRight);
	   }
	   else if (helpIndex > 0){
		   addElementToView(buttonLeft);
		   buttonLeft.getElement().setAlpha(1.f);
	   }
	   
	   
	   if (helpIndex > 5){
		  // buttonRight.getElement().setAlpha(0.f);
		   //buttonRight.getElement().setEnabled(false);
	   }
	   
	   if (helpIndex > 6){
		   
		   addElementToView(imagePlatform);
	       addElementToView(imageBall);
	       addElementToView(textPoints);

	       addElementToView(textMain);
	       addElementToView(pointsIndicator);
		   
	       buttonLeft.getElement().setAlpha(0.f);
	       buttonLeft.getElement().setEnabled(false);
	       
	       buttonRight.getElement().setAlpha(0.f);
		   buttonRight.getElement().setEnabled(false);
	       
	       missionSetup();

	       
	   }
   }
   private void helpPrev(){
	   if (helpIndex <= 1){
		   buttonLeft.getElement().setAlpha(0.f);
		   if (helpIndex == 0)
			   return;
	   }
	   buttonRight.getElement().setAlpha(1.f);
       buttonRight.getElement().setEnabled(true);
	   textHelp.setText(helpText[--helpIndex]);
   }
   
   private void setButtonHandlers(){
	   buttonLeft.getElement().setOnClickListener(new View.OnClickListener() {
		   @Override
			public void onClick(View v) {
			   helpPrev();
			}
	   });
	   
	   buttonRight.getElement().setOnClickListener(new View.OnClickListener() {
		   @Override
			public void onClick(View v) {
			   helpNext();
			}
	   });
   }
   
   
   
   private TextObject textMain;
   private TextObject pointsIndicator;
   private TextObject textHelp;
   private TextObject textTimer;
   private TextObject textPoints;
   
   private ImageObject imagePlatform;
   private ImageObject imageBall;
   
   private ButtonObject buttonLeft;
   private ButtonObject buttonRight;
   
   private int currentMission = -1;
   
   private float pointsIndicatorOffset = 0;
   private float timerScale = 5;
   private long missionCompletionTimes[] = new long[6];
   private long missionStartTimes[] = new long[6];
   private boolean missionCompletion[] = new boolean[6];
   
   private Random random = new Random();

   private List<String> missionTitles = new ArrayList<String>();
   
   public SceneMain(int idIn, Activity a, boolean visible) {
     super(idIn, a, visible);
     
     textMain = new TextObject("This is going to be a question", a, Globals.newId());
     textMain.setTextSize(Globals.screenDimensions.x/22.f);
     textMain.setGravity(Gravity.CENTER);
     textMain.getElement().setPaintFlags(Paint.FAKE_BOLD_TEXT_FLAG);
     textMain.getElement().setPadding(Globals.screenDimensions.x/20, 0, Globals.screenDimensions.x/20,0);
     textMain.alignToTop();
     textMain.setColour(Colour.FromRGB(255, 255, 255));
     textMain.setMargins(0, Globals.screenDimensions.y/8, 0, Globals.screenDimensions.y/24);
     
     imageBall = new ImageObject(R.drawable.ball, a, Globals.newId(), false);
     imageBall.alignToLeft();
     imageBall.alignToTop();
     imageBall.getElement().setAlpha(0.f);

     if (NfcHelper.mNfcAdapter == null)
    	 textHelp.setText("This device does not support NFC.");
     else if (!NfcHelper.mNfcAdapter.isEnabled())
    	 textHelp.setText("Enable NFC in your settings to play.");
     else
    	 textHelp.setText("Press the arrow to proceed.");
     
     textHelp.setTextSize(Globals.screenDimensions.y/18.f);
     textHelp.setColour(Colour.FromRGB(255, 255, 255));
     textHelp.getElement().setPadding(Globals.screenDimensions.x/20, 0, Globals.screenDimensions.x/20,0);
     
     textHelp.setGravity(Gravity.CENTER);
     textHelp.getElement().setPaintFlags(Paint.FAKE_BOLD_TEXT_FLAG);
     textHelp.addRule(RelativeLayout.ABOVE, buttonLeft.getId());
     addElementToView(textHelp);
     
     imagePlatform = new ImageObject(R.drawable.circle, a, Globals.newId(), false);
     imagePlatform.setAbsScaleX((int)(Globals.screenDimensions.x/1.5f));
     imagePlatform.setMargins(0, Globals.screenDimensions.y/16, 0, Globals.screenDimensions.y/16);
     imagePlatform.alignToBottom();
     
     buttonLeft = new ButtonObject("<", a, Globals.newId());
     buttonRight = new ButtonObject(">", a, Globals.newId());
     
     buttonLeft.alignToLeft();
     buttonLeft.alignToBottom();
     buttonLeft.getLayoutParams().setMargins(Globals.screenDimensions.x/10, 0, 0, Globals.screenDimensions.y/10);
     
     buttonRight.alignToRight();
     buttonRight.alignToBottom();
     buttonRight.getLayoutParams().setMargins(0, 0, Globals.screenDimensions.x/10, Globals.screenDimensions.y/10);
    
     
     
     buttonLeft.getElement().setTextColor(Colour.FromRGB(255, 255, 255));
     buttonRight.getElement().setTextColor(Colour.FromRGB(255, 255, 255));
     setButtonHandlers();

     textTimer = new TextObject("FREEZE", a, Globals.newId());
     textTimer.setTextSize(timerScale);
     textTimer.getElement().setAlpha(0.f);
     textTimer.setColour(Colour.FromRGB(255, 255, 255));
     
     textPoints = new TextObject("Points: " + 0, a, Globals.newId());
     textPoints.setTextSize(Globals.screenDimensions.x/40.f);
     textPoints.alignToRight();
     textPoints.alignToTop();
     textPoints.setColour(Colour.FromRGB(255, 255, 255));
     textPoints.getLayoutParams().setMargins(0, Globals.screenDimensions.y/36, Globals.screenDimensions.x/12, 0);
     
     pointsIndicator = new TextObject("", a, Globals.newId());
     pointsIndicator.alignToTop();
     pointsIndicator.getElement().setAlpha(0.f);
     pointsIndicator.setMargins(0, Globals.screenDimensions.y/24, 0, 0);
     pointsIndicator.setTextSize(Globals.screenDimensions.x/28.f);
     
     Timer myTimer = new Timer();
     myTimer.schedule(new TimerTask() {
        @Override
        public void run() {
           handler.postDelayed(runnable, 0); // Here you can update the UI as well
        }
     }, 0, 16);
   }
   
   public void updatePoints(double p)
   {
	   if (allComplete)
		   return;
	   	points += p;
	   	textPoints.setText("Points: " + (int)points);
	   	
	   	if (p >= 0)
	   		pointsIndicator.setColour(Colour.FromRGB(10, 255, 10));
	   	else
	   		pointsIndicator.setColour(Colour.FromRGB(255, 10, 10));
	   	
	   	pointsIndicatorOffset = 0;
	   	pointsIndicator.setPadding(0, (int)pointsIndicatorOffset, 0, 0);
	   	pointsIndicator.getElement().setAlpha(1.0f);
	   	
	   	String sign = p > 0 ? "+" : "";
	   	pointsIndicator.setText(sign+(int)p);
	   	
   }
   
   public void ScreenPressed(){
	   timerScale = 6;
   }

   public void ScreenReleased(){
	   timerScale = 5;
	   textTimer.setTextSize(timerScale);
	   if (textTimer.getElement().getAlpha() > 0.1f && !correctAnswerPicked)
		   updatePoints(-50.f);
	   textTimer.getElement().setAlpha(0.f);
   }
   
   boolean allComplete = false;
   
   
   
   private void missionSetup()
   {
     if (currentMission >= 0){
       missionCompletionTimes[currentMission] = System.currentTimeMillis();
       missionCompletion[currentMission] = true;
     }
     
     allComplete = true;
     for (boolean x : missionCompletion)
       if (!x){
         allComplete = false;
         break;
     }
     
     if (allComplete){
    	 textMain.setTextSize(Globals.screenDimensions.x/36.f);
    	 textMain.setText("All questions answered!\nYour times are ...\n\n");	
    	 long totalTime = 0;
    	 for (int i = 0; i < 6; i++){
	    	 long millis = missionCompletionTimes[i] - missionStartTimes[i];
	    	 int seconds = (int) (millis / 1000);
	    	 int minutes = seconds / 60;
	    	 seconds = seconds % 60;
	    	 textMain.getElement().append(missionTitles.get(i) + String.format(": %d:%02d\n", minutes, seconds));
	    	 
	    	 totalTime += millis;
    	 }
    	 
    	 // Bonus points for under ten minutes
    	 if (totalTime < 600000){
    		 allComplete = false;
    		 float bonus = (600000-totalTime)/35;
    		 updatePoints(bonus);
    		 allComplete = true;
    		 textMain.append("\n\nTime bonus!\n+" + (int)bonus + " points!");
    	 }
    	 textMain.append("\n\nTotal points: " + points);
       return;
     }
     
     do{
       currentMission = random.nextInt(6);
     } while (missionCompletion[currentMission] == true);
     
     missionStartTimes[currentMission] = System.currentTimeMillis();
     
     textMain.setText(missionQuestions[currentMission]);
   }
   
   public void NfcScanned(String in, Activity a)
   {
	   if (in.equals("Start")){
		   if (helpIndex == 6){
			   Toast.makeText((Context)a, "Go!", Toast.LENGTH_LONG).show();
			   helpNext();
		   }
	   }
	   else if (missionNames[currentMission].equals(in)){
		  
		   missionTitles.add(in);
	       Toast.makeText((Context)a, "Correct!", Toast.LENGTH_LONG).show();
	       updatePoints(500);
	       correctAnswerPicked = true;
	       missionSetup();
	   }
	   else if (helpIndex > 6){
		   Toast.makeText((Context)a, "Wrong answer!", Toast.LENGTH_LONG).show();
		   updatePoints(-50);
	   }
	   else { }
   }
   
   private int helpIndex = -1;
   
   private String helpText[] = {
		   "Trivia questions will appear on screen",
		   "Scan the circle on the correct answer posters",
		   "Keep the ball balanced whilst you search",
		   "Hold the screen to freeze the ball for up to five seconds",
		   "You will gain points for correct answers, and lose them for incorrect answers",
		   "You will lose points for freezing the ball to answer a question incorrectly",
		   "Hold your device flat and begin!",
		   ""
   };
   private String missionNames[] = {
	       "Big Ben",
	       "Burj Khalifa",
	       "Brazil",
	       "Chrysler Building",
	       "Germany",
	       "Russia"
	   };
	   
	   private String missionQuestions[] = {
	       "Which building is the oldest?",
	       "Which building is the tallest?",
	       "Which country will host the 2016 Olympics?",
	       "Which building is in the USA?",
	       "Which country won the 2014 World Cup?",
	       "Which country is the biggest by area?"
	   };
}