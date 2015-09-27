#
# Copyright (C) 2008 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_MODULE := MyVoiceRecognizer

LOCAL_SRC_FILES := $(LOCAL_MODULE).apk

LOCAL_MODULE_CLASS := APPS

LOCAL_MODULE_SUFFIX := $(COMMON_ANDROID_PACKAGE_SUFFIX)

LOCAL_MODULE_PATH := $(TARGEI_OUT_DATA_APPS)

LOCAL_CERTIFICATE := platform

include $(BUILD_PREBUILT)

###########################
include $(CLEAR_VARS)

RECOG_TYPE := xf
ifeq ($(RECOG_TYPE), yzs)
LOCAL_PREBUILT_LIBS := libasrfix:libs/mips/libasrfix.so libuscasr:libs/mips/libuscasr.so 
else
LOCAL_PREBUILT_LIBS := libmsc:libs/mips/libmsc.so
endif

LOCAL_PREBUILT_LIBS += libsc_blur:libs/mips/libsc_blur.so libyzstts:libs/mips/libyzstts.so
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := VoiceRecognizerAPI:libs/VoiceRecognizerAPI.jar
LOCAL_MODULE_TAGS := optional
include $(BUILD_MULTI_PREBUILT)