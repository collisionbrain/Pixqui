package com.libre.mixtli.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.libre.mixtli.ClassifierActivity;
import com.libre.mixtli.DetectorActivity;
import com.libre.mixtli.R;
import com.libre.mixtli.prefs.Pref;
import com.libre.mixtli.prefs.Utils;

import static com.libre.mixtli.env.Constants.pixquiExternalData;

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
        Utils.createDirs(pixquiExternalData);
        new Handler().postDelayed(new Runnable(){
                @Override
                public void run() {

                    String key=prefs.loadData(REGISTER_USER_KEY);
                    if(key==null){
                        Intent registerIntent = new Intent(Splash.this,RegisterActivity.class);
                        Splash.this.startActivity(registerIntent);
                        Splash.this.finish();
                    }else{
                        Intent registerIntent = new Intent(Splash.this,ClassifierActivity.class);
                        Splash.this.startActivity(registerIntent);
                        Splash.this.finish();
                    }
                }
            }, 3000);

    }


}