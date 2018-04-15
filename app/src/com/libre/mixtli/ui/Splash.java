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
import com.libre.mixtli.DetectorActivity;
import com.libre.mixtli.R;
import com.libre.mixtli.prefs.Pref;
import com.libre.mixtli.ui.animations.DotProgressBar;
import com.libre.mixtli.ui.animations.DotProgressBarBuilder;

/**
 * Created by hgallardo on 10/03/18.
 */

public class Splash extends Activity {

    private Pref prefs;
    private String REGISTER_USER_KEY="REGISTER_USER_KEY";
    private String ID_CAMERA_PREFERENCE="ID_CAMERA_PREFERENCE";
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.splash_screen);
        this.prefs=new Pref(this);
        prefs.saveData(ID_CAMERA_PREFERENCE,"0");
        new Handler().postDelayed(new Runnable(){
                @Override
                public void run() {

                    String key=prefs.loadData(REGISTER_USER_KEY);
                    if(key==null){
                        Intent registerIntent = new Intent(Splash.this,RegisterActivity.class);
                        Splash.this.startActivity(registerIntent);
                        Splash.this.finish();
                    }else{
                        Intent registerIntent = new Intent(Splash.this,DetectorActivity.class);
                        Splash.this.startActivity(registerIntent);
                        Splash.this.finish();
                    }
                }
            }, 3000);

    }


}