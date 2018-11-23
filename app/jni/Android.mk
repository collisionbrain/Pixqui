LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
OPENCV_INSTALL_MODULES:=on
include /home/hugo/librerias/OpenCV-android-sdk/sdk/native/jni/OpenCV.mk

LOCAL_MODULE     := pixqui_based_face_detection
LOCAL_SRC_FILES  := PixquiFaceBasedDetection.cpp
LOCAL_C_INCLUDES += $(LOCAL_PATH)

LOCAL_LDLIBS     += -llog -ldl

LOCAL_LDFLAGS += -ljnigraphics
include $(BUILD_SHARED_LIBRARY)
