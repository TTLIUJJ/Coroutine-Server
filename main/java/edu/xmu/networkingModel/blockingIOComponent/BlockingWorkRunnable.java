package edu.xmu.networkingModel.blockingIOComponent;


import edu.xmu.networkingModel.AbstractWorkRunnable;

import java.net.Socket;

/**
 * @Program: soldier
 * @Description:
 * @Author: Ackerman
 * @Create: 2019-01-12 22:54
 */
public class BlockingWorkRunnable extends AbstractWorkRunnable {
    public BlockingWorkRunnable(Socket client) {
        super(client);
    }

    @Override
    public void run() {
        super.run();
    }
}
