package com.libre.mixtli.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.libre.mixtli.CameraActivity;
import com.libre.mixtli.R;
import com.libre.mixtli.ui.animations.DotProgressBar;
import com.libre.mixtli.ui.animations.DotProgressBarBuilder;

/**
 * Created by hgallardo on 10/03/18.
 */

public class Splash extends Activity {

    /** Duration of wait **/
    private final int SPLASH_DISPLAY_LENGTH = 1000;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.splash_screen);
        DotProgressBar dotProgressBar = (DotProgressBar) findViewById(R.id.progress_bar);

        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                /* Create an Intent that will start the Menu-Activity. */
                Intent mainIntent = new Intent(Splash.this,CameraActivity.class);
                Splash.this.startActivity(mainIntent);
                Splash.this.finish();
            }
        }, SPLASH_DISPLAY_LENGTH);
    }
}