package com.example.gamecity;

import java.util.Arrays;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.graphics.Paint;
import android.os.Handler;
import android.view.Gravity;
import android.widget.Toast;

import com.scenelibrary.classes.AccelerometerManager;
import com.scenelibrary.classes.Colour;
import com.scenelibrary.classes.Globals;
import com.scenelibrary.classes.Scene;
import com.scenelibrary.classes.Objects.ImageObject;
import com.scenelibrary.classes.Objects.TextObject;

public class SceneMain extends Scene{

   int currentMission = -1;
   boolean missionCompletion[] = new boolean[6];
   double missionCompletionTimes[] = new double[6];
   double missionStartTimes[] = new double[6];
   private Random random = new Random();
   
   ImageObject ball;
   
   double accelerationX = 0;
   double accelerationY = 0;
   double velocityX = 0;
   double velocityY = 0;
   
   double ballOffsetX = 0;
   double ballOffsetY = 0;
   
   private Handler handler = new Handler();
   private Runnable runnable = new Runnable() {

     @Override
     public void run() {
       double accX = AccelerometerManager.getX();
       double accY = AccelerometerManager.getY();
       
       velocityX += accX/8;
       velocityY += accY/8;

       ballOffsetX -= velocityX;
       ballOffsetY += velocityY;

       ball.getElement().setPadding((int)((Globals.screenDimensions.x/2) + ballOffsetX), (int)((Globals.screenDimensions.y/2) + ballOffsetY), 0, 0);
       //handler.postDelayed(this, 16);
     }
   
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
   
   TextObject textMain;
   
   public SceneMain(int idIn, Activity a, boolean visible) {
     super(idIn, a, visible);
     
     textMain = new TextObject("This is going to be a question", a, Globals.newId());
     textMain.setTextSize(Globals.getTextSize()*4.5f);
     textMain.setGravity(Gravity.CENTER);
     textMain.getElement().setPaintFlags(Paint.FAKE_BOLD_TEXT_FLAG);
     textMain.getElement().setPadding(Globals.screenDimensions.x/20, 0, Globals.screenDimensions.x/20, Globals.screenDimensions.y/4);
     textMain.setColour(Colour.FromRGB(255, 255, 255));
     missionSetup();
     
     ball = new ImageObject(R.drawable.ball, a, Globals.newId(), false);
     ball.alignToLeft();
     ball.alignToTop();
     ball.getElement().setPadding(Globals.screenDimensions.x/2, Globals.screenDimensions.y/2, 0, 0);
     ball.setAbsScaleX(Globals.screenDimensions.x/10);
     addElementToView(textMain);
     addElementToView(ball);
     
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
}