package edu.xmu.baseConponent.http;

import edu.xmu.baseConponent.MethodType;
import edu.xmu.baseConponent.RequestMessage;

import java.util.Map;

public interface Request {
    public void initRequestAttribute();

    public MethodType getMethod();
    public void setMethod(MethodType method);

    public String getProtocol();
    public void setProtocol(String protocol);

    public String getURI();
    public void setURI(String uri);

    public Map<String, String> getHeaders();
    public void setHeaders(Map<String, String> headers);

    public RequestMessage getRequestMessage();
    public void setRequestMessage(RequestMessage requestMessage);
}
