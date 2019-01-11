package edu.xmu.baseConponent.http;

import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

/**
 * @Program: soldier
 * @Description: 保存HTTP请求和回复的上下文
 * @Author: Ackerman
 * @Create: 2019-01-10 19:12
 */
public class HttpContext {
    private Selector selector;
    private SelectionKey selectionKey;
    private Request  request;
    private Response response;

    public HttpContext(Selector selector, SelectionKey selectionKey) {
        request  = new HttpRequest();
        response = new HttpResponse();
        this.selector = selector;
        this.selectionKey = selectionKey;
    }

    public Selector getSelector() {
        return selector;
    }

    public void setSelector(Selector selector) {
        this.selector = selector;
    }

    public SelectionKey getSelectionKey() {
        return selectionKey;
    }

    public void setSelectionKey(SelectionKey selectionKey) {
        this.selectionKey = selectionKey;
    }

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    public Response getResponse() {
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
    }
}
