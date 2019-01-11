package edu.xmu;

import edu.xmu.networkingModel.eventDrivenConponent.EventDrivenServer;

/**
 * @Program: soldier
 * @Description: HTTP服务器的启动接口
 *               如果可能会使用多种网络模型来测试
 *               其中包括: 阻塞模型、非阻塞模型、事件驱动模型、异步模型、协程模型
 *               并且已有的代理进行对比
 *               其中包括 tomcat、nginx、goroutine
 * @Author: Ackerman
 * @Create: 2019-01-10 17:02
 */
public class HttpServerConsole {
    public static void main(String args[]) {
        System.out.println(System.getProperty("user.dir"));
        EventDrivenServer eventDrivenServer = new EventDrivenServer();
        eventDrivenServer.start();
    }
}