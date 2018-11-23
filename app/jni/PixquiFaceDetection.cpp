//
// Created by collisionbrain on 29/09/2016.
// cd ..
//export PATH=$PATH:/home/hugo/Android/Ndk/android-ndk/
// PROJECT/app/src/jni$ ndk-build
//

#include "PixquiFaceDetection.h"
#include <jni.h>
#include <assert.h>
#include <opencv2/objdetect.hpp>
#include <opencv2/highgui.hpp>
#include <opencv2/imgproc.hpp>
#include <opencv2/dnn.hpp>
#include <fstream>
#include <iostream>
#include <cstdlib>
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
using namespace cv::dnn;


void getMaxClass(const Mat &probBlob,int *classId,double *classProb)
{
    Mat probMat =probBlob.reshape(1,1);
    Point classNumber;
    minMaxLoc(probMat,NULL,classProb,NULL,&classNumber);
    *classId=classNumber.x;
}

std::vector<String> readClassNames(const char *fileName){
    std::vector<String> classNames;
    std::ifstream fp(fileName);
    if(!fp.is_open())
    {
    LOGD("Error loading labels ");

    }
    std::string name;
    while(!fp.eof())
    {
        std::getline(fp,name);
        if(name.length())
        {
            classNames.push_back(name);
        }
    }
    fp.close();
    return classNames;

}

jboolean detectObject(Mat& img,string& fileModel,string& fileLabels)
{

    String input="Mul";
    String output="final_result";

    dnn::Net net=dnn::readNetFromTensorflow(fileModel);
    if(net.empty())
    {
     LOGD("Error loading model ");
    }

    Mat inputBlob=blobFromImage(img,1.0f,Size(299,299),Scalar(),true,false);
    net.setInput(inputBlob,input);
    Mat result = net.forward(output);
    std::vector<String> classNames = readClassNames(fileLabels.c_str());
    int classId;
    double classProb;
    getMaxClass(result, &classId, &classProb);//find the best class
    std::cout << "Best class: #" << classId << " '" << classNames.at(classId) << "'" << std::endl;
    std::cout << "Probability: " << classProb * 100 << "%  daisy" <<  result.at<float>(0) << ",dandelion" << result.at<float>(1) << ",roses" << result.at<float>(2) << ",sunflower" << result.at<float>(3) << ",tulip" << result.at<float>(4) << std::endl;
    return false;
}



vector<Rect> detectPerson( Mat& img,string& fileCascade)
 {
     double t = 0;
     double scale;
     bool tryflip;



     CascadeClassifier cascade;
    if( !cascade.load( fileCascade ) ){ LOGD("--(!)Error loading File"); };



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
     LOGD("VARIABLES AHS BEEN CREATED ");
     cvtColor( img, gray, COLOR_BGR2GRAY );
     LOGD("SETTED GRAY COLOR ");
     double fx = 1 / scale;
     //resize( gray, smallImg, Size(), fx, fx, INTER_LINEAR );
     equalizeHist( gray, gray );

     t = (double)getTickCount();
     cascade.detectMultiScale( gray, faces,
         1.1, 2, 0
          |CASCADE_FIND_BIGGEST_OBJECT,
         //|CASCADE_DO_ROUGH_SEARCH
         //|CASCADE_SCALE_IMAGE,
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
        (JNIEnv* jenv, jclass,jlong imageMat, jlong squareMat,jstring jFileName)
{
    LOGD("STARTING PROCESS ");

    Mat& imgMat=*((Mat*)imageMat);
    LOGD("SET INPUT MATERIAL  ");
    const char* jnamestr = jenv->GetStringUTFChars(jFileName, NULL);
    string stdFileName(jnamestr);
    LOGD("SET URL STRING  ");
    CascadeClassifier cascade;
    if( !cascade.load( stdFileName ) ){ LOGD("--(!)Error loading File"); };
    LOGD("LOADED FILE");
    //vector<Rect> founded = detect(imgMat,stdFileName);
     LOGD("*************");
    LOGD("FAOUDED FACES FINISH  ");
   //*((Mat*)squareMat) = Mat(founded, true);

}

