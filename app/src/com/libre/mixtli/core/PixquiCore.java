package com.libre.mixtli.core;

import android.graphics.Bitmap;
import android.graphics.PointF;
import android.util.Log;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfRect;

import java.util.Map;

/**
 * Created by hugo on 25/05/18.
 */

public class PixquiCore {

        private Mat inputImageMat;
        private MatOfRect txtArea;

        static{
            System.loadLibrary("opencv_java3");
            System.loadLibrary("pixqui_face_detection");



        }
        public PixquiCore(){

        }

        public void detectFace(MatOfPoint cardArea) {

            detectFace(inputImageMat.getNativeObjAddr(), cardArea.getNativeObjAddr());

        }



        public void destroyObject(){
            this.inputImageMat=null;
        }
        public native  long  detectFace(long inputImage,long outputArea);

    }
