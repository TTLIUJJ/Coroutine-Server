#include <unistd.h>
#include <stdio.h>
#include "coroutine.h"
#include "server.h"


int main() {
    struct scheduler *scheduler = scheduler_open();

    int server_fd = socket_open();
    socket_handler(server_fd, scheduler, process_event);

    return 0;
}