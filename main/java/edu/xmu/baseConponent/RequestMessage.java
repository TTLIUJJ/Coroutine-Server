package edu.xmu.baseConponent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @Program: soldier
 * @Description: 保存每个HTTP请求的相关信息
 * @Author: Ackerman
 * @Create: 2019-01-10 18:39
 */
public class RequestMessage {
    private ArrayList<Byte> message;    // 请求的原始数据
                                        // 可以解决半包和粘包造成的数据丢

    private RequestState state;    // 保存当前请求的状态
    private boolean finishLine;
    private boolean finishHeader;
    private int  pos;                   // 指向当前请求的位置, 也是目前message的位置
    private int pbuf;                   // 指向当前缓存buff的位置

    private int methodBeg;              // 记录请求的方法
    private int methodEnd;
    private MethodType methodType;

    private int uriBeg;                 // 记录请求的资源
    private int uriEnd;
    private String uri;

    private int major;                  // 记录请求的版本号
    private int minor;

    private int keyBeg;
    private int keyEnd;
    private int valueBeg;
    private int valueEnd;
    private Map<String, String> headers;    // 记录请求头

    public RequestMessage() {
        message = new ArrayList<Byte>();
        state = RequestState.LINE_START;
        headers = new HashMap<String, String>();
    }

    public ArrayList<Byte> getMessage() {
        return message;
    }

    public void setMessage(ArrayList<Byte> message) {
        this.message = message;
    }

    public RequestState getState() {
        return state;
    }

    public void setState(RequestState state) {
        this.state = state;
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public int getPbuf() {
        return pbuf;
    }

    public void setPbuf(int pbuf) {
        this.pbuf = pbuf;
    }

    public int getMethodBeg() {
        return methodBeg;
    }

    public void setMethodBeg(int methodBeg) {
        this.methodBeg = methodBeg;
    }

    public int getMethodEnd() {
        return methodEnd;
    }

    public void setMethodEnd(int methodEnd) {
        this.methodEnd = methodEnd;
    }

    public MethodType getMethodType() {
        return methodType;
    }

    public void setMethodType(MethodType methodType) {
        this.methodType = methodType;
    }

    public int getUriBeg() {
        return uriBeg;
    }

    public void setUriBeg(int uriBeg) {
        this.uriBeg = uriBeg;
    }

    public int getUriEnd() {
        return uriEnd;
    }

    public void setUriEnd(int uriEnd) {
        this.uriEnd = uriEnd;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public int getMajor() {
        return major;
    }

    public void setMajor(int major) {
        this.major = major;
    }

    public int getMinor() {
        return minor;
    }

    public void setMinor(int minor) {
        this.minor = minor;
    }

    public int getKeyBeg() {
        return keyBeg;
    }

    public void setKeyBeg(int keyBeg) {
        this.keyBeg = keyBeg;
    }

    public int getKeyEnd() {
        return keyEnd;
    }

    public void setKeyEnd(int keyEnd) {
        this.keyEnd = keyEnd;
    }

    public int getValueBeg() {
        return valueBeg;
    }

    public void setValueBeg(int valueBeg) {
        this.valueBeg = valueBeg;
    }

    public int getValueEnd() {
        return valueEnd;
    }

    public void setValueEnd(int valueEnd) {
        this.valueEnd = valueEnd;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public boolean isFinishLine() {
        return finishLine;
    }

    public void setFinishLine(boolean finishLine) {
        this.finishLine = finishLine;
    }

    public boolean isFinishHeader() {
        return finishHeader;
    }

    public void setFinishHeader(boolean finishHeader) {
        this.finishHeader = finishHeader;
    }
}
