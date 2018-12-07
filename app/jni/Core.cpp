//
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
#define LOGD(...) ((void)__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__))
using namespace std;
using namespace cv;
using namespace cv::dnn;

static const  String input="Mul";
static const  String output="final_result";


class Core
{

private:

public:

        Core();
        virtual ~Core();
        dnn::Net netDetector;
        string stdFileNameModel;
        string stdFileNameLabel;

        dnn::Net create( const char* jmodeltr , const char* jlabeltr)
        {


             stdFileNameModel=jmodeltr;
             stdFileNameLabel=jlabeltr;
             netDetector=dnn::readNetFromTensorflow(stdFileNameModel);
             return netDetector;

        }
        void destroy();
        void getMaxClass(const Mat &probBlob,int *classId,double *classProb)
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
        bool detectObject(const cv::Mat &img)
         {
          if(netDetector.empty())
             {
              LOGD("Error loading model ");
             }

             Mat inputBlob=blobFromImage(img,1.0f,Size(299,299),Scalar(),true,false);
             netDetector.setInput(inputBlob,input);
             Mat result = netDetector.forward(output);
             std::vector<String> classNames = readClassNames(stdFileNameLabel.c_str());
             int classId;
             double classProb;
             getMaxClass(result, &classId, &classProb);//find the best class
             std::cout << "Best class: #" << classId << " '" << classNames.at(classId) << "'" << std::endl;
             std::cout << "Probability: " << classProb * 100 << "%  daisy" <<  result.at<float>(0) << ",dandelion" << result.at<float>(1) << ",roses" << result.at<float>(2) << ",sunflower" << result.at<float>(3) << ",tulip" << result.at<float>(4) << std::endl;
             return false;
         }







};



JNIEXPORT jlong JNICALL Java_com_libre_mixtli_core_PixquiCore_createObject
(JNIEnv * jenv, jclass, jstring jFileNameModel, jstring jFileNameLabel)
{
    const char* jmodeltr = jenv->GetStringUTFChars(jFileNameModel, NULL);
    const char* jlabeltr = jenv->GetStringUTFChars(jFileNameLabel, NULL);
    jlong result = 0;

    return result;
}

JNIEXPORT void JNICALL Java_com_libre_mixtli_core_PixquiCore_detect
(JNIEnv * jenv, jclass, jlong thiz, jlong imageGray, jlong faces)
{

}


