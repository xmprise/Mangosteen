COMMON_CFLAGS := -DHAVE_AV_CONFIG_H -D_ISOC99_SOURCE -D_FILE_OFFSET_BITS=64 -D_LARGEFILE_SOURCE -D_POSIX_C_SOURCE=200112 -D_XOPEN_SOURCE=600 -DPIC -std=c99 -fomit-frame-pointer -fPIC -fno-math-errno -fno-signed-zeros -fno-tree-vectorize

OBJS += $(OBJS-yes)

ALL_S_FILES := $(wildcard $(LOCAL_PATH)/$(ARCH)/*.S)
ALL_S_FILES := $(addprefix $(ARCH)/,$(notdir $(ALL_S_FILES)))

NEON_S_FILES := $(wildcard $(LOCAL_PATH)/$(ARCH)/*_neon.S)
NEON_S_FILES := $(addprefix $(ARCH)/,$(notdir $(NEON_S_FILES)))

NEON_C_FILES := $(wildcard $(LOCAL_PATH)/$(ARCH)/*_neon.c)
NEON_C_FILES := $(addprefix $(ARCH)/,$(notdir $(NEON_C_FILES)))

S_FILES := $(filter-out $(NEON_S_FILES),$(ALL_S_FILES))

C_OBJS := $(OBJS)
ifneq ($(S_FILES),)
S_OBJS := $(S_FILES:.S=.o)
S_OBJS := $(filter $(S_OBJS),$(C_OBJS))
C_OBJS := $(filter-out $(S_OBJS),$(C_OBJS))
else
S_OBJS :=
endif

ifneq ($(NEON_S_FILES),)
NEON_S_OBJS := $(NEON_S_FILES:.S=.o)
NEON_S_OBJS := $(filter $(NEON_S_OBJS),$(C_OBJS))
C_OBJS := $(filter-out $(NEON_S_OBJS),$(C_OBJS))
else
NEON_S_OBJS :=
endif

ifneq ($(NEON_C_FILES),)
NEON_C_OBJS := $(NEON_C_FILES:.c=.o)
NEON_C_OBJS := $(filter $(NEON_C_OBJS),$(C_OBJS))
C_OBJS := $(filter-out $(NEON_C_OBJS),$(C_OBJS))
else
NEON_C_OBJS :=
endif

C_FILES := $(C_OBJS:.o=.c)
S_FILES := $(S_OBJS:.o=.S)
NEON_C_FILES := $(NEON_C_OBJS:.o=.c.neon)
NEON_S_FILES := $(NEON_S_OBJS:.o=.S.neon)

FFFILES := $(sort $(NEON_S_FILES)) $(sort $(NEON_C_FILES)) $(sort $(S_FILES)) $(sort $(C_FILES))
