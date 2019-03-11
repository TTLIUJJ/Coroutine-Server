package edu.xmu.networkingModel.asynchronousIOComponent;

import edu.xmu.baseConponent.http.HttpContext;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

/**
 * @Program: soldier
 * @Description:
 * @Author: Ackerman
 * @Create: 2019-01-13 16:58
 */
public class AcceptCompletionHandler implements
        CompletionHandler<AsynchronousSocketChannel, Void> {

    private static int BUFF_SIZE = 1024;
    private AsynchronousServerSocketChannel server;

    public AcceptCompletionHandler(AsynchronousServerSocketChannel server) {
        this.server = server;
    }

    @Override
    public void completed(AsynchronousSocketChannel client, Void attachment) {
        server.accept(attachment, this);

        ByteBuffer buffer = ByteBuffer.allocate(BUFF_SIZE);
        client.read(buffer, new HttpContext(), new ReadCompletionHandler(client, buffer));
    }

    @Override
    public void failed(Throwable exc, Void attachment) {
        System.out.println(this.getClass().getName() + " failed");
    }
}
