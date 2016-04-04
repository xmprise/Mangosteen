LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := libsegmenter
LOCAL_SRC_FILES := segmenter.c cmdutils.c encoding.c

LOCAL_C_INCLUDES := $(LOCAL_PATH)/../ffmpeg/ \
                    $(LOCAL_PATH)/../ffmpeg/libavcodec \
                    $(LOCAL_PATH)/../ffmpeg/libavformat \
                    $(LOCAL_PATH)/../ffmpeg/libswscale \
                    $(LOCAL_PATH)/../ffmpeg/libavutil \
                    $(LOCAL_PATH)/../ffmpeg/libavfilter

LOCAL_STATIC_LIBRARIES := libavformat libavcodec libswscale libavutil libavfilter libx264 cpufeatures

LOCAL_LDLIBS := -lz -llog	

LOCAL_ARM_MODE := arm

include $(BUILD_SHARED_LIBRARY)

$(call import-module,android/cpufeatures)
