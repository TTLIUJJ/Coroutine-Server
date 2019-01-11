package edu.xmu.baseConponent.http;

import edu.xmu.baseConponent.MethodType;
import edu.xmu.baseConponent.RequestMessage;

import java.util.Map;

/**
 * @Program: soldier
 * @Description: 保存HTTP请求信息
 * @Author: Ackerman
 * @Create: 2019-01-10 18:52
 */
public class HttpRequest implements Request{
    private MethodType methodType;
    private String uri;
    private String protocol;
    private Map<String, String> headers;
    private RequestMessage requestMessage;

    public HttpRequest() {
        requestMessage = new RequestMessage();
    }

    public void initRequestAttribute() {
        methodType   = requestMessage.getMethodType();
        uri      = requestMessage.getUri();
        protocol = requestMessage.getMajor() + "." + requestMessage.getMinor();
        headers  = requestMessage.getHeaders();
    }

    public String toString() {
        String res = methodType + " " + uri + " " + "HTTP/" + protocol + "\r\n\r\n";
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                res += entry.getKey() + ": " + entry.getValue() + "\r\n";
            }
            res += "\r\n\r\n";
        }
        return res;
    }

    public MethodType getMethod() {
        return methodType;
    }

    public void setMethod(MethodType methodType) {
        this.methodType = methodType;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getURI() {
        return uri;
    }

    public void setURI(String uri) {
        this.uri = uri;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public RequestMessage getRequestMessage() {
        return requestMessage;
    }

    public void setRequestMessage(RequestMessage requestMessage) {
        this.requestMessage = requestMessage;
    }
}
