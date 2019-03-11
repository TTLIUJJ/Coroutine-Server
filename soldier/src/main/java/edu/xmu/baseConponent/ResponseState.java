package edu.xmu.baseConponent;


/**
 * @Program: soldier
 * @Description: HTTP 响应状态
 * @Author: Ackerman
 * @Create: 2019-01-12 15:09
 */
public enum  ResponseState {
    OK(200, "OK", "一切正常，对GET和POST请求的应答文档跟在后面"),

    MOVE_PERMANENTLY(301, "MOVE PERMANENTLY", "永久重定向，新的URL在Location头中，浏览器应自动访问新的URL"),
    MOVE_FOUND(302, "MOVE FOUND", "临时重定向"),
    SEE_OTEHER(303, "SEE_OTHER", "浏览器可以跟随GET和POST的重定向"),
    NOT_MODIFIED(304, "NOT MODIFIED", "资源被缓存, 有协商缓存和强制缓两种"),
    TEMPORARY_REDIRECT(307, "TEMPORARY REDIRECT", "浏览器只能跟随GET请求的重定向"),

    BAD_REQUEST(400, "BAD REQUEST", "请求出现语法错误"),
    UNAUTHORIZED(401, "UNAUTHORIZED", "访问被拒绝，客户试图未经授权访问受密码保护的页面"),
    FORBIDDEN(403, "FORBIDDEN", "资源不可用，服务器理解客户的请求，但是拒绝处理"),
    NOT_FOUND(404, "NOT FOUND", "无法找到指定位置的资源"),
    METHOD_NOT_ALLOWED(405, "METHOD NOT ALLOWED", "请求方法对指定的资源不适用"),

    INTERNAL_SERVER_ERROR(500, "INTERNAL SERVER ERROR", "服务器遇到了意料不到的情况，不能完成客户的请求"),
    NOT_IMPLEMENTEED(501, "NOT IMPLEMENTED", "服务器不支持请求需要的功能"),
    BAD_GATEWAY(502, "BAD GATEWAY", "服务器作为网关或者代理时，为了完成请求访问下个服务器，却得到了非法的回答"),
    SERVICE_UNAVAILABLE(503, "SERVICE UNAVAILABLE", "服务器由于维护或者负载过重未能应答"),
    GATEWAY_TIMEOUT(504, "GATEWAY TIMEOUT", "网关超时，表示不能及时从远程服务器得到应答"),
    HTTP_VERSION_NOT_SUPPORTED(505, "HTTP VERSION NOT SUPPORTED", "服务器不支持请求中的指明的HTTP版本"),

    ;

    private int code;
    private String msg;
    private String desc;

    private ResponseState(int code, String msg, String desc) {
        this.code = code;
        this.msg    = msg;
        this.desc   = desc;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public String getDesc() {
        return desc;
    }
}
