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
#include <tensorflow/c/c_api.h>
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



JNIEXPORT void JNICALL Java_com_libre_mixtli_core_PixquiCore_detectFace
        (JNIEnv *, jclass,jlong imageMat, jlong squareMat)
{
    LOGD("STARTING PROCESS ");
     

}

