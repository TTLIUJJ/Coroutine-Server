package edu.xmu.networkingModel.eventDrivenConponent;

import edu.xmu.baseConponent.http.HttpContext;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.*;

/**
 * @Program: soldier
 * @Description: 事件驱动网络模型的服务器
 * @Author: Ackerman
 * @Create: 2019-01-11 10:17
 */
public class EventDrivenServer {
    private int   port = 8080;
    private int   corePoolSize = 2;
    private int   maxPoolSize  = 4;
    private long keepAliveTime = 10;
    private ArrayBlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<Runnable>(1000);
    private Selector selector;
    private ExecutorService threadPoolExecutor;

    private void initConfiguration() {
        threadPoolExecutor = new ThreadPoolExecutor(
                corePoolSize,
                maxPoolSize,
                keepAliveTime,
                TimeUnit.SECONDS,
                workQueue,
                new RejectedExecutionHandler() {
                    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {

                    }
                }
        );

    }

    private void initServerSocket() {
        ServerSocketChannel serverSocketChannel;

        try {
            serverSocketChannel = ServerSocketChannel.open();
            ServerSocket serverSocket = serverSocketChannel.socket();
            InetSocketAddress address = new InetSocketAddress(port);
            serverSocket.bind(address);
            serverSocketChannel.configureBlocking(false);

            selector = Selector.open();
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private void workProcess() {
        while (true) {
            int readyChannels = 0;
            try {
                readyChannels = selector.select();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }

            if (readyChannels == 0) {
                continue;
            }
            Set<SelectionKey> readyKeys     = selector.selectedKeys();
            Iterator<SelectionKey> iterator = readyKeys.iterator();

            while(iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();
                if (!key.isValid()) {
                    continue;
                }

                try {
                    if (key.isAcceptable()) {
                        ServerSocketChannel server = (ServerSocketChannel) key.channel();
                        SocketChannel client = server.accept();
                        if (client == null) {
                            continue;
                        }
                        client.configureBlocking(false);
                        SelectionKey clientKey = client.register(selector, SelectionKey.OP_READ);
                        HttpContext httpContext = new HttpContext(selector, clientKey);
                        clientKey.attach(httpContext);
                    }
                    else if (key.isReadable()) {
                        key.interestOps(key.interestOps() & (~SelectionKey.OP_READ));
                        HttpContext httpContext = (HttpContext) key.attachment();
                        // TODO with readthread
                        ReadThread readThread = new ReadThread(httpContext);
                        threadPoolExecutor.execute(readThread);
                    }
                    else if (key.isWritable()) {
                        key.interestOps(key.interestOps() & (~SelectionKey.OP_WRITE));
                        HttpContext httpContext = (HttpContext) key.attachment();
                        // TODO with writeThread
                    }
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        }
    }

    public void start() {
        initConfiguration();
        initServerSocket();
        workProcess();
    }
}
