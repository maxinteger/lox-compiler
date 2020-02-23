cmake_minimum_required(VERSION 3.13)
project(c C)

set(CMAKE_C_STANDARD 99)

add_executable(c main.c chunk.c chunk.h memory.h memory.c debug.h debug.c value.h value.c vm.h vm.c compiler.c compiler.h scanner.h scanner.c)