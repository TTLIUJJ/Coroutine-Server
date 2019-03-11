# 基于C语言的协程和Java混合编程的服务器


- 协程
- JNI
- 进程间通信



写在前面的话：

笔者在学习《UDP》中的网络模型之后，已经尝试使用Java语言写过阻塞IO模型、非阻塞IO模型、IO多路复用模型以及异步IO模型。每种网络模型的特点这里就不再赘述，本文主要利用[云风的协程库](https://github.com/cloudwu/coroutine/)和JNI技术调用笔者之前写过的解析HTTP请求的Java代码（[笔者的HTTP服务器Demo](https://github.com/TTLIUJJ/HTTP-Server)），从而实现一个新的HTTP服务器。这个基于协程的服务器只是笔者学习新知识的一个小demo，简单地使用webbench测试的QPS结果并不理想，由于已经开学写毕业论文了，所以接下来没时间来改善该demo的性能，故在此写下该博客进行总结。



## 协程

谷歌开发的GoLang语言由于Goroutine协程在web服务器上的强大性能而渐渐被人熟知，对于每一个web请求，协程服务器都会使用一个go协程去处理，从而让一个服务器可以轻松同时开启上万个协程。

为了更好的理解协程的概念，先来看以下关于协程的例子，类似于多线程操作，在两个协程之间切换进行任务：

```c
void foo(struct scheduler *s, int start) {
    for (int i = 0; i < 3; i++) {
        printf("coroutine %d: %d\n", coroutine_running_id(s) , start + i);
        coroutine_yield(s);	//挂起协程
    }
}

void test() {
    struct scheduler * scheduler = scheduler_open();
    int id1 = coroutine_new(scheduler, foo, 0);
    int id2 = coroutine_new(scheduler, foo, 10);
    // while判断协程是否存活
    while (coroutine_status(scheduler, id1) && coroutine_status(scheduler, id2)) {
        coroutine_resume(scheduler, id1);	//	恢复协程
        coroutine_resume(scheduler, id2);
    }
}

输出：
coroutine 0: 0	
coroutine 1: 10
coroutine 0: 1
coroutine 1: 11
coroutine 0: 2
coroutine 1: 12
```

协程是一种用户态的轻量级线程，通过上面的例子，知道协程把正在运行的任务暂时挂起，经过后续的操作恢复该协程继续进行任务（可把协程当成能够保存局部变量的可重入函数）。

**协程的优点**

1. 线程的切换需要系统内核的操作，花费时间较多，而协程的切换只在用户态空间内进行操作，花费的时间是低于线程切换的。

2. 协程可以简单地使用全局变量进行协程间通信，对比于较为麻烦的线程同步技术。

3. 协程的切换为运行间的协程主动让出CPU，而非是抢占式的占用。

## JNI

笔者使用了C语言和Java代码混合编程，需要使用JNI技术，如非项目真的需要，并不建议使用JNI技术。笔者以C语言为主程序调用Java代码，例子如下：

```java
/** C代码 */
void jni_test() {
    JavaVM *jvm;
    JNIEnv *env;
    jclass clazz;
    jmethodID method_id;

    env   = jni_init(jvm, "-Djava.class.path=/root/demo/classes");
    clazz  = load_class(env, "edu/xmu/TestJavaClass");
    if (clazz == 0) {
        printf("error: call method failed.\n");
        return NULL;
    }
    method_id = (*env)->GetStaticMethodID(env, clazz, "testMethod", "()I");
    if (method_id == 0) {
        printf("error: call method  .\n");
        return NULL;
    }
	
    jint i = ((*env)->CallStaticIntMethod(env, clazz, method_id);
    printf("get result of Java: %d.\n", (int)i);
	
    return jni_data;
}

// Java代码
package com.edu.TestJavaClass

public class TestJavaClass {
	public static int testMethod() {
		return 111;
	}
}
```

上面的例子只是展示一下使用C语言和Java如何进行混合编程，具体的调用过程笔者将会在后续的文章中贴出教程，有兴趣了解的可以关注一下。


## 进程间通信

笔者尝试在子协程和多线程使用JNI调用Java代码，二者方法均无法顺利进行。比如如下代码

```c
// 全局变量
JavaVM *jvm;
JNIEnv *env;
jclass clazz;
jmethodID method_id;

// 主协程初始化JVM
void jni_data_init() {
    JavaVM *jvm;
    JNIEnv *env;
    jclass clazz;
    jmethodID method_id;

    env   = jni_init(jvm, "-Djava.class.path=/root/demo/classes");
    clazz  = load_class(env, "edu/xmu/networkingModel/coroutineIOComponent/CoroutineIOServer");
    if (clazz == 0) {
        printf("error: call method failed.\n");
        return;
    }
    method_id = (*env)->GetStaticMethodID(env, clazz, "test", "()I");
    if (method_id == 0) {
        printf("error: call method  .\n");
        return;
    }
}

// 子协程调用
void foo() {
	// ...
    JavaVM *jvm;
    JNIEnv *env;
    jclass clazz;
    jmethodID method_id;

    jint   y  = (*env)->CallStaticIntMethod(env, clazz, method_id);
    printf("lallalal: %d\n", (int)y);
	
	//...
}
```

由于主协程和子协程进行切换之后，CPU运行子协程的任务，子协程调用JVM相关的代码导致JVM运行出错。所以笔者只好使用进程间通信，使用子进程进行非阻塞的数据读取HTTP请求之后，将数据传给父进程处理得到HTTP响应之后，子进程再次从父进程中读取数据，最后返回给客户端；使用父进程读取子进程传来的请求数据，调用Java处理HTTP请求和响应的代码之后，将响应返回给子进程。

这里是为了解决代码的缺陷而使用进程间的通信，数据在进程间的一次来回传输，以及进程间数据的同步处理都会消耗CPU的时间以及IO的负担，最终造成本Demo能够提供的并发量相当有限。

```c
if ((pid = fork()) < 0) {
        printf("fork() error.\n");
        return;
    }
    else if (pid == 0) {
		// 父进程
        char req_buf[REQUEST_BUF];
        ssize_t n_read;
        // 先从子进程中读取数据
        while ((n_read = read(pipe_fd[0], req_buf, REQUEST_BUF)) != -1) {
   			// 调用Java代码处理请求
            client_fd = get_client_fd(req_buf);
            char *r_buf = get_message_buf(req_buf);
            jstring request = (*env)->NewStringUTF(env, r_buf);
            jstring j_string = (jstring)((*env)->CallStaticObjectMethod(env, clazz, method_id, client_fd, request));
            char *response = (char *)((*env)->GetStringUTFChars(env, j_string, NULL));
			// 将请求得到响应写回子进程
            char *w_buf = set_message_buf(response, client_fd);
            write(pipe_fd[1], w_buf, strlen(w_buf));
        }
        printf("(parent process) read error.");
        close(pipe_fd[0]);
        close(pipe_fd[1]);
        return ;
    }
    else {
        while (1) {
        	//子进程的处理与父进程的处理是镜像的，具体代码就不贴了...
        }
    }
```

后续更新的博客：

[学习云风协程库]()

[消灭本Demo中处理协程出现的Bug]()

[在Linux上使用JNI代码]()
