#include <jni.h>
#include <stddef.h>
#include <stdio.h>
#include "tool.h"
#include "coroutine.h"

struct args {
    int n;
};

static
void foo(struct scheduler * S, void *ud) {
    struct args * arg = ud;
    int start = arg->n;
    int i;
    for (i=0;i<5;i++) {
        printf("coroutine %d : %d\n", coroutine_running_id(S) , start + i);
        coroutine_yield(S);
    }
}

int main() {

    JavaVM *jvm;
    JNIEnv* env = jni_init(jvm, "-Djava.class.path=/root/demo4");

    if (env == NULL) {
        printf("error: load JNIEnv failed.\n");
        return -1;
    }
    else {
        jclass clazz = load_class(env, "Demo4");
        if (clazz == 0) {
            printf("error: load class failed.\n");
            return -1;
        }
        jmethodID method_id = (*env)->GetStaticMethodID(env, clazz, "intMethod", "(I)I");
        if (method_id == 0) {
            return -1;
        }
        int square = (*env)->CallStaticIntMethod(env, clazz, method_id, 10);
        printf("Result of intMethod: %d\n", square);

        /** test for coroutine */

        struct scheduler * scheduler = scheduler_open();

        struct args arg1 = { 0 };
        struct args arg2 = { 100 };

        int id1 = coroutine_new(scheduler, foo, &arg1);
        int id2 = coroutine_new(scheduler, foo, &arg2);
        printf("test2 start\n");
        while (coroutine_status(scheduler, id1) && coroutine_status(scheduler, id2)) {
            coroutine_resume_all(scheduler);
        }
        printf("test2 end\n");

        scheduler_close(scheduler);
    }


    return 0;
}