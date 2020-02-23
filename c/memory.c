//
// Created by vadasz on 2019.03.13..
//

#include <stdlib.h>

#include "common.h"
#include "memory.h"

void *reallocate(void *previous, size_t oldSize, size_t newSize) {
    if (newSize == 0) {
        free(previous);
        return NULL;
    }
    return realloc(previous, newSize);
}