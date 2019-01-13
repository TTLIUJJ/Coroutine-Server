package edu.xmu.baseConponent.http;

public interface Response {
    public int getStatusCode();
    public void setStatusCode(int statusCode);

    public String getStatusMsg();
    public void setStatusMsg(String statusMsg);

    public String getContent();
    public void setContent(String content);

    public String getContentType();
    public void setContentType(String contentType);

    public int getContentLength();
    public void setContentLength(int contentLength);
}
