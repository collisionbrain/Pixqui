package com.libre.mixtli.env;

import android.os.Environment;

/**
 * Created by hugo on 14/04/18.
 */

public class Constants {

    private String REGISTER_USER_KEY="REGISTER_USER_KEY";
    private String ID_CAMERA_PREFERENCE="ID_CAMERA_PREFERENCE";
    private String REGISTER_USER_VALUE;
    private String ID_CAMERA_BACK="O";
    private String ID_CAMERA_FRONT="1";
    private String ID_CAMERA_BLUETOOTH="2";
    private String JPG_FORMAT_FINAL="JPG_FORMAT_FINAL";
    private String MPG_FORMAT_FINAL="MPG_FORMAT_FINAL";

    public static String DIRNAME = "pdata";
    public static String XML = "xml";
    //
    public static String URL = "gs://botsec-65c94.appspot.com";
    public static String FACE_XML = "haarcascade_frontalcatface.xml";

    public static String pixquiExternalData = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + DIRNAME + "/"+XML;


}

