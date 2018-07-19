#include <jni.h>
#ifndef _Included_com_libre_mixtli_core_PixquiCore
#define _Included_com_libre_mixtli_core_PixquiCore
#endif

#ifdef __cplusplus
extern "C"{
#endif

JNIEXPORT void JNICALL Java_com_libre_mixtli_core_PixquiCore_detectFace
        (JNIEnv *, jclass, jlong, jlong ,jstring);


#ifdef __cplusplus
}
#endif