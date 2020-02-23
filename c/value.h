//
// Created by vadasz on 2019.03.14..
//

#ifndef C_VALUE_H
#define C_VALUE_H

typedef double Value;

typedef struct {
    int capacity;
    int count;
    Value* values;
} ValueArray;

void initValueArray(ValueArray* array);
void writeValueArray(ValueArray* array, Value value);
void freeValueArray(ValueArray* array);

void printValue(Value value);

#endif //C_VALUE_H
