LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
OPENCV_INSTALL_MODULES:=on
include /home/hugo/librerias/OpenCV-android-sdk/sdk/native/jni/OpenCV.mk 

LOCAL_MODULE     := pixqui_face_detection
LOCAL_SRC_FILES  := PixquiFaceDetection.cpp PixquiFaceBasedDetection.cpp
LOCAL_C_INCLUDES += $(LOCAL_PATH)
<<<<<<< HEAD
LOCAL_LDLIBS     += -llog -ldl -ltensorflow
LOCAL_LDFLAGS += -ljnigraphics 
=======
LOCAL_LDLIBS     += -llog -ldl

LOCAL_LDFLAGS += -ljnigraphics
>>>>>>> 0540b9abfea5c4ba5205bc5eb94db0a703cb35a1
include $(BUILD_SHARED_LIBRARY)
