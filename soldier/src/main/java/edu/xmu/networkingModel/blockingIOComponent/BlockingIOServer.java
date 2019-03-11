package edu.xmu.networkingModel.blockingIOComponent;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

/**
 * @Program: soldier
 * @Description: 同步阻塞IO服务器
 * @Author: Ackerman
 * @Create: 2019-01-12 11:34
 */
public class BlockingIOServer {
    private static int port = 8001;
    private ExecutorService threadPoolExecutor;

    private void initConfiguration() {
        threadPoolExecutor = Executors.newCachedThreadPool();
    }


    private void initServerSocket() {
        ServerSocket server = null;
        try {
            server = new ServerSocket(port);
            while (true) {
                Socket client = null;
                try {
                    client = server.accept();
                    BlockingWorkRunnable blockingWorkRunnable = new BlockingWorkRunnable(client);
                    threadPoolExecutor.execute(blockingWorkRunnable);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private void workProcess() {

    }

    public void start() {
        initConfiguration();
        initServerSocket();
    }
}
