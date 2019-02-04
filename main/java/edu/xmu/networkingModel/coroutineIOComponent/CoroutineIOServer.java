package edu.xmu.networkingModel.coroutineIOComponent;

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

/**
 * @Program: soldier
 * @Description: 协程服务器（异步非阻塞的实现）
 * @Author: Ackerman
 * @Create: 2019-01-12 11:41
 */
public class CoroutineIOServer {
    private int port = 8005;
    private Selector selector;

    private void initConfiguration() {

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
                        // TODO with c-caller finish read
                        
                    }
                    else if (key.isWritable()) {
                        key.interestOps(key.interestOps() & (~SelectionKey.OP_WRITE));
                        HttpContext httpContext = (HttpContext) key.attachment();
                        // TODO with c-caller finish write

//                        System.out.println(httpContext.getRequest());


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
