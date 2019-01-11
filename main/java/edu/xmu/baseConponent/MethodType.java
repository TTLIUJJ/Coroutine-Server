package edu.xmu.baseConponent;

public enum MethodType {
    PUT("PUT"),
    DELETE("DELETE"),
    POST("POST"),
    GET ("GET"),
    PATCH("PATCH"),
    HEAD("HEAD"),
    OPTIONS("OPTIONS"),
    TRACE("TRACE"),
    ;

    private String method;
    private MethodType(String method) {
        this.method = method;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }
}
