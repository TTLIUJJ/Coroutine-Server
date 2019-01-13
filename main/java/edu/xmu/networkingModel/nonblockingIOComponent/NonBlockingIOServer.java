package edu.xmu.networkingModel.nonblockingIOComponent;

import edu.xmu.networkingModel.blockingIOComponent.BlockingWorkRunnable;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Program: soldier
 * @Description: 同步非阻塞IO服务器
 * @Author: Ackerman
 * @Create: 2019-01-12 11:35
 */
public class NonBlockingIOServer {
    private int port;
    private int OPEN_SIZE;   // TODO get system open_size
    private ExecutorService threadPoolExecutor;
    private ArrayBlockingQueue<NonBlockingWorkRunnable> nonBlockingWorkRunnables;

    private void initConfiguration() {
        port = 8002;
        OPEN_SIZE = 7168;
        threadPoolExecutor = Executors.newCachedThreadPool();
        nonBlockingWorkRunnables = new ArrayBlockingQueue<NonBlockingWorkRunnable>(OPEN_SIZE);
    }

    private void initServerSocket() {
        new Thread(new Runnable() {
            public void run() {
                ServerSocket server = null;
                try {
                    server = new ServerSocket(port);
                    while (true) {
                        Socket client = null;
                        try {
                            client = server.accept();
                            System.out.println("accept");

                            NonBlockingWorkRunnable nonBlockingWorkRunnable = new NonBlockingWorkRunnable(client);
                            nonBlockingWorkRunnables.add(nonBlockingWorkRunnable);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                } finally {
                    try {
                        if (server != null) {
                            server.close();
                        }
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private void workProcess() {


        while (true) {
            Iterator<NonBlockingWorkRunnable> iterator = nonBlockingWorkRunnables.iterator();
            while (iterator.hasNext()) {
                NonBlockingWorkRunnable nonBlockingWorkRunnable = iterator.next();
                if (nonBlockingWorkRunnable.ready()) {
                    iterator.remove();
                    threadPoolExecutor.execute(nonBlockingWorkRunnable);
                }
            }
        }

    }

    public void start() {
        initConfiguration();
        initServerSocket();
        workProcess();
    }



    public static void main(String []args) {
        ArrayList<Integer> list = new ArrayList<Integer>();
        list.add(1);
        list.add(2);
        list.add(3);

        Iterator<Integer> iterator = list.iterator();
        while (iterator.hasNext()) {
            int x = iterator.next();
            if (x == 2) {
                iterator.remove();
            }
        }

        for (int x : list) {
            System.out.println(x);
        }

    }
}
