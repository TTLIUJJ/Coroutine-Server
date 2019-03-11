//
// Created by ackerman on 2019-02-04.
//

#ifndef COROUTINEIOSERVER_TOOL_H
#define COROUTINEIOSERVER_TOOL_H

#include <jni.h>


JNIEnv* jni_init(JavaVM *jvm, char *class_path);
jclass load_class(JNIEnv *env, char *class_name);

int get_client_fd(char *buf);
char *get_message_buf(char *buf);
char* number_to_string(int client_fd);
char *set_message_buf(char *buf, int client_fd);
int request_message_done(char *buf);

#endif //COROUTINEIOSERVER_TOOL_H
