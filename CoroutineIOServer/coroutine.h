#ifndef C_COROUTINE_H
#define C_COROUTINE_H

#define COROUTINE_DEAD 0
#define COROUTINE_READY 1
#define COROUTINE_RUNNING 2
#define COROUTINE_SUSPEND 3

struct scheduler;

typedef void (*coroutine_function)(struct scheduler *, void *param);

struct scheduler * scheduler_open(void);
void scheduler_close(struct scheduler *);
int  coroutine_new(struct scheduler *, coroutine_function, void *param);

void coroutine_resume(struct scheduler *, int id);
void coroutine_yield(struct scheduler *);
void coroutine_resume_all(struct scheduler *scheduler);

int coroutine_status(struct scheduler *, int id);
int coroutine_running_id(struct scheduler *);

char* get_coroutine_buf(struct scheduler *scheduler,  int client_fd);
void set_client_fd_out(struct scheduler *scheduler, int client_fd, int flag);
int get_client_fd_flag(struct scheduler *scheduler, int client_fd);
void after_coroutine_new(struct scheduler *scheduler, int coroutine_id, int client_fd);
struct coroutine* get_coroutine(struct scheduler *scheduler, int client_fd);

char* set_shit(struct scheduler *scheduler, int client_fd, char *p);
char *get_shit(struct scheduler *scheduler, int client_fd);


#endif
