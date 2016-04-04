LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

include $(LOCAL_PATH)/../config.mak

OBJS :=
OBJS-yes :=
include $(LOCAL_PATH)/Makefile
include $(LOCAL_PATH)/$(ARCH)/Makefile
include $(LOCAL_PATH)/Makefile2

include $(LOCAL_PATH)/../common.mk

LOCAL_MODULE := lib$(NAME)

LOCAL_C_INCLUDES := $(LOCAL_PATH)/.. \
                    /home/jbj/workspace/Mango_Streaming/jni/x264

LOCAL_CFLAGS := $(COMMON_CFLAGS)

NEON_ADD_C := aacdec.c
FFFILES := $(filter-out $(NEON_ADD_C),$(FFFILES))
NEON_ADD_C := $(NEON_ADD_C:.c=.c.neon)
LOCAL_SRC_FILES := $(FFFILES) $(NEON_ADD_C)

LOCAL_ARM_MODE := arm

LOCAL_STATIC_LIBRARIES := $(foreach,NAME,$(FFLIBS),lib$(NAME)) libx264 cpufeatures

include $(BUILD_STATIC_LIBRARY)

$(call import-module,android/cpufeatures)
