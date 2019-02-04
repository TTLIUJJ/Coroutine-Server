package edu.xmu;

import edu.xmu.networkingModel.asynchronousIOComponent.AsynchronousIOServer;
import edu.xmu.networkingModel.blockingIOComponent.BlockingIOServer;
import edu.xmu.networkingModel.coroutineIOComponent.CoroutineIOServer;
import edu.xmu.networkingModel.multiplexingIOComponent.MultiplexingIOServer;
import edu.xmu.networkingModel.nonblockingIOComponent.NonBlockingIOServer;

import java.util.Scanner;

/**
 * @Program: soldier
 * @Description: HTTP服务器的启动接口
 *               如果可能会使用多种网络模型来测试
 *               其中包括: 同步阻塞模型、同步非阻塞模型、同步非阻塞模型、异步非阻塞模型、协程模型
 *               并且已有的代理进行对比
 *               其中包括 tomcat、nginx、goroutine
 * @Author: Ackerman
 * @Create: 2019-01-10 17:02
 */
public class HttpServerConsole {
    private static void initIOServer(int model) {
        System.out.println("Server is initializing at port " + model + "...");
        switch (model) {
            case 8001:
                BlockingIOServer blockingIOServer = new BlockingIOServer();
                blockingIOServer.start();
                break;

            case 8002:
                NonBlockingIOServer nonBlockingIOServer = new NonBlockingIOServer();
                nonBlockingIOServer.start();
                break;

            case 8003:
                MultiplexingIOServer multiplexingIOServer = new MultiplexingIOServer();
                multiplexingIOServer.start();
                break;

            case 8004:
                AsynchronousIOServer asynchronousIOServer = new AsynchronousIOServer();
                asynchronousIOServer.start();
                break;

            case 8005:
                CoroutineIOServer coroutineIOServer = new CoroutineIOServer();
                coroutineIOServer.start();
                break;

            default:
                System.out.println("input error");
        }
    }

    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);
        int model = in.nextInt();

        in.close();
        initIOServer(model);
    }
}