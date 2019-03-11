//
// Created by ackerman on 2019-02-04.
//

#include <stddef.h>
#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <jni.h>
#include "tool.h"

JNIEnv* jni_init(JavaVM *jvm, char *class_path) {
    JNIEnv *env;
    JavaVMInitArgs vm_args;
    JavaVMOption options[1];

    options[0].optionString = class_path;
    memset(&vm_args, 0, sizeof(vm_args));
    vm_args.version = JNI_VERSION_1_8;
    vm_args.nOptions = 1;
    vm_args.options = options;

    if ((JNI_CreateJavaVM(&jvm, (void**)&env, &vm_args)) == JNI_ERR)  {
        return NULL;
    }


    return env;
}

jclass load_class(JNIEnv *env, char *class_name) {
    jclass clazz = (*env)->FindClass(env, class_name);
    if (clazz == 0) {
        printf("error: load class %s failed.\n", class_name);
    }

    return clazz;
}

int get_client_fd(char *buf) {
    int cur = 0;
    int fd  = 0;
    for (char *p = buf; *p != '$'; ++p) {
        fd *= 10;
        cur = *p - '0';
        fd += cur;
    }

    return fd;
}

char *get_message_buf(char *buf) {
    char *p = buf;
    while (*p != '$') {
        ++p;
    }
    ++p;

    size_t len = strlen(p);
    char *q = (char *)malloc(sizeof(char) * len + 1);
    q = memcpy(q, p, len);
    q[len] = 0;

    return q;
}

char* number_to_string(int client_fd) {
    char *p = (char *) malloc(sizeof(char) * 33);
    memset(p, 0, 33);

    if (client_fd == 0) {
        p[0] = '0';
        p[1] = '$';
        return p;
    }

    int cur = 0;
    int index = 0;
    while (client_fd != 0) {
        cur = client_fd % 10;
        p[index++] = (char)('0' + cur);
        client_fd /= 10;
    }
    p[index] = '$';


    int beg = 0;
    int end = index-1;
    char temp;
    while (beg < end) {
        temp = p[beg];
        p[beg] = p[end];
        p[end] = temp;
        ++beg;
        --end;
    }

    return p;
}

char *set_message_buf(char *buf, int client_fd) {
    char *fd_$ = number_to_string(client_fd);
    size_t len = strlen(buf) + strlen(fd_$) + 1;
    char *p = (char *)malloc(sizeof(char) * len);
    memset(p, 0, len);
    strcat(p, fd_$);
    strcat(p, buf);
    p[len] = '\0';

    return p;
}

int request_message_done(char *buf) {
    int len = (int)strlen(buf);

    for (int i = 3; i <= len; ++i) {
        if (buf[i-3] == '\r' && buf[i-2] == '\n' && buf[i-1] == '\r' && buf[i] == '\n') {
            return 0;
        }
    }

    return -1;
}