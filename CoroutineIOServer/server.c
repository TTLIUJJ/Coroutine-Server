//
// Created by ackerman on 2019-01-30.
//

#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include <errno.h>
#include <fcntl.h>
#include <unistd.h>
#include <string.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <sys/ioctl.h>
#include <netinet/in.h>
#include <pthread.h>
#include <ctype.h>
#include <sys/epoll.h>
#include "coroutine.h"
#include "tool.h"
#include "server.h"

#define MAX_BUF 1024
#define REQUEST_BUF  2048
#define RESPONSE_BUF 2048
#define EPOLL_SIZE 1024
#define EPOLL_EVENTS 100
#define PORT    8005

int socket_open() {

    int server_fd;
    struct sockaddr_in server_addr;

    if ((server_fd = socket(AF_INET, SOCK_STREAM, 0)) == -1) {
        printf("create server socket: %s(errno: %d)\n", strerror(errno), errno);
        return -1;
    }

    int flags = fcntl(server_fd, F_GETFL, 0);
    fcntl(server_fd, F_SETFL, flags | O_NONBLOCK);

    memset(&server_addr, 0, sizeof(server_addr));
    server_addr.sin_family = AF_INET;
    server_addr.sin_addr.s_addr = htonl(INADDR_ANY);
    server_addr.sin_port = htons(PORT);

    if ((bind(server_fd, (struct sockaddr *)&server_addr, sizeof(server_addr))) == -1) {
        printf("bind server socket: %s(errno: %d)\n", strerror(errno), errno);
        exit(0);
    }
    if (listen(server_fd, 1024) == -1) {
        printf("listen server socket: %s(errno: %d)\n", strerror(errno), errno);
        exit(0);
    }

    return server_fd;
}

JavaVM *jvm;
JNIEnv *env;
jclass clazz;
jmethodID method_id;

void socket_handler(int server_fd, struct scheduler *scheduler, coroutine_function f) {

    JavaVMInitArgs vm_args;
    JavaVMOption options[1];
    options[0].optionString = "-Djava.class.path=/root/demo/classes";
    memset(&vm_args, 0, sizeof(vm_args));
    vm_args.version = JNI_VERSION_1_8;
    vm_args.nOptions = 1;
    vm_args.options = options;
    if ((JNI_CreateJavaVM(&jvm, (void**)&env, &vm_args)) == JNI_ERR)  {
        return;
    }

    clazz = (*(env))->FindClass(env, "edu/xmu/networkingModel/coroutineIOComponent/CoroutineIOServer");
//    method_id = (*(env))->GetStaticMethodID(env, clazz, "test", "()I");
    method_id = (*env)->GetStaticMethodID(env, clazz, "parseRequest", "(ILjava/lang/String;)Ljava/lang/String;");

//    printf("\njvm: %p\nenv: %p\nclazz: %p\nmethod_id: %p\n", jvm, env, &clazz, &method_id);


    int epoll_fd;
    int event_cnt;
    int client_fd;
    socklen_t client_addr_len;
    struct epoll_event event;
    struct sockaddr_in client_addr;
    struct epoll_event events[EPOLL_EVENTS];

    // epoll_create(size) size的大小并不影响监听的大小数目, 只要空间足够即可
    if ((epoll_fd = epoll_create(EPOLL_SIZE)) == -1) {
        printf("epoll_create fd: %s(errno: %d)\n", strerror(errno), errno);
        exit(0);
    }

    event.events = EPOLLIN;
    event.data.fd = server_fd;
    epoll_ctl(epoll_fd, EPOLL_CTL_ADD, server_fd, &event);

    event_cnt = 0;
    int fd;
    while (1) {
        event_cnt = epoll_wait(epoll_fd, events, EPOLL_EVENTS, -1);
        for (int i = 0; i < event_cnt; ++i) {
            fd = events[i].data.fd;

            if ((server_fd == fd) && (events[i].events & EPOLLIN)) {
                client_addr_len = sizeof(client_addr);
                client_fd = accept(server_fd, (struct sockaddr *)& client_addr, &client_addr_len);

                int flags = fcntl(client_fd, F_GETFL, 0);
                fcntl(client_fd, F_SETFL, flags | O_NONBLOCK);

                event.events = EPOLLIN | EPOLLET;
                event.data.fd = client_fd;
                epoll_ctl(epoll_fd, EPOLL_CTL_ADD, client_fd, &event);

                int fds[] = { server_fd, client_fd, epoll_fd };
                int coroutine_id = coroutine_new(scheduler, f, (void *)fds);
                after_coroutine_new(scheduler, coroutine_id, client_fd);
            }
            else if (events[i].events & EPOLLIN){
                coroutine_resume_all(scheduler);

//                printf("epollin");
//                printf("\n【%s】\n", get_shit(scheduler, client_fd));

                jstring request = (*env)->NewStringUTF(env, get_shit(scheduler, client_fd));
                jstring j_string = (jstring)((*env)->CallStaticObjectMethod(env, clazz, method_id, client_fd, request));
                char *response = (char *)((*env)->GetStringUTFChars(env, j_string, NULL));
//                printf("\n%s\n", response);
                write(fd, response, strlen(response));
                // TODO 释放内存 s
//                jint   y  = (*(env))->CallStaticIntMethod(env, clazz, method_id);
//                printf("\nlallalal: %d\n", (int)y);
            }
        }
    }
}

void process_event(struct scheduler *scheduler, void *param) {
//    printf("\n~~~~~\n");

    ssize_t nread;
    int *fds = (int *) param;
    int client_fd = fds[1];
    int epoll_fd = fds[2];


    ssize_t max_buf = 10;
    char buf[max_buf];

    while (1) {
        // nread: =  0 客户端关闭
        //        = -1 没有读到数据
        //        >  0
        memset(buf, 0,  max_buf);
        nread = recv(client_fd, buf, max_buf, MSG_DONTWAIT);
        if (nread > 0) {
            char *p = get_shit(scheduler, client_fd);
            memcpy(p + strlen(p), buf, strlen(buf));

            if (request_message_done(p) == 0) {
                // request_message_done 表示请求已经结束
                struct epoll_event event;
                event.events = EPOLLIN | EPOLLET;
                event.data.fd = client_fd;
                epoll_ctl(epoll_fd, EPOLL_CTL_ADD, client_fd, &event);
                break;
            }
            else {
                // 请求未结束 继续读取数据
                continue;
            }

        }
        else if (nread < 0) {
            coroutine_yield(scheduler); // 内核数据未准备好, 挂起协程
        }
        else {
//            printf("close by client");
            break;
        }
    }
}

