// Created by hugo gallardo on 12/4/18.
//

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
#define LOG_TAG "Pixqui Core"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

using namespace std;
using namespace cv;
using namespace cv::dnn;

static const  String input="Mul";
static const  String output="final_result";


class Core
{

private:
        dnn::Net netDetector;
        string stdFileNameModel;
        string stdFileNameLabel;

public:
       cv::dnn::Net create( const char* jmodeltr , const char* jlabeltr);


};

cv::dnn::Net Core::create( const char* jmodeltr , const char* jlabeltr)
{


      stdFileNameModel=jmodeltr;
      stdFileNameLabel=jlabeltr;
      netDetector=dnn::readNetFromTensorflow(stdFileNameModel);
      return netDetector;
}

struct Detector
{
     string model;
     string label;
     cv::dnn::Net detector;
     Core core;
     Detector(string _model,string _label):model(_model),label(_label)
     {

         detector=core.create(model.c_str(),label.c_str());

     }


};


void  getMaxClass(const Mat &probBlob,int *classId,double *classProb)
        {
            Mat probMat =probBlob.reshape(1,1);
            Point classNumber;
            minMaxLoc(probMat,NULL,classProb,NULL,&classNumber);
            *classId=classNumber.x;
        }
std::vector<String> readClassNames(const char *fileName)
        {
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

JNIEXPORT jlong JNICALL Java_com_libre_mixtli_core_PixquiCore_create
(JNIEnv * jenv, jclass, jstring jFileNameModel, jstring jFileNameLabel)
{
    const char* jmodeltr = jenv->GetStringUTFChars(jFileNameModel, NULL);
    const char* jlabeltr = jenv->GetStringUTFChars(jFileNameLabel, NULL);
    return (jlong)new Detector(jmodeltr,jlabeltr);


}


JNIEXPORT void JNICALL Java_com_libre_mixtli_core_PixquiCore_detect
(JNIEnv * jenv, jclass, jlong thiz, jlong image, jboolean detected)
{

    int classId;
    double classProb;
    Mat inputBlob=blobFromImage(*((Mat*)image),1.0f,Size(299,299),Scalar(),true,false);
    ((Detector*)thiz)->detector.setInput(inputBlob,input);
    Mat result =  ((Detector*)thiz)->detector.forward(output);
    getMaxClass(result, &classId, &classProb);
    double prob=classProb * 100;

    LOGD("Probabilidad de deteccion: %f",prob);
    LOGD("Tipo de Objeto: %d",classId);

    if(prob >= 90)
     {
     detected=true;
     }else{
     detected=false;
     }
}

JNIEXPORT void JNICALL Java_com_libre_mixtli_core_PixquiCore_destroy
(JNIEnv * jenv, jclass, jlong thiz)
{

  delete (Detector*)thiz;

}
