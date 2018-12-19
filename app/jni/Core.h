//
// Created by hugo gallardo on 12/4/18.
//

#include <jni.h>
#ifndef _Included_com_libre_mixtli_core_PixquiCore
#define _Included_com_libre_mixtli_core_PixquiCore
#endif

#ifdef __cplusplus
extern "C"{
#endif

JNIEXPORT void JNICALL Java_com_libre_mixtli_core_PixquiCore_create
        (JNIEnv * , jclass, jstring , jstring );

JNIEXPORT void JNICALL Java_com_libre_mixtli_core_PixquiCore_detect
        (JNIEnv * , jclass, jlong , jlong , jboolean );


#ifdef __cplusplus
}
#endif