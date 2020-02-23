//
// Created by vadasz on 2019.03.13..
//

#ifndef C_DEBUG_H
#define C_DEBUG_H

#include "chunk.h"

void disassembleChunk(Chunk *chunk, const char *name);

int disassembleInstruction(Chunk *chunk, int offset);

#endif //C_DEBUG_H
