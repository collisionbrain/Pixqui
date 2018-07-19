package com.libre.mixtli.core;

import android.graphics.Bitmap;
import android.graphics.PointF;
import android.os.Environment;
import android.util.Log;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfRect;

import java.io.File;
import java.util.Map;

/**
 * Created by hugo on 25/05/18.
 */

public class PixquiCore {

        String faceFile;
        File externalDir ;
        String cascadeFilePath;
        File mCascadeFile;
        static{
            System.loadLibrary("opencv_java3");
            System.loadLibrary("pixqui_face_detection");



        }
        public PixquiCore(){
            faceFile = "/pdata/xml/haarcascade_frontalcatface.xml";
            externalDir = Environment.getExternalStorageDirectory();
            cascadeFilePath=externalDir.getAbsolutePath().toString().concat(faceFile);
            mCascadeFile = new File(cascadeFilePath);
            Log.d("XXXXXXXXX","EXIST : "+mCascadeFile.exists());
            Log.d("XXXXXXXXX",mCascadeFile.getAbsolutePath());
            Log.d("XXXXXXXXX"," FILE SIZE"+mCascadeFile.length());
        }

        public void detectFace(Mat inputImageMat,MatOfRect faceArea) {

            detectFace(inputImageMat.getNativeObjAddr(), faceArea.getNativeObjAddr(),mCascadeFile.getAbsolutePath());

        }




        public native  long  detectFace(long inputImage,long outputArea,String filename);

    }
