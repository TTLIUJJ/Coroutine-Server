# 在Linux上使用JNI代码


- JNI
- 链接


参考文章：

- [GCC编译过程与动态链接库和静态链接库](https://www.cnblogs.com/king-lps/p/7757919.html)


## 使用C语言调用Java代码

由于协程Demo的需要，笔者需要使用C语言调用之前写过的Java代码库，从网上搜索到的资料，大多数是关于如何以Java代码为主程序，调用本地C语言接口函数，不符合笔者的需求，所以写下该博客，记录以C语言为运行主程序代码，调用Java相关的代码。

下面代码展示如何在C语言中调用Java代码。

**C代码**

```c
#include <jni.h>
#include <stdio.h>

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


int main() {
	JavaVM *jvm;
    JNIEnv *env;
    jclass clazz;
    jmethodID method_id;

    env   = jni_init(jvm, "-Djava.class.path=/root/demo/classes");
    clazz  = load_class(env, "edu/xmu/networkingModel/coroutineIOComponent/CoroutineIOServer");
    if (clazz == 0) {
        printf("error: call method failed.\n");
        return NULL;
    }
    method_id = (*env)->GetStaticMethodID(env, clazz, "testMethod", "()I");
    if (method_id == 0) {
        printf("error: call method failed.\n");
    }
    
    jint res = (*env)->CallStaticIntMethod(env, clazz, method_id);
    printf("get from java: %d\n", res);
}
```

**Java代码**

```java
package edu.xmu.networkingModel.coroutineIOComponent;

public class CoroutineIOServer {
   public static int testMethod() {
        return 111;
    }
}
```


## 在Linux上生成可执行文件


关于JNI的一些详细语言，笔者这里就不再赘述。主要记录在Linux上使用C语言，调用Java项目工程相关的代码的解决方案。


首先查看笔者的项目目录：

```
demo
├── coroutine.c
├── coroutine.h
├── demo4.c
├── edu
│   └── xmu
│       ├── baseConponent
│       │   ├── http
│       │   │   ├── HttpContext.java
│       │   │   ├── HttpRequest.java
│       │   │   ├── HttpResponse.java
│       │   │   ├── Request.java
│       │   │   └── Response.java
│       │   ├── MethodType.java
│       │   ├── RequestMessage.java
│       │   ├── RequestParseUtil.java
│       │   ├── RequestState.java
│       │   ├── ResponseParseUtil.java
│       │   └── ResponseState.java
│       ├── HttpServerConsole.java
│       └── networkingModel
│           ├── AbstractWorkRunnable.java
│           ├── asynchronousIOComponent
│           │   ├── AcceptCompletionHandler.java
│           │   ├── AsynchronousIOServer.java
│           │   ├── ReadCompletionHandler.java
│           │   └── WriteCompletionHandler.java
│           ├── blockingIOComponent
│           │   ├── BlockingIOServer.java
│           │   └── BlockingWorkRunnable.java
│           ├── coroutineIOComponent
│           │   └── CoroutineIOServer.java
│           ├── multiplexingIOComponent
│           │   ├── MultiplexingIOServer.java
│           │   ├── MultiplexingReadRunnable.java
│           │   └── MultiplexingWriteRunnable.java
│           └── nonblockingIOComponent
│               ├── NonBlockingIOServer.java
│               └── NonBlockingWorkRunnable.java
├── javaClassList.txt
├── main.c
├── server.c
├── server.h
├── start
├── tool.c
└── tool.h
```




### 静态库和动态库

静态库的两个缺点：

- 浪费大量的空间
- 静态库一旦被修改，所有连接该静态库的程序都要重新编译。

动态库在程序编译时并不会被链接到目标代码中，而是在程序运行时才被载入。不同的程序如果调用相同的库，那么在内存中只需要一份该共享库的实例。由于动态库在程序运行时才被载入，解决了静态库对调用程序更新带来的麻烦，用户只需更新被修改的库文件代码。







### gcc链接库文件的使用

从程序员的角度看，函数库实际上就是头文件(.h)和库文件(.so或者.a)的集合。虽然Linux下的大多数函数都默认将头文件放到/usr/inculde/目下以及将库文件放在/usr/lib/目录下，但在需要第三方库函数库的时候，gcc在编译时必须要有自己的办法来查找所需的头文件和库文件。


gcc采用搜索目录的办法来查找所需的文件：

- -I选项可以向gcc的头文件搜索路径中添加新的目录
- -L选项可以向gcc的库文件搜索路径中添加新的目录



```=-va" > javaClassList.txt

2. javac -d /root/demo/classes @javaClassList.txt

3. gcc -o start coroutine.c tool.c server.c main.c -I/root/downloads/jdk1.8.0_201/include -I/root/downloads/jdk1.8.0_201/include/linux -L/root/downloads/jdk1.8.0_201/jre/lib/amd64/server -ljvm
```




熟悉makefile语言的同学，可以将编译链接过程写入makefile文件中。


```
 y1=0.3431*x1+0.3384*x2+0.3552*x3+0.3692*x4+0.3752*x5+0.3587*x6+0.3427*x7+0.3441*x8

 y2=0.5035*x1-0.4866*x2+0.1968*x3+0.1088*x4-0.0547*x5-0.2208*x6-0.4783*x7+0.4225*x8
```