package edu.xmu.networkingModel.asynchronousIOComponent;

import edu.xmu.baseConponent.ResponseParseUtil;
import edu.xmu.baseConponent.ResponseState;
import edu.xmu.baseConponent.http.HttpContext;
import edu.xmu.baseConponent.http.Response;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Date;

/**
 * @Program: soldier
 * @Description:
 * @Author: Ackerman
 * @Create: 2019-01-13 22:30
 */
public class WriteCompletionHandler
        implements CompletionHandler<Integer, HttpContext> {

    private static ResponseParseUtil responseParseUtil = ResponseParseUtil.getInstance();
    private AsynchronousSocketChannel client;
    private ByteBuffer buffer;
    private boolean done;

    public WriteCompletionHandler(AsynchronousSocketChannel client, HttpContext httpContext) {
        this.client = client;
        sendTo(httpContext);
    }

    @Override
    public void completed(Integer result, HttpContext attachment) {
        // TODO: if buffer is limited, that write model should be reset
        if (!done) {
            sendTo(attachment);
            client.write(buffer, attachment, this);
        }
        else {
            // TODO: after written
        }

    }

    @Override
    public void failed(Throwable exc, HttpContext attachment) {

    }

    public ByteBuffer getBuffer() {
        return buffer;
    }

    private void sendTo(HttpContext httpContext) {
        try {
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
                    buffer = ByteBuffer.wrap(res.getBytes());
//                    client.write(buffer);

//
                    done = true;

                    //TODO safe close socket.
//                    client.close();
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
        }
    }
}
