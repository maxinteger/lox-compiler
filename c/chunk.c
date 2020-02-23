//
// Created by vadasz on 2019.03.13..
//

#include <stdlib.h>

#include "chunk.h"
#include "memory.h"
#include "value.h"
#include "debug.h"

void initChunk(Chunk *chunk) {
    chunk->count = 0;
    chunk->capacity = 0;
    chunk->code = NULL;
    initCodeLines(&chunk->lines);
    initValueArray(&chunk->constants);
}

void freeChunk(Chunk *chunk) {
    FREE_ARRAY(uint8_t, chunk->code, chunk->capacity);
    freeCodeLines(&chunk->lines);
    freeValueArray(&chunk->constants);
    initChunk(chunk);
}

void writeChunk(Chunk *chunk, uint8_t byte, int line) {
    if (chunk->count < chunk->capacity + 1) {
        int oldCapacity = chunk->capacity;
        chunk->capacity = GROW_CAPACITY(oldCapacity);
        chunk->code = GROW_ARRAY(chunk->code, uint8_t, oldCapacity, chunk->capacity);
    }
    chunk->code[chunk->count] = byte;
    writeCodeLines(&chunk->lines, line);
    chunk->count++;
}

int addConstant(Chunk *chunk, Value value) {
    writeValueArray(&chunk->constants, value);
    return chunk->constants.count - 1;
}


///

void initCodeLines(CodeLines *codeLines) {
    codeLines->capacity = 0;
    codeLines->lineCounts = NULL;
}

void freeCodeLines(CodeLines *codeLines) {
    FREE_ARRAY(uint8_t, codeLines->lineCounts, codeLines->capacity);
    initCodeLines(codeLines);
}

void writeCodeLines(CodeLines *codeLines, int line) {
    if (line > codeLines->capacity) {
        int oldCapacity = codeLines->capacity;
        codeLines->capacity = GROW_CAPACITY(oldCapacity);
        codeLines->lineCounts = GROW_ARRAY(codeLines->lineCounts, uint8_t, oldCapacity, codeLines->capacity);
    }
    codeLines->lineCounts[line]++;
}

int readCodeLine(CodeLines *codeLines, int chunkNumber) {
    if (codeLines->capacity <= 0) return 0;

    int lineIdx = 0;
    int chunkCount = 0;
    for(;;) {
        if (lineIdx >= codeLines->capacity) return 1;
        chunkCount += codeLines->lineCounts[lineIdx];
        if (chunkCount >= chunkNumber) return lineIdx == 0 ? 1 : lineIdx;
        lineIdx++;
    }
}