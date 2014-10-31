package com.example.gamecity;

import com.scenelibrary.classes.AccelerometerManager;
import com.scenelibrary.classes.Colour;
import com.scenelibrary.classes.Globals;
import com.scenelibrary.classes.LayoutManager;
import com.scenelibrary.classes.NfcSceneActivity;
import com.scenelibrary.classes.SceneActivity;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.Toast;

public class MainActivity extends NfcSceneActivity {

	SceneMain sceneMain;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		GradientDrawable gd = new GradientDrawable(
	            GradientDrawable.Orientation.TOP_BOTTOM,
	            new int[] {Colour.FromRGB(110, 145, 255), Colour.FromRGB(255, 110, 228)});
	    gd.setCornerRadius(0f);
	    this.getLayout().get().setBackgroundDrawable(gd);

		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE); 
		Globals.Init(this);
		AccelerometerManager.Init(this);
		sceneMain = new SceneMain(0, this, false);
		
		AddScene(sceneMain);
		
		InitScenes();
		
		SetScreenState(sceneMain);
	}
	
	@Override
    public boolean onTouchEvent(MotionEvent event) {
		
		if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
			sceneMain.ScreenPressed();
	    } else if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
	    	sceneMain.ScreenReleased();
	    }
		
        return super.onTouchEvent(event);
    }
	
	protected void handleNfcScanned(String in, Activity a){ // Called from 'handleIntent' if intent is NFC.
		sceneMain.NfcScanned(in, a);
	}
	
	@Override
 	public void onNewIntent(Intent intent){
 		this.handleIntent(intent, this);
 	} 
}
