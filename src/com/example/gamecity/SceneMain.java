package com.example.gamecity;

import java.util.Arrays;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.view.Gravity;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.scenelibrary.classes.AccelerometerManager;
import com.scenelibrary.classes.Colour;
import com.scenelibrary.classes.Globals;
import com.scenelibrary.classes.Scene;
import com.scenelibrary.classes.Objects.ImageObject;
import com.scenelibrary.classes.Objects.TextObject;

public class SceneMain extends Scene{

   
   
   
   
   double accelerationX = 0;
   double accelerationY = 0;
   double velocityX = 0;
   double velocityY = 0;
   
   double ballOffsetX = 0;
   double ballOffsetY = 0;
   
   double ballBaseX = 0;
   double ballBaseY = 0;
   
   double ballHeight = 0;
   
   double boundingX = 0;
   double boundingY = 0;
   double boundingRad = 0;
   
   private Handler handler = new Handler();
   private Runnable runnable = new Runnable() {

	   private boolean boundingCircle(double x1, double y1, double x2, double y2, double rad1, double rad2)
	   {
		   double distanceX = x2 - x1;
		   double distanceY = y2 - y1;
		   
		   double mag = Math.sqrt((distanceX * distanceX) + (distanceY * distanceY));
		   
		   return mag < rad1 + rad2;
	   }
     @Override
     public void run() {
    	 
       if (ballBaseX == 0 && box.getElement().getX() == 0)
    	   return;
       else if (ballBaseX == 0)
       {
    	    ballBaseX = box.getElement().getX();
			ballBaseY = box.getElement().getY();
			ballBaseX += box.getWidth()/2;
			ballBaseY += box.getHeight()/2;
			ballBaseX -= ball.getWidth()/2;
			ballBaseX += ball.getHeight()/2;
			
			boundingX = box.getElement().getWidth();
			boundingY = box.getElement().getHeight();
			
			boundingRad = (box.getWidth()/2);
       }
       
       if (!boundingCircle(ballOffsetX, ballOffsetY, 0, 0, ball.getWidth()/2, box.getWidth()/2))
       {
    	   ballHeight++;
    	   textMain.setText("" + velocityX);
       }
       
       if (ballHeight == 0)
       {
    	   textMain.setText("BallX: " + (int)ballOffsetX +
    			   			"\nBallY: " + (int)ballOffsetY +
    			   			"\nBoxX: " + (int)boundingX +
    			   			"\nBoxY: " + (int)boundingY +
    			   			"\nBallRad: " + (int)ball.getWidth() +
    			   			"\nBoxRad: " + (int)box.getWidth());
	       double accX = AccelerometerManager.getX();
	       double accY = AccelerometerManager.getY();
	
	       velocityX += accX/20;
	       velocityY += accY/20;
	
	       
	
	       
       }
       else if (ball.getWidth() > 5){
    	   ball.setAbsScaleX(ball.getWidth()-1);
    	   
    	   velocityX -= velocityX/ball.getWidth();
    	   velocityY -= velocityY/ball.getWidth();
       }
       
       ballOffsetX -= velocityX;
       ballOffsetY += velocityY;
       
       ball.getElement().setPadding((int)((ballBaseX) + ballOffsetX), (int)((ballBaseY) + ballOffsetY), 0, 0);
       //handler.postDelayed(this, 16);
     }
   
   };
   
   int currentMission = -1;
   boolean missionCompletion[] = new boolean[6];
   double missionCompletionTimes[] = new double[6];
   double missionStartTimes[] = new double[6];
   private Random random = new Random();
   
   public TextObject textMain;
   ImageObject ball;
   public ImageObject box;
   
   public SceneMain(int idIn, Activity a, boolean visible) {
     super(idIn, a, visible);
     
     textMain = new TextObject("This is going to be a question", a, Globals.newId());
     textMain.setTextSize(Globals.getTextSize()*2.5f);
     textMain.setGravity(Gravity.CENTER);
     textMain.getElement().setPaintFlags(Paint.FAKE_BOLD_TEXT_FLAG);
     textMain.getElement().setPadding(Globals.screenDimensions.x/20, 0, Globals.screenDimensions.x/20, Globals.screenDimensions.y/4);
     textMain.setColour(Colour.FromRGB(255, 255, 255));
     missionSetup();
     
     ball = new ImageObject(R.drawable.ball, a, Globals.newId(), false);
     ball.alignToLeft();
     ball.alignToTop();
     ball.setAbsScaleX(Globals.screenDimensions.x/10);
     
     
     box = new ImageObject(R.drawable.circle, a, Globals.newId(), false);
     box.setAbsScaleX((int)(Globals.screenDimensions.x/1.5f));
     box.alignToBottom();
     
     
     
     
     addElementToView(box);
     addElementToView(ball);

     addElementToView(textMain);
     ball.getElement().setPadding((int)ballBaseX, (int)ballBaseY, 0, 0);
     
     
     //handler.postDelayed(runnable, 0);
     
     Timer myTimer = new Timer();
     myTimer.schedule(new TimerTask() {
        @Override
        public void run() {
           handler.postDelayed(runnable, 0); // Here you can update the UI as well
        }
     }, 0, 16);
   }

   
   private void missionSetup()
   {
     if (currentMission >= 0){
       missionCompletionTimes[currentMission] = System.currentTimeMillis();
       missionCompletion[currentMission] = true;
     }
     
     boolean allComplete = true;
     for (boolean x : missionCompletion)
       if (!x){
         allComplete = false;
         break;
     }
     
     if (allComplete){
       textMain.setText("COMPLETE");
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
     if (missionNames[currentMission].equals(in))
     {
       Toast.makeText((Context)a, "YES!", Toast.LENGTH_LONG).show();
       missionSetup();
     }
     else
       Toast.makeText((Context)a, in, Toast.LENGTH_LONG).show();

   }
   
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