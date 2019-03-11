//
// Created by ackerman on 2019-01-30.
//

#ifndef COROUTINEIOSERVER_SERVER_H
#define COROUTINEIOSERVER_SERVER_H
#include "coroutine.h"

int socket_open();
void socket_handler(int server_fd, struct scheduler *scheduler, coroutine_function f);
void process_event(struct scheduler *scheduler, void *param);

#endif //COROUTINEIOSERVER_SERVER_H
