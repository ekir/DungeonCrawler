package com.example.ekir.project;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import java.util.Random;


/**
 * This activity handles the game itself
 */
public class MainActivity extends Activity {
    DungeonCrawler packmanHunt;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        packmanHunt = new DungeonCrawler(this);
        setContentView(packmanHunt);
        // http://stackoverflow.com/questions/12388771/how-to-set-activity-to-fullscreen-mode-in-android
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            packmanHunt.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);}
        /* immersive mode
        * https://developer.android.com/training/system-ui/immersive.html
        */
    }

    @Override public void onResume() {
        super.onResume();
        packmanHunt.resume();
    }

    @Override public void onPause() {
        super.onPause();
        packmanHunt.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        packmanHunt.destroy();
    }




}