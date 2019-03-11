package edu.xmu.networkingModel.asynchronousIOComponent;

import edu.xmu.baseConponent.RequestMessage;
import edu.xmu.baseConponent.RequestParseUtil;
import edu.xmu.baseConponent.RequestState;
import edu.xmu.baseConponent.http.HttpContext;
import edu.xmu.baseConponent.http.Response;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Date;

/**
 * @Program: soldier
 * @Description:
 * @Author: Ackerman
 * @Create: 2019-01-13 20:17
 */
public class ReadCompletionHandler implements CompletionHandler<Integer, HttpContext> {
    private static RequestParseUtil requestParseUtil = RequestParseUtil.getInstance();
    private static int MAX_SIZE = 5;
    private AsynchronousSocketChannel client;
    private ByteBuffer buffer;

    public ReadCompletionHandler(AsynchronousSocketChannel client, ByteBuffer buffer) {
        this.client = client;
        this.buffer = buffer;
    }

    @Override
    public void completed(Integer result, HttpContext attachment) {
        if (result == -1) {
            try {
                client.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }

            return;
        }

        RequestState state = recvFrom(attachment, result);
        switch (state) {
            case PARSE_OK:
                // TODO return write To socketChannel
                attachment.getRequest().initRequestAttribute();

//                String body  = "123";
//                String line   = "HTTP/1.1 200 OK\r\n";
//                String header =
//                        "Date: " + new Date() + "\r\n" +
//                                "Server: Ackerman\r\n" +
//                                "Content-type: text/html\r\n" +
//                                "Content-length: " + body.length() + "\r\n" +
//                                "Connection: close\r\n\r\n";
//
//                String res = line + header + body;
//                client.write(ByteBuffer.wrap(res.getBytes()));

                WriteCompletionHandler writeCompletionHandler = new WriteCompletionHandler(client, attachment);
                client.write(writeCompletionHandler.getBuffer(), attachment, writeCompletionHandler);

                return;

            case PARSE_MORE:
                // TODO 继续处理监听剩余到来的信息
                client.read(buffer, attachment, this);
                break;

            case PARSE_ERROR:

                return;
        }


    }

    @Override
    public void failed(Throwable exc, HttpContext attachment) {

    }


    private RequestState recvFrom(HttpContext httpContext, int length) {
        RequestMessage rs = httpContext.getRequest().getRequestMessage();
        RequestState state = rs.getState();
        rs.setPbuf(0);

        byte []buf = new byte[MAX_SIZE];
        buffer.flip();
        buffer.get(buf, 0, length < MAX_SIZE ? length : MAX_SIZE);
        buffer.clear();

        byte []bytes = buffer.array();

        if (!rs.isFinishLine()) {
            state = requestParseUtil.parseHttpRequestLine(rs, bytes);
        }
        if (state == RequestState.PARSE_OK ||
                state == RequestState.PARSE_ERROR) {

            return state;
        }

        if (!rs.isFinishHeader()) {
            state = requestParseUtil.parseHttpRequestHeader(rs, bytes);
        }
        if (state == RequestState.PARSE_OK ||
                state == RequestState.PARSE_ERROR) {

            return state;
        }

        return RequestState.PARSE_MORE;
    }
}
