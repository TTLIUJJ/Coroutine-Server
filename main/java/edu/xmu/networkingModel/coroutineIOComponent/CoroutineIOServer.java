package edu.xmu.networkingModel.coroutineIOComponent;


import edu.xmu.baseConponent.RequestMessage;
import edu.xmu.baseConponent.RequestParseUtil;
import edu.xmu.baseConponent.RequestState;
import edu.xmu.baseConponent.http.HttpContext;

import java.util.HashMap;

/**
 * @Program: soldier
 * @Description: 协程服务器（异步非阻塞的实现）
 * @Author: Ackerman
 * @Create: 2019-01-12 11:41
 */
public class CoroutineIOServer {
    private HashMap<Integer, HttpContext> fdContextMap;
    private RequestParseUtil requestParseUtil;

    public CoroutineIOServer() {
        fdContextMap = new HashMap<>();
        requestParseUtil = RequestParseUtil.getInstance();
    }

    public int parseRequest(int client_fd, char []buf) {
        if (!fdContextMap.containsKey(client_fd)) {
            HttpContext httpContext = new HttpContext();
            fdContextMap.put(client_fd, httpContext);
        }

        byte []bytes = new String(buf).getBytes();
        HttpContext httpContext = fdContextMap.get(client_fd);
        RequestMessage rs = httpContext.getRequest().getRequestMessage();
        RequestState state = innerParseRequest(rs, bytes);

        switch (state) {
            case PARSE_OK:
                httpContext.getRequest().initRequestAttribute();
                return 0;

            case PARSE_MORE:

                return 1;

            default:
                destroyRequestMessage(client_fd);
                return -1;
        }
    }

    private RequestState innerParseRequest(RequestMessage rs, byte []buf) {

        RequestState state = rs.getState();

        try {
            if (state == RequestState.PARSE_OK ||
                    state == RequestState.PARSE_ERROR) {

                return state;
            }

            if (!rs.isFinishHeader()) {
                state = requestParseUtil.parseHttpRequestHeader(rs, buf);
            }
            if (state == RequestState.PARSE_OK ||
                    state == RequestState.PARSE_ERROR) {

                return state;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return state;
    }

    public void destroyRequestMessage(int client_fd) {
        HttpContext httpContext = fdContextMap.get(client_fd);
        httpContext = null;
        fdContextMap.remove(client_fd);
    }
}
