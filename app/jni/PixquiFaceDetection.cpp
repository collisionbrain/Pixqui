//
// Created by collisionbrain on 29/09/2016.
// cd ..
// OCR/app/src/jni$ ndk-build
//

#include "PixquiFaceDetection.h"
#include <jni.h>
#include <opencv2/opencv.hpp>
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/features2d/features2d.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <string>
#include <algorithm>
#include <android/log.h>
#include <android/bitmap.h>
#include <algorithm>
#include <string>
#include <vector>




#define LOG_TAG "CcrCore"
#define LOGD(...) ((void)__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__))
using namespace std;
using namespace cv;

double angle(Point pt1, Point pt2, Point pt0)
{
    double dx1 = pt1.x - pt0.x;
    double dy1 = pt1.y - pt0.y;
    double dx2 = pt2.x - pt0.x;
    double dy2 = pt2.y - pt0.y;
    return (dx1*dx2 + dy1*dy2)/sqrt((dx1*dx1 + dy1*dy1)*(dx2*dx2 + dy2*dy2) + 1e-10);
}

void findLargestSquare(const vector< vector<Point> >& squares,
                       vector<Point>& biggest_square)
{
    if (!squares.size())
    {

        return;
    }

    int max_width = 0;
    int max_height = 0;
    int max_square_idx = 0;
    for (size_t i = 0; i < squares.size(); i++)
    {
        Rect rectangle = boundingRect(Mat(squares[i]));
        if ((rectangle.width >= max_width) && (rectangle.height >= max_height))
        {
            max_width = rectangle.width;
            max_height = rectangle.height;
            max_square_idx = i;
        }
    }

    biggest_square = squares[max_square_idx];
}
vector<Point> getCardArea(Mat image){

    vector<vector<Point> > squares;
    vector<Point> largest_square;
    vector<Rect> rectFounded;
    int newHeigth= image.cols/4;
	int newWidth= image.rows/4;
	Size size(newHeigth,newWidth);
	Mat resized;
	resize(image,resized,size);

	Mat src_gray,filtered;
	cvtColor(resized, src_gray, CV_BGR2GRAY);
    blur(src_gray, filtered, Size(3, 3));
    Mat edges;
    int thresh = 128;
    Canny(filtered, edges, 10,100, 3);
    Mat dilated_edges;
    dilate(edges, dilated_edges,Mat(),  Point(-1, -1), 2, 1, 1);
    vector< vector<Point> > contours;
    findContours(dilated_edges, contours,RETR_LIST, CHAIN_APPROX_SIMPLE);
    vector<Point> approx;
    for (size_t i = 0; i < contours.size(); i++)
        {
            approxPolyDP(Mat(contours[i]), approx,arcLength(Mat(contours[i]), true)*0.02, true);
            if (approx.size() == 4 && fabs(contourArea(Mat(approx))) > 1000 &&
                isContourConvex(Mat(approx)))
            {
                double maxCosine = 0;
                for (int j = 2; j < 5; j++)
                {
                    double cosine =  fabs(angle(approx[j%4], approx[j-2], approx[j-1]));
                    maxCosine = MAX(maxCosine, cosine);
                }

                if (maxCosine < 0.3)
                    squares.push_back(approx);
            }
        }
         findLargestSquare(squares, largest_square);
         //Rect rectangle = boundingRect(Mat(largest_square));
         //rectFounded.push_back(rectangle);
         return  largest_square;
}



vector<Rect> getLettersArea(Mat word){
    Mat  pMatSmall;
    vector<Rect> roiFounded;
    Mat pMatGray=word.clone();
    Size size(3,3);
    GaussianBlur(pMatGray,pMatGray,size,0);
    adaptiveThreshold(pMatGray,pMatGray,255,ADAPTIVE_THRESH_GAUSSIAN_C,  THRESH_BINARY,11,2);
    Mat pMatBw;

    vector < vector<Point> > contours;
    vector <Vec4i> hierarchy;
    findContours(pMatGray, contours, hierarchy, CV_RETR_LIST, CHAIN_APPROX_SIMPLE, Point(0, 0) );
    for(int i= 0; i < contours.size(); i++)
    {
        if (contourArea(contours[i]) > 50 ){
            Rect rect = boundingRect(contours[i]);
            int midWidth=pMatBw.cols/2;
            int midHeigth=pMatBw.rows/2;
            int z=pMatBw.rows/4;
            int zx=pMatBw.rows-z;
            int h=pMatBw.rows;
            if (rect.height >=zx  && rect.width < midWidth ){
                Rect roi= Rect(
                        Point(rect.x, rect.y),
                        Point(rect.x + rect.width, rect.y +  (h-rect.y )));
                roiFounded.push_back(roi);
            }

        }


    }

    return roiFounded;
}

vector<Rect>  getTextArea (Mat img)
{

    LOGD("CLONING MAT ");
    Mat pMatLarge=img.clone();
    LOGD("FINISH CLONE");
    Mat  pMatRgb;
    Mat  pMatSmall;
    vector<Rect> rectTextFounded;
    LOGD("INSIDE FUNCTION");
    try{
        Mat pMatGrd;
        LOGD("START MORPH_ELLIPSE");
        Mat morphKernel = getStructuringElement(MORPH_ELLIPSE, Size(3, 3));
        LOGD("FINISH MORPH_ELLIPSE");
        LOGD("INSIDE FUNCTION : morphologyEx");
        morphologyEx(pMatLarge, pMatGrd, MORPH_GRADIENT, morphKernel);
        LOGD("INSIDE FUNCTION : morphologyEx");
        Mat pMatBw;
        threshold(pMatGrd, pMatBw,0.0, 255.0, THRESH_BINARY | THRESH_OTSU);

        Mat pMatConn;
        morphKernel = getStructuringElement(MORPH_RECT, Size(9,1));
        morphologyEx(pMatBw, pMatConn, MORPH_CLOSE, morphKernel );
        Mat mask = Mat::zeros(pMatBw.size(), CV_8UC1);
        vector < vector<Point> > contours;
        vector <Vec4i> hierarchy;
        findContours(pMatConn, contours, hierarchy, CV_RETR_CCOMP, CV_CHAIN_APPROX_SIMPLE, Point(0, 0) );
        for( int idx = 0; idx >= 0; idx = hierarchy[idx][0] )
        {
            Rect rect = boundingRect(contours[idx]);
            Mat maskROI(mask, rect);
            maskROI = Scalar(0, 0, 0);
            drawContours(mask, contours, idx, Scalar(255, 255, 255), CV_FILLED);
            double r = (double)countNonZero(maskROI)/(rect.width*rect.height);
            if (r > .45   && (rect.height > 8 && rect.width > 8)  )
            {
                rectTextFounded.push_back(rect);

            }

        }
         return rectTextFounded;
        }catch(Exception& exception){
        LOGD("===================> EXCEPTION");
        }
}

Point2f computePoint(int p1, int p2) {
    Point2f pt;
    pt.x = p1;
    pt.y = p2;
    return pt;
}

int numberdiff_pixels(Mat in1, Mat in2) {
    Mat diff;
    compare(in1, in2, diff, CMP_NE);
    return countNonZero(diff);
}
Mat getCorrectDocument(Mat img, jfloat x1, jfloat y1, jfloat x2, jfloat y2, jfloat x3, jfloat y3, jfloat x4, jfloat y4) {

                           // define the destination image size:

                          float w1 = sqrt( pow(x4 - x3 , 2) + pow(x4 - x3, 2));
                          float w2 = sqrt( pow(x2 - x1 , 2) + pow(x2-x1, 2));
                          float h1 = sqrt( pow(y2 - y4 , 2) + pow(y2 - y4, 2));
                          float h2 = sqrt( pow(y1 - y3 , 2) + pow(y1-y3, 2));

                          float maxWidth = (w1 < w2) ? w1 : w2;
                          float maxHeight = (h1 < h2) ? h1 : h2;

                          Mat dst = Mat::zeros(maxHeight, maxWidth, CV_8UC3);

                          // corners of destination image with the sequence [tl, tr, bl, br]
                          vector<Point2f> dst_pts, img_pts;
                          dst_pts.push_back(Point(0, 0));
                          dst_pts.push_back(Point(maxWidth - 1, 0));
                          dst_pts.push_back(Point(0, maxHeight - 1));
                          dst_pts.push_back(Point(maxWidth - 1, maxHeight - 1));

                          img_pts.push_back(computePoint(x1,y1));
                          img_pts.push_back(computePoint(x2,y2));
                          img_pts.push_back(computePoint(x3,y3));
                          img_pts.push_back(computePoint(x4,y4));

                          // get transformation matrix
                          Mat transmtx = getPerspectiveTransform(img_pts, dst_pts);
                          // apply perspective transformation
                          warpPerspective(img, dst, transmtx, dst.size());

                          return dst;
                      }


jobject mat_to_bitmap(JNIEnv * env, Mat & src, bool needPremultiplyAlpha, jobject bitmap_config){
    jclass java_bitmap_class = (jclass)env->FindClass("android/graphics/Bitmap");
    jmethodID mid = env->GetStaticMethodID(java_bitmap_class,
                                           "createBitmap", "(IILandroid/graphics/Bitmap$Config;)Landroid/graphics/Bitmap;");

    jobject bitmap = env->CallStaticObjectMethod(java_bitmap_class,
                                                 mid, src.size().width, src.size().height, bitmap_config);
    AndroidBitmapInfo  info;
    void*              pixels = 0;

    try {
        CV_Assert(AndroidBitmap_getInfo(env, bitmap, &info) >= 0);
        CV_Assert(src.type() == CV_8UC1 || src.type() == CV_8UC3 || src.type() == CV_8UC4);
        CV_Assert(AndroidBitmap_lockPixels(env, bitmap, &pixels) >= 0);
        CV_Assert(pixels);
        if(info.format == ANDROID_BITMAP_FORMAT_RGBA_8888){
            Mat tmp(info.height, info.width, CV_8UC4, pixels);
            if(src.type() == CV_8UC1){
                cvtColor(src, tmp, CV_GRAY2RGBA);
            }else if(src.type() == CV_8UC3){
                cvtColor(src, tmp, CV_RGB2RGBA);
            }else if(src.type() == CV_8UC4){
                if(needPremultiplyAlpha){
                    cvtColor(src, tmp, COLOR_RGBA2mRGBA);
                }else{
                    src.copyTo(tmp);
                }
            }
        }else{
            // info.format == ANDROID_BITMAP_FORMAT_RGB_565
            Mat tmp(info.height, info.width, CV_8UC2, pixels);
            if(src.type() == CV_8UC1){
                cvtColor(src, tmp, CV_GRAY2BGR565);
            }else if(src.type() == CV_8UC3){
                cvtColor(src, tmp, CV_RGB2BGR565);
            }else if(src.type() == CV_8UC4){
                cvtColor(src, tmp, CV_RGBA2BGR565);
            }
        }
        AndroidBitmap_unlockPixels(env, bitmap);
        return bitmap;
    }catch(cv::Exception e){
        AndroidBitmap_unlockPixels(env, bitmap);
        jclass je = env->FindClass("org/opencv/core/CvException");
        if(!je) je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, e.what());
        return bitmap;
    }catch (...){
        AndroidBitmap_unlockPixels(env, bitmap);
        jclass je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, "Unknown exception in JNI code {nMatToBitmap}");
        return bitmap;
    }
}
JNIEXPORT void JNICALL Java_libre_com_ocrsmart_core_OcrCore_getCardArea
        (JNIEnv *, jclass,jlong imageMat, jlong squareMat)
{
    LOGD("STARTING PROCESS ");
    Mat& imgMat=*((Mat*)imageMat);
    LOGD("GET CARD AREA");
    vector<Point> founded = getCardArea(imgMat);
   *((Mat*)squareMat) = Mat(founded, true);

}
JNIEXPORT void JNICALL Java_libre_com_ocrsmart_core_OcrCore_getTextArea
        (JNIEnv *, jclass,jlong wordMat, jlong letterMat)
{
    LOGD("STARTING PROCESS");
    Mat& imgMat=*((Mat*)wordMat);
    LOGD("GET TEXT AREA");
    vector<Rect> letteres = getTextArea(imgMat);
    *((Mat*)letterMat) = Mat(letteres, true);


}
JNIEXPORT void JNICALL Java_libre_com_ocrsmart_core_OcrCore_getLetters
        (JNIEnv *, jclass,jlong wordMat, jlong letterMat)
{
    LOGD("STARTING PROCESS");
    Mat& imgMat=*((Mat*)wordMat);
    LOGD("GET TEXT BY LETTER");
    vector<Rect> letteres = getLettersArea(imgMat);
    *((Mat*)letterMat) = Mat(letteres, true);

}
JNIEXPORT jobject JNICALL  Java_libre_com_ocrsmart_core_OcrCore_getCorrectDocument
(JNIEnv *env, jobject thiz, jobject bitmap,jfloat x1,jfloat y1,jfloat x2, jfloat y2, jfloat x3,jfloat y3,jfloat x4,jfloat y4)
{
    int ret;
    AndroidBitmapInfo info;
    void* pixels = 0;

    if ((ret = AndroidBitmap_getInfo(env, bitmap, &info)) < 0) {
         return NULL;
    }

    if (info.format != ANDROID_BITMAP_FORMAT_RGBA_8888 )
    {    return NULL;
    }

    if ((ret = AndroidBitmap_lockPixels(env, bitmap, &pixels)) < 0) {
         }

    // init our output image
    Mat mbgra(info.height, info.width, CV_8UC4, pixels);
    Mat dst = getCorrectDocument(mbgra,x1,y1,x2,y2,x3,y3,x4,y4);

    //get source bitmap's config
    jclass java_bitmap_class = (jclass)env->FindClass("android/graphics/Bitmap");
    jmethodID mid = env->GetMethodID(java_bitmap_class, "getConfig", "()Landroid/graphics/Bitmap$Config;");
    jobject bitmap_config = env->CallObjectMethod(bitmap, mid);
    jobject _bitmap = mat_to_bitmap(env,dst,false,bitmap_config);

    AndroidBitmap_unlockPixels(env, bitmap);
    return _bitmap;
}
