cmake_minimum_required(VERSION 3.13)
project(CoroutineIOServer C)

set(CMAKE_C_STANDARD 11)

add_executable(CoroutineIOServer main.c coroutine.c coroutine.h server.c server.h demo4.c tool.h tool.c)