//
// Created by collisionbrain on 29/09/2016.
// cd ..
// OCR/app/src/jni$ ndk-build
//

#include "PixquiFaceDetection.h"
#include <jni.h>
#include <assert.h>
#include <opencv2/objdetect.hpp>
#include <opencv2/highgui.hpp>
#include <opencv2/imgproc.hpp>
#include <opencv2/object_tracking.hpp>
#include <iostream>
#include <string>
#include <algorithm>
#include <android/log.h>
#include <android/bitmap.h>
#include <algorithm>
#include <string>
#include <vector>
#include <sys/types.h>
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>
#include <android/log.h>


#define LOG_TAG "Face Detection"
#define LOGD(...) ((void)__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__))
using namespace std;
using namespace cv;

vector<Rect> detect ( Mat& img)
 {
     double t = 0;
     double scale;
     bool tryflip;
     string faceFile = "/sdcard/pdata/xml/haarcascade_frontalface_alt2.xml";

     CascadeClassifier cascade;
     cascade.load(faceFile);

     vector<Rect> faces, faces2;
     const static Scalar colors[] =
     {
         Scalar(255,0,0),
         Scalar(255,128,0),
         Scalar(255,255,0),
         Scalar(0,255,0),
         Scalar(0,128,255),
         Scalar(0,255,255),
         Scalar(0,0,255),
         Scalar(255,0,255)
     };
     Mat gray, smallImg;

     cvtColor( img, gray, COLOR_BGR2GRAY );
     double fx = 1 / scale;
     resize( gray, smallImg, Size(), fx, fx, INTER_LINEAR );
     equalizeHist( smallImg, smallImg );

     t = (double)getTickCount();
     cascade.detectMultiScale( smallImg, faces,
         1.1, 2, 0
         //|CASCADE_FIND_BIGGEST_OBJECT
         //|CASCADE_DO_ROUGH_SEARCH
         |CASCADE_SCALE_IMAGE,
         Size(30, 30) );
     if( tryflip )
     {
         flip(smallImg, smallImg, 1);
         cascade.detectMultiScale( smallImg, faces2,
                                  1.1, 2, 0
                                  //|CASCADE_FIND_BIGGEST_OBJECT
                                  //|CASCADE_DO_ROUGH_SEARCH
                                  |CASCADE_SCALE_IMAGE,
                                  Size(30, 30) );
         for( vector<Rect>::const_iterator r = faces2.begin(); r != faces2.end(); ++r )
         {
             faces.push_back(Rect(smallImg.cols - r->x - r->width, r->y, r->width, r->height));
         }
     }

    return faces;

 }

JNIEXPORT void JNICALL Java_com_libre_mixtli_core_PixquiCore_detectFace
        (JNIEnv *, jclass,jlong imageMat, jlong founded)
{
    LOGD("STARTING PROCESS ");
    Mat& imgMat=*((Mat*)imageMat);
   // vector<Rect> founded = detect(imgMat);
   //*((Mat*)squareMat) = Mat(founded, true);

}

