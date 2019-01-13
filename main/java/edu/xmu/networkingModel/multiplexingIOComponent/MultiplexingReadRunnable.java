package edu.xmu.networkingModel.multiplexingIOComponent;

import edu.xmu.baseConponent.RequestMessage;
import edu.xmu.baseConponent.RequestState;
import edu.xmu.baseConponent.RequestParseUtil;
import edu.xmu.baseConponent.http.HttpContext;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

/**
 * @Program: soldier
 * @Description: 事件驱动模式下的读工作线程
 * @Author: Ackerman
 * @Create: 2019-01-11 10:49
 */
public class MultiplexingReadRunnable implements Runnable {
    private static RequestParseUtil requestParseUtil = RequestParseUtil.getInstance();
    private static int MAX_BUF = 1024;
    private HttpContext httpContext;
    private SelectionKey key;
    private Selector selector;


    public MultiplexingReadRunnable(HttpContext httpContext) {
        this.httpContext = httpContext;
        this.key = httpContext.getSelectionKey();
        this.selector = httpContext.getSelector();
    }

    public void run() {
        SocketChannel client = null;
        try {
            client = (SocketChannel) key.channel();
            RequestState state   = recvFrom();

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
                    key.channel().close();  // TODO TEST
                    break;
            }
//            System.out.println("yes: " + state);
        } catch (Exception e) {
            e.printStackTrace();
        }

//        httpContext.getRequest().initRequestAttribute();
//        if (httpContext != null) {
//            System.out.println(httpContext.getRequest());
//        }
    }

    /**
     * @Description: 处理从缓冲区获取MAX_BUF的数据, 进行处理
     *               可能从缓冲区读到cnt个字节, 可能的大小如下:
     *               cnt <  0: 读取发生错误, 返回PARSE_ERROR
     *               cnt == 0: 假唤醒,      返回PARSE_MORE
     *               cnt >  0: 解析数据流,  返回解析结果state
     *
     * @Date  : 2019/1/11
     * @Param : HTTP的上下文
     * @Return: 请求状态
     */
    private RequestState recvFrom() {
        SocketChannel client = (SocketChannel) key.channel();
        ByteBuffer buffer    = ByteBuffer.allocate(MAX_BUF);

        RequestMessage rs = httpContext.getRequest().getRequestMessage();
        RequestState state = rs.getState();
        int nread;

        try {
            while ((nread = client.read(buffer)) != -1) {
                rs.setPbuf(0);

                if (nread < 0) {
                    return RequestState.PARSE_ERROR;
                }
                else if (nread == 0) {  // 只有当client管道读不出更多的数据, 才需要向内核缓冲区索要数据
                    return RequestState.PARSE_MORE;
                }
                else {
                    byte []bytes = buffer.array();

//                    for (int i = 0; i < nread; ++i) {
//                        char ch = (char) bytes[i];
//                        System.out.print(ch);
//                    }

                    buffer.flip();  // 更新buffer pos的位置

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
                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        return state;
    }


}
