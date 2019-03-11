package edu.xmu.networkingModel;

import edu.xmu.baseConponent.*;
import edu.xmu.baseConponent.http.HttpContext;
import edu.xmu.baseConponent.http.Response;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Date;

/**
 * @Program: soldier
 * @Description: 阻塞IO和非阻塞IO的工作线程
 * @Author: Ackerman
 * @Create: 2019-01-13 15:14
 */
public abstract class AbstractWorkRunnable implements Runnable {
    private static RequestParseUtil requestParseUtil   = RequestParseUtil.getInstance() ;
    private static ResponseParseUtil responseParseUtil = ResponseParseUtil.getInstance();
    private static int MAX_BUF = 1024;
    protected Socket client;
    protected HttpContext httpContext;

    public AbstractWorkRunnable(Socket client) {
        this.client = client;
        this.httpContext = new HttpContext();
    }

    public void run() {
        try {
            RequestState state = recvFrom();

            switch (state) {
                case PARSE_OK:
                    httpContext.getRequest().initRequestAttribute();
                    System.out.println(httpContext.getRequest());
                    sendTo();
                    break;

                default:
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            httpContext = null;     // help gc
            try {
                if (client != null && !client.isClosed()) {
                    client.close();
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    private RequestState recvFrom() {
        RequestMessage rs = httpContext.getRequest().getRequestMessage();
        RequestState state = null;
        byte []bytes = new byte[MAX_BUF];
        int nread;

        try {
            InputStream inputStream = client.getInputStream();
            while ((nread = inputStream.read(bytes)) != -1) {
                rs.setPbuf(0);

//                for (int i = 0; i < nread; ++i) {
//                    char ch = (char) bytes[i];
//                    System.out.print(ch);
//                }

                if (nread == 0) {
                    continue;
                }
                else {
                    if (!rs.isFinishLine()) {
                        state = requestParseUtil.parseHttpRequestLine(rs, bytes);
                    }
                    if (state == RequestState.PARSE_OK ||
                            state == RequestState.PARSE_ERROR) {

                        break;
                    }

                    if (!rs.isFinishHeader()) {
                        state = requestParseUtil.parseHttpRequestHeader(rs, bytes);
                    }
                    if (state == RequestState.PARSE_OK ||
                            state == RequestState.PARSE_ERROR) {

                        break;
                    }
                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
            state = RequestState.PARSE_ERROR;
        }
//        System.out.println(state);
        return state;
    }

    private void sendTo() {
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
                    OutputStream outputStream = client.getOutputStream();
                    outputStream.write(res.getBytes());
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
