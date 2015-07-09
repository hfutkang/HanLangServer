NEED_CHANGED_SO_PATH_TO := armeabi
LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)
LOCAL_PROGUARD_ENABLED := full 
LOCAL_MODULE_TAGS := optional
#LOCAL_MODULE_PATH := $(PRODUCT_OUT)/

LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_STATIC_JAVA_LIBRARIES :=  \
	android-support-v7-appcompat \
	android-support-v4 \
	IngenicGlassSDK\

LOCAL_JNI_SHARED_LIBRARIES := libSmartGlassesLedTest
LOCAL_REQUIRED_MODULES := libSmartGlassesLedTest

LOCAL_RESOURCE_DIR = \
    $(LOCAL_PATH)/res \
    frameworks/support/v7/appcompat/res \

LOCAL_AAPT_FLAGS := \
	--auto-add-overlay \
	--extra-packages android.support.v7.appcompat

LOCAL_PACKAGE_NAME := SmartGlasses_Server

# Builds against the public SDK
#LOCAL_SDK_VERSION := current

include $(BUILD_PACKAGE)

include $(CLEAR_VARS)
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES += IngenicGlassSDK:libs/IngenicGlassSDK.jar
LOCAL_CERTIFICATE := platform  
include $(BUILD_MULTI_PREBUILT)  

# This finds and builds the test apk as well, so a single make does both.
include $(call all-makefiles-under,$(LOCAL_PATH))
