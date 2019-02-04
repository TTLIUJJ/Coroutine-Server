package edu.xmu.networkingModel.nonblockingIOComponent;

import edu.xmu.networkingModel.AbstractWorkRunnable;

import java.io.InputStream;
import java.net.Socket;

/**
 * @Program: soldier
 * @Description: 非阻塞IO需要CPU轮询用户数据数据准备好, 如果准备好, 写入内核缓冲区
 * @Author: Ackerman
 * @Create: 2019-01-13 11:42
 */
public class NonBlockingWorkRunnable extends AbstractWorkRunnable {
    public NonBlockingWorkRunnable(Socket client) {
        super(client);
    }

    @Override
    public void run() {
        super.run();
    }

    public boolean ready() {
        InputStream inputStream;
        try {
            inputStream = client.getInputStream();
            int available = inputStream.available();
            if (available > 0) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

}
