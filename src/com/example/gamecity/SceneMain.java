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
import android.widget.FrameLayout;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Toast;

import com.scenelibrary.classes.AccelerometerManager;
import com.scenelibrary.classes.Colour;
import com.scenelibrary.classes.Globals;
import com.scenelibrary.classes.Scene;
import com.scenelibrary.classes.Objects.ImageObject;
import com.scenelibrary.classes.Objects.TextObject;

class vec2{
	double x = 0;
			double y = 0;
	public vec2(){
	}
}
public class SceneMain extends Scene{

   
   
   
   
   vec2 acceleration = new vec2();
   vec2 velocity = new vec2();
   
   vec2 ballOffset = new vec2();
   
   vec2 ballBase = new vec2();
   
   double ballHeight = 0;
   
   double points = 0;
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
    	 
       if (ballBase.x == 0 && box.getElement().getX() == 0)
    	   return;
       else if (ballBase.x == 0)
       {
    	    ballHeight = 0;
    	    ballBase.x = box.getElement().getX();
			ballBase.y = box.getElement().getY();
			ballBase.x += box.getWidth()/2;
			ballBase.y += box.getHeight()/2;
			ballBase.x -= ball.getWidth()/2;
			ballBase.y -= ball.getHeight()/2;
			ballOffset.x = 0;
			ballOffset.y = 0;
			ball.getElement().setAlpha(1.f);
			ball.setImage(R.drawable.ball);
			ball.setAbsScaleX(Globals.screenDimensions.x/10);
			boundingRad = (box.getWidth()/2);
			timer.getElement().setAlpha(0.f);
			addElementToView(timer);
       }
       
       if (pointsIndicator.getElement().getAlpha() > 0.f)
       {
    	   pointsIndicator.getElement().setAlpha(Math.abs(1.f-(pointsIndicatorOffset/255.f)));
    	   // This division isn't just for comedic purposes
    	   pointsIndicator.setPadding(0, (int)(pointsIndicatorOffset/(Globals.screenDimensions.y/(Globals.screenDimensions.y/1.5f))), 0, 0);
    	   pointsIndicatorOffset+=3;
       }
       
       if (((MainActivity)activity).held && timerScale < 255 && ballHeight == 0){
    	   timerScale++;
    	   timer.setTextSize(timerScale/3.f);
    	   timer.getElement().setAlpha(Math.abs(1.f-(timerScale/255.f)));
    	   
    	   if (timer.getElement().getAlpha() == 0.f)
    	   {
    		   ((MainActivity)activity).held = false;
    		   updatePoints(-50);
    	   }
    	   return;
       }
       
       
       
       if (!boundingCircle(ballOffset.x, ballOffset.y, 0, 0, ball.getWidth()/4, box.getWidth()/2))
       {
    	   ballHeight++;
    	   ball.getElement().setAlpha(Math.abs(1.f-((int)ballHeight*3/255.f)));
       }
       
       if (ballHeight == 0)
       {
	       double accX = AccelerometerManager.getX();
	       double accY = AccelerometerManager.getY();
	
	       velocity.x += accX/26;
	       velocity.y += accY/26;
       }
       else if (ball.getWidth() > 5){
    	   ball.setAbsScaleX(ball.getWidth()-1);
    	   
    	   velocity.x -= velocity.x/(50-Math.min(ballHeight, 20));
    	   velocity.y -= velocity.y/(50-Math.min(ballHeight, 20));
       }
       if (ballHeight > 100)
       {
    	   ballBase.x = 0;
    	   updatePoints(-150);
    	   
       }
       
       ballOffset.x -= velocity.x;
       ballOffset.y += velocity.y;
       
       ball.getElement().setPadding((int)((ballBase.x) + ballOffset.x), (int)((ballBase.y) + ballOffset.y), 0, 0);
       //handler.postDelayed(this, 16);
     }
   
   };
   
   int currentMission = -1;
   boolean missionCompletion[] = new boolean[6];
   double missionCompletionTimes[] = new double[6];
   double missionStartTimes[] = new double[6];
   private Random random = new Random();
   
   int timerScale = 5;
   
   public TextObject textMain;
   ImageObject ball;
   TextObject timer;
   TextObject textPoints;
   public ImageObject box;
   TextObject pointsIndicator;
   
   vec2 pointsIndicatorBase = new vec2();
   float pointsIndicatorOffset = 0;
   
   public SceneMain(int idIn, Activity a, boolean visible) {
     super(idIn, a, visible);
     
     textMain = new TextObject("This is going to be a question", a, Globals.newId());
     textMain.setTextSize(Globals.getTextSize()*2.5f);
     textMain.setGravity(Gravity.CENTER);
     textMain.getElement().setPaintFlags(Paint.FAKE_BOLD_TEXT_FLAG);
     textMain.getElement().setPadding(Globals.screenDimensions.x/20, 0, Globals.screenDimensions.x/20,0);
     textMain.alignToTop();
     textMain.setColour(Colour.FromRGB(255, 255, 255));
     missionSetup();
     
     ball = new ImageObject(R.drawable.ball, a, Globals.newId(), false);
     ball.alignToLeft();
     ball.alignToTop();
     
     
     
     box = new ImageObject(R.drawable.circle, a, Globals.newId(), false);
     box.setAbsScaleX((int)(Globals.screenDimensions.x/1.5f));
     box.setMargins(0, Globals.screenDimensions.y/16, 0, Globals.screenDimensions.y/16);
     box.alignToBottom();
     //textMain.addRule(RelativeLayout.ABOVE, box.getId());
     textMain.setMargins(0, Globals.screenDimensions.y/8, 0, Globals.screenDimensions.y/24);
     
     timer = new TextObject("FREEZE", a, Globals.newId());
     //timer.getElement().setScaleType(ScaleType.CENTER_CROP);
     timer.setTextSize(timerScale);
     timer.getElement().setAlpha(0.f);
     timer.setColour(Colour.FromRGB(255, 255, 255));
     
     textPoints = new TextObject("Points: " + 0, a, Globals.newId());
     textPoints.setTextSize(Globals.getTextSize()*0.7f);
     textPoints.alignToRight();
     textPoints.alignToTop();
     
     pointsIndicator = new TextObject("", a, Globals.newId());
     pointsIndicator.alignToTop();
     pointsIndicator.getElement().setAlpha(0.f);
     pointsIndicator.setMargins(0, Globals.screenDimensions.y/24, 0, 0);
     pointsIndicator.setTextSize(Globals.getTextSize()*1.5f);
     addElementToView(box);
     addElementToView(ball);
     addElementToView(textPoints);

     addElementToView(textMain);
     addElementToView(pointsIndicator);
     
     ball.getElement().setPadding((int)ballBase.x, (int)ballBase.y, 0, 0);
     
     
     //handler.postDelayed(runnable, 0);
     
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
	   	points += p;
	   	textPoints.setText("Points: " + points);
	   	
	   	if (p >= 0)
	   		pointsIndicator.setColour(Colour.FromRGB(10, 255, 10));
	   	else
	   		pointsIndicator.setColour(Colour.FromRGB(255, 10, 10));
	   	
	   	pointsIndicatorOffset = 0;
	   	pointsIndicator.setPadding(0, (int)pointsIndicatorOffset, 0, 0);
	   	pointsIndicator.getElement().setAlpha(1.0f);
	   	
	   	String sign = p > 0 ? "+" : "";
	   	pointsIndicator.setText(sign+p);
	   	
   }

   public void ScreenReleased(){
	   timerScale = 5;
	   timer.setTextSize(timerScale);
	   if (timer.getElement().getAlpha() > 0.1f)
		   updatePoints(-50.f);
	   timer.getElement().setAlpha(0.f);
	   
	   //timer.setScale(timerScale, timerScale);
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
       updatePoints(200.f);
       missionSetup();
     }
     else{
       Toast.makeText((Context)a, in, Toast.LENGTH_LONG).show();
       updatePoints(-50);
     }

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