#include "coroutine.h"
#include "tool.h"
#include "server.h"
#include <stdio.h>
#include <stdlib.h>
#include <assert.h>
#include <stddef.h>
#include <string.h>
#include <stdint.h>
#include <jni.h>
#include "tool.h"

#if __APPLE__ && __MACH__
#include <sys/ucontext.h>
#else
#include <ucontext.h>
#endif

#define STACK_SIZE (1024*1024*4)
#define DEFAULT_COROUTINE 16

struct coroutine;

struct scheduler {
    char stack[STACK_SIZE];
    ucontext_t main_context;
    int size;
    int cap;
    int running_id;
    struct coroutine **coroutines;
    int client_id_map[65536];

    char **buf_map;
};

struct coroutine {
    coroutine_function function;
    void *param;
    ucontext_t context;
    struct scheduler * scheduler;
    ptrdiff_t cap;
    ptrdiff_t size;
    int status;
    char *stack;

    char *buf;
    int client_fd;
};

struct coroutine *_coroutine_create(struct scheduler *scheduler, coroutine_function f, void *param) {
    struct coroutine * coroutine = malloc(sizeof(*coroutine));
    coroutine->function = f;
    coroutine->param = param;
    coroutine->scheduler = scheduler;
    coroutine->cap = 0;
    coroutine->size = 0;
    coroutine->status = COROUTINE_READY;
    coroutine->stack = NULL;
    coroutine->buf = (char *)malloc(sizeof(char) * 1024);
    coroutine->buf[0] = 's';
    coroutine->buf[1] = 'h';
    coroutine->buf[2] = 'i';
    coroutine->buf[3] = 't';
    coroutine->buf[4] = '\0';



    return coroutine;
}

void _coroutine_free(struct coroutine *coroutine) {
    free(coroutine->stack);
    free(coroutine);
}

struct scheduler *
scheduler_open(void) {
    struct scheduler *S = malloc(sizeof(*S));
    S->size = 0;
    S->cap = DEFAULT_COROUTINE;
    S->running_id = -1;
    S->coroutines = (struct coroutine **)malloc(sizeof(struct coroutine *) * S->cap);
    memset(S->coroutines, 0, sizeof(struct coroutine *) * S->cap);

    S->buf_map = (char **)malloc(sizeof(char *) * 65536);
    memset(S->buf_map, 0, sizeof(char *) * 65536);

    return S;
}

void
scheduler_close(struct scheduler *scheduler) {
    for (int i = 0; i < scheduler->cap; ++i) {
        struct coroutine *coroutine = scheduler->coroutines[i];
        if (coroutine) {
            _coroutine_free(coroutine);
        }
    }
    free(scheduler->coroutines);
    scheduler->coroutines = NULL;
    free(scheduler);
}

int coroutine_new(struct scheduler *scheduler, coroutine_function f, void *param) {
    struct coroutine *co = _coroutine_create(scheduler, f, param);
    if (scheduler->size >= scheduler->cap) {
        int id = scheduler->cap;
        scheduler->coroutines = (struct coroutine **)realloc(scheduler->coroutines, scheduler->cap * 2 * sizeof(struct coroutine *));
        memset(scheduler->coroutines + scheduler->cap , 0 , sizeof(struct coroutine *) * scheduler->cap);
        scheduler->coroutines[scheduler->cap] = co;
        scheduler->cap *= 2;
        ++scheduler->size;
        return id;
    } else {
        for (int i = 0; i < scheduler->cap; ++i) {
            int id = (i + scheduler->size) % scheduler->cap;
            if (scheduler->coroutines[id] == NULL) {
                scheduler->coroutines[id] = co;
                ++scheduler->size;
                return id;
            }
        }
    }
    assert(0);
    return -1;
}

//static struct jni_data *jni_data;

static
void main_function(uint32_t low_ptr, uint32_t high_ptr) {
    uintptr_t ptr = (uintptr_t)low_ptr | ((uintptr_t)high_ptr << 32);
    struct scheduler *scheduler = (struct scheduler *)ptr;
    int id = scheduler->running_id;
    struct coroutine *C = scheduler->coroutines[id];

    C->function(scheduler, C->param);

    _coroutine_free(C);
    scheduler->coroutines[id] = NULL;
    --scheduler->size;
    scheduler->running_id = -1;
}

void coroutine_resume(struct scheduler * scheduler, int id) {
    assert(scheduler->running_id == -1);
    assert(id >=0 && id < scheduler->cap);
    struct coroutine *coroutine = scheduler->coroutines[id];
    if (coroutine == NULL)
        return;

    int status = coroutine->status;
    switch(status) {
        case COROUTINE_READY:
            getcontext(&coroutine->context);
            coroutine->context.uc_stack.ss_sp = scheduler->stack;
            coroutine->context.uc_stack.ss_size = STACK_SIZE;
            coroutine->context.uc_link = &scheduler->main_context;
            scheduler->running_id = id;
            coroutine->status = COROUTINE_RUNNING;
            uintptr_t ptr = (uintptr_t)scheduler;
            makecontext(&coroutine->context, (void (*)(void)) main_function, 2, (uint32_t) ptr, (uint32_t) (ptr >> 32));
            swapcontext(&scheduler->main_context, &coroutine->context);
            break;

        case COROUTINE_SUSPEND:
            memcpy(scheduler->stack + STACK_SIZE - coroutine->size, coroutine->stack, coroutine->size);
            scheduler->running_id = id;
            coroutine->status = COROUTINE_RUNNING;
            swapcontext(&scheduler->main_context, &coroutine->context);
            break;

        default:
            assert(0);
    }
}

static
void _save_stack(struct coroutine *coroutine, char *top) {
    char dummy = 0;
    assert(top - &dummy <= STACK_SIZE);
    if (coroutine->cap < top - &dummy) {
        free(coroutine->stack);
        coroutine->cap = top-&dummy;
        coroutine->stack = (char *)malloc(coroutine->cap);
    }
    coroutine->size = top - &dummy;
    memcpy(coroutine->stack, &dummy, coroutine->size);
}

void coroutine_yield(struct scheduler * scheduler) {
    int id = scheduler->running_id;
    assert(id >= 0);
    struct coroutine * coroutine = scheduler->coroutines[id];
    assert((char *)&coroutine > scheduler->stack);
    _save_stack(coroutine,scheduler->stack + STACK_SIZE);
    coroutine->status = COROUTINE_SUSPEND;
    scheduler->running_id = -1;
    swapcontext(&coroutine->context, &scheduler->main_context);
}

int coroutine_status(struct scheduler * scheduler, int id) {
    assert(id >= 0 && id < scheduler->cap);
    if (scheduler->coroutines[id] == NULL) {
        return COROUTINE_DEAD;
    }
    return scheduler->coroutines[id]->status;
}

int coroutine_running_id(struct scheduler *scheduler) {
    return scheduler->running_id;
}

void coroutine_resume_all(struct scheduler *scheduler) {
    for (int i = 0; i < scheduler->cap; ++i) {
        struct coroutine *coroutine = scheduler->coroutines[i];
        if (coroutine == NULL) {
            continue;
        }
        coroutine_resume(scheduler, i);
    }
}

char* get_coroutine_buf(struct scheduler *scheduler, int client_fd) {
    int id = scheduler->client_id_map[client_fd];

    assert(id >= 0 && id < scheduler->cap);
    struct coroutine *coroutine = scheduler->coroutines[id];

    return coroutine->buf;
}

void set_client_fd_out(struct scheduler *scheduler, int client_fd, int flag) {
    scheduler->client_id_map[client_fd] = flag;
}

int get_client_fd_flag(struct scheduler *scheduler, int client_fd) {
    return scheduler->client_id_map[client_fd];
}

void after_coroutine_new(struct scheduler *scheduler, int coroutine_id, int client_fd) {

    assert(coroutine_id >= 0 && coroutine_id < scheduler->cap);
    struct coroutine *coroutine = scheduler->coroutines[coroutine_id];
    coroutine->client_fd = client_fd;


    scheduler->buf_map[client_fd] = (char *) malloc(sizeof(char) * 1024);
    memset(scheduler->buf_map[client_fd], 0, 1024);
}


struct coroutine* get_coroutine(struct scheduler *scheduler, int client_fd) {
    int coroutine_id = scheduler->client_id_map[client_fd];

    return scheduler->coroutines[coroutine_id];
}


char* set_shit(struct scheduler *scheduler, int client_fd, char *p) {
    scheduler->buf_map[client_fd] = (char *) malloc(sizeof(char) * 1024);
    memset(scheduler->buf_map[client_fd], 0, 1024);

    char *q = scheduler->buf_map[client_fd];

    for (int i = 0; i < strlen(p); ++i) {
        q[i] = p[i];
    }

    return q;
}

char *get_shit(struct scheduler *scheduler, int client_fd) {
    return scheduler->buf_map[client_fd];
}