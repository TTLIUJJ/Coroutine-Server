package edu.xmu.networkingModel.asynchronousIOComponent;

import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.util.concurrent.Executors;

/**
 * @Program: soldier
 * @Description: 异步IO服务器
 * @Author: Ackerman
 * @Create: 2019-01-12 11:36
 */
public class AsynchronousIOServer {
    private int threadPoolSize;
    private int acceptWaitingSize;
    private int port;

    private final Object waitObject = new Object();

    private void initConfiguration() {
        port = 8004;
        threadPoolSize = 4;
        acceptWaitingSize = 1024;

    }

    private void initServerSocket() {
        try {
            AsynchronousChannelGroup group =
                    AsynchronousChannelGroup.withCachedThreadPool(Executors.newCachedThreadPool(), threadPoolSize);

            AsynchronousServerSocketChannel server =
                    AsynchronousServerSocketChannel.open(group);

            InetSocketAddress address = new InetSocketAddress(port);
            server.bind(address, acceptWaitingSize);

            server.accept(null,  new AcceptCompletionHandler(server));

            synchronized (waitObject) {
                waitObject.wait();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void workProcess() {

    }

    public void start() {
        initConfiguration();
        initServerSocket();
        workProcess();
    }
}
