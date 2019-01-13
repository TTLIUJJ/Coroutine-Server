package edu.xmu.networkingModel.multiplexingIOComponent;

import edu.xmu.baseConponent.ResponseParseUtil;
import edu.xmu.baseConponent.ResponseState;
import edu.xmu.baseConponent.http.HttpContext;
import edu.xmu.baseConponent.http.Response;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Date;

/**
 * @Program: soldier
 * @Description:
 * @Author: Ackerman
 * @Create: 2019-01-12 16:20
 */
public class MultiplexingWriteRunnable implements Runnable{
    private static ResponseParseUtil responseParseUtil = ResponseParseUtil.getInstance();
    private HttpContext httpContext;
    private SelectionKey key;
    private Selector selector;


    public MultiplexingWriteRunnable(HttpContext httpContext) {
        this.httpContext = httpContext;
        this.key = httpContext.getSelectionKey();
        this.selector = httpContext.getSelector();
    }

    public void run() {
        SocketChannel client = null;
        try {
            client = (SocketChannel) key.channel();
            ResponseState state  = responseParseUtil.parseHttpResponse(httpContext);
            int code = state.getCode();
            Response response = httpContext.getResponse();

            switch (code) {
                case 200:
                    String body  = response.getContent();
                    String line   = "HTTP/1.1 200 OK\r\n";
                    String header =
                                    "Date: " + new Date() + "\r\n" +
                                    "Server: Ackerman\r\n" +
                                    "Content-type: text/html\r\n" +
                                    "Content-length: " + body.length() + "\r\n" +
                                    "Connection: close\r\n\r\n";

                    String res = line + header + body;

                    ByteBuffer buffer = ByteBuffer.wrap(res.getBytes());
                    client.write(buffer);
                    break;

                case 300:

                    break;

                case 400:

                    break;

                case 500:

                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                key = null;
                if (client != null) {
                    client.socket().close();
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }
}
