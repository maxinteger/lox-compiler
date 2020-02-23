#ifndef clox_chunk_h
#define clox_chunk_h

#include "common.h"
#include "value.h"

typedef enum {
    OP_CONSTANT,
    OP_ADD,
    OP_SUBTRACT,
    OP_MULTIPLY,
    OP_DIVIDE,
    OP_NEGATE,
    OP_RETURN,
} OpCode;


typedef struct {
    int capacity;
    uint8_t *lineCounts;
} CodeLines;

typedef struct {
    int count;
    int capacity;
    uint8_t *code;
    CodeLines lines;
    ValueArray constants;
} Chunk;


void initChunk(Chunk *chunk);

void freeChunk(Chunk *chunk);

void writeChunk(Chunk *chunk, uint8_t byte, int line);


int addConstant(Chunk *chunk, Value value);


void initCodeLines(CodeLines *codeLines);

void freeCodeLines(CodeLines *codeLines);

void writeCodeLines(CodeLines *codeLines, int line);

int readCodeLine(CodeLines *codeLines, int chunkNumber);


#endif

