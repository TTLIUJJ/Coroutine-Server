package edu.xmu.networkingModel.coroutineIOComponent;


import edu.xmu.baseConponent.RequestMessage;
import edu.xmu.baseConponent.RequestParseUtil;
import edu.xmu.baseConponent.RequestState;
import edu.xmu.baseConponent.http.HttpContext;

import java.util.Date;
import java.util.HashMap;

/**
 * @Program: soldier
 * @Description: 协程服务器（异步非阻塞的实现）
 * @Author: Ackerman
 * @Create: 2019-01-12 11:41
 */
public class CoroutineIOServer {
    private static HashMap<Integer, HttpContext> fdContextMap = new HashMap<>();
    private static RequestParseUtil requestParseUtil = RequestParseUtil.getInstance();
    private static String parse_error = "parse error";

    public static int test() {
        return 111;
    }

    public static String parseRequest(int client_fd, String request) {
        try {
//            if (request == null) {
//                System.out.println("buf is null");
//            }
//            else {
//                System.out.println("buf is not null");
//                System.out.println("in java: " + client_fd + ", " + request);
//            }

        }catch (Exception e) {
            e.printStackTrace();
        }
        String body  = "jiajia is jiajing's precious~";
        String line   = "HTTP/1.1 200 OK\r\n";
        String header =
                "Date: " + new Date() + "\r\n" +
                        "Server: Ackerman\r\n" +
                        "Content-type: text/html\r\n" +
                        "Content-length: " + body.length() + "\r\n" +
                        "Connection: close\r\n\r\n";

        String res = line + header + body;

        return res;
        /*return "i am ok";*/
//        if (!fdContextMap.containsKey(client_fd)) {
//            HttpContext httpContext = new HttpContext();
//            fdContextMap.put(client_fd, httpContext);
//        }
//
//        byte []bytes = new String(buf).getBytes();
//        HttpContext httpContext = fdContextMap.get(client_fd);
//        RequestMessage rs = httpContext.getRequest().getRequestMessage();
//        RequestState state = innerParseRequest(rs, bytes);
//
//        switch (state) {
//            case PARSE_OK:
//                httpContext.getRequest().initRequestAttribute();
//                destroyRequestMessage(client_fd);
//                return "i am ok";
//
//            case PARSE_MORE:
//
//                return parse_more;
//
//            default:
//                destroyRequestMessage(client_fd);
//                return parse_error;
//        }
    }

    private static RequestState innerParseRequest(RequestMessage rs, byte []buf) {
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

    public static void destroyRequestMessage(int client_fd) {
        HttpContext httpContext = fdContextMap.get(client_fd);
        httpContext = null;
        fdContextMap.remove(client_fd);
    }
}
