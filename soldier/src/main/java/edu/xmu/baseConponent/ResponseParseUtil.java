package edu.xmu.baseConponent;

import edu.xmu.baseConponent.http.HttpContext;
import edu.xmu.baseConponent.http.HttpResponse;
import edu.xmu.baseConponent.http.Response;

import java.util.Arrays;

/**
 * @Program: soldier
 * @Description:
 * @Author: Ackerman
 * @Create: 2019-01-12 16:21
 */
public class ResponseParseUtil {
    private volatile static ResponseParseUtil responseParseUtil;

    private ResponseParseUtil() {

    }

    public static ResponseParseUtil getInstance () {
        if (responseParseUtil == null) {
            synchronized (ResponseParseUtil.class) {
                if (responseParseUtil == null) {
                    responseParseUtil = new ResponseParseUtil();
                }
            }
        }
        return responseParseUtil;
    }

    public ResponseState parseHttpResponse(HttpContext httpContext) {
        char []chars = new char[512];
        Arrays.fill(chars, '*');
        String content = new String(chars);
        String contentType = "text/plain";
        int contentLength  = 1024;

        try {
            Response response = httpContext.getResponse();
            response.setContent(content);
            response.setContentType(contentType);
            response.setContentLength(contentLength);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ResponseState.OK;
    }

}
