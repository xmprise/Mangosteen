# Copyright (C) 2009 The Android Open Source Project
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

# the purpose of this sample is to demonstrate how one can
# generate two distinct shared libraries and have them both
# uploaded in
# 
# Author : Young-Hyeon, Park (DMLab, Sungkyunkwan Univ. , South Korea)
#
# email : evo0722@gmail.com
#

LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)
# Makefile

#include $(LOCAL_PATH)/config.mak


LOCAL_MODULE := libx264
LOCAL_SRC_FILES := common/arm/cpu-a.S common/arm/pixel-a.S common/arm/mc-a.S \
       common/arm/dct-a.S common/arm/quant-a.S common/arm/deblock-a.S \
       common/arm/predict-a.S \
       common/arm/mc-c.c common/arm/predict-c.c \
       common/mc.c common/predict.c common/pixel.c common/macroblock.c \
       common/frame.c common/dct.c common/cpu.c common/cabac.c \
       common/common.c common/osdep.c common/rectangle.c \
       common/set.c common/quant.c common/deblock.c common/vlc.c \
       common/mvpred.c common/bitstream.c \
       encoder/analyse.c encoder/me.c encoder/ratecontrol.c \
       encoder/set.c encoder/macroblock.c encoder/cabac.c \
       encoder/cavlc.c encoder/encoder.c encoder/lookahead.c \
       x264.c input/input.c input/timecode.c input/raw.c input/y4m.c \
       output/raw.c output/matroska.c output/matroska_ebml.c \
       output/flv.c output/flv_bytestream.c filters/filters.c \
       filters/video/video.c filters/video/source.c filters/video/internal.c \
       filters/video/resize.c filters/video/cache.c filters/video/fix_vfr_pts.c \
       filters/video/select_every.c filters/video/crop.c filters/video/depth.c


CONFIG := $(shell cat $(LOCAL_PATH)/config.h)


ifneq ($(findstring HAVE_THREAD 1, $(CONFIG)),)
LOCAL_SRC_FILES  += common/threadpool.c input/thread.c
endif

ifneq ($(findstring HAVE_WIN32THREAD 1, $(CONFIG)),)
LOCAL_SRC_FILES += common/win32thread.c
endif

LOCAL_CFLAGS += "-std=gnu99"
TARGET_ARCH := arm
TARGET_PLATFORM := android-8
TARGET_ARCH_ABI := armeabi-v7a
LOCAL_ARM_NEON := true
LOCAL_ARM_MODE := arm
#LOCAL_LDLIBS := -L$(SYSROOT)/usr/lib -llog


include $(BUILD_STATIC_LIBRARY)
