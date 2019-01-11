package edu.xmu.networkingModel.eventDrivenConponent;

import edu.xmu.baseConponent.RequestParseState;
import edu.xmu.baseConponent.RequestParseUtil;
import edu.xmu.baseConponent.http.HttpContext;

import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

/**
 * @Program: soldier
 * @Description: 事件驱动模式下的读工作线程
 * @Author: Ackerman
 * @Create: 2019-01-11 10:49
 */
public class ReadThread implements Runnable {
    private static RequestParseUtil requestParseUtil = RequestParseUtil.getInstance();
    private HttpContext httpContext;
    private SelectionKey key;
    private Selector selector;


    public ReadThread(HttpContext httpContext) {
        this.httpContext = httpContext;
        this.key = httpContext.getSelectionKey();
        this.selector = httpContext.getSelector();
    }

    public void run() {

        try {
            SocketChannel client    = (SocketChannel) key.channel();
            RequestParseState state = requestParseUtil.recvFrom(httpContext);

            switch (state) {
                case PARSE_OK:
                    httpContext.getRequest().initRequestAttribute();
                    client.register(selector, SelectionKey.OP_WRITE, httpContext);
                    selector.wakeup();
                    break;

                case PARSE_MORE:
                    client.register(selector, SelectionKey.OP_READ, httpContext);
                    break;

                default:
                    httpContext = null;
                    break;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        httpContext.getRequest().initRequestAttribute();
        if (httpContext != null) {
            System.out.println(httpContext.getRequest());
        }
    }
}
