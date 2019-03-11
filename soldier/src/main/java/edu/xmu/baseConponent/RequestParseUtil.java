package edu.xmu.baseConponent;

import edu.xmu.baseConponent.http.HttpContext;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Map;

/**
 * @Program: soldier
 * @Description: 解析HTTP请求的请求行 请求头 请求体
 * @Author: Ackerman
 * @Create: 2019-01-10 19:17
 */
public class RequestParseUtil {
    private static volatile RequestParseUtil requestParseUtil;

    /**
     * @Description: 线程安全的单例模式
     * @Date  : 2019/1/11
     */
    public static RequestParseUtil getInstance() {
        if (requestParseUtil == null) {
            synchronized (RequestParseUtil.class) {
                if (requestParseUtil == null) {
                    requestParseUtil = new RequestParseUtil();
                }
            }
        }
        return requestParseUtil;
    }

//    /**
//     * @Description: 处理从缓冲区获取MAX_BUF的数据, 进行处理
//     *               可能从缓冲区读到cnt个字节, 可能的大小如下:
//     *               cnt <  0: 读取发生错误, 返回PARSE_ERROR
//     *               cnt == 0: 假唤醒,      返回PARSE_MORE
//     *               cnt >  0: 解析数据流,  返回解析结果state
//     *
//     * @Date  : 2019/1/11
//     * @Param : HTTP的上下文
//     * @Return: 请求状态
//     */
//    public RequestState recvFrom(HttpContext httpContext) {
//        SelectionKey key     = httpContext.getSelectionKey();
//        SocketChannel client = (SocketChannel) key.channel();
//        ByteBuffer buffer    = ByteBuffer.allocate(MAX_BUF);
//
//        RequestMessage rs = httpContext.getRequest().getRequestMessage();
//        RequestState state = rs.getState();
//        int nread;
//
//        try {
//            while ((nread = client.read(buffer)) != -1) {
//                rs.setPbuf(0);
//
//                if (nread < 0) {
//                    return RequestState.PARSE_ERROR;
//                }
//                else if (nread == 0) {  // 只有当client管道读不出更多的数据, 才需要向内核缓冲区索要数据
//                    return RequestState.PARSE_MORE;
//                }
//                else {
//                    byte []bytes = buffer.array();
//
////                    for (int i = 0; i < nread; ++i) {
////                        char ch = (char) bytes[i];
////                        System.out.print(ch);
////                    }
//
//                    buffer.flip();  // 更新buffer pos的位置
//
//                    if (!rs.isFinishLine()) {
//                        state = parseHttpRequestLine(rs, bytes);
//                    }
//                    if (state == RequestState.PARSE_OK ||
//                            state == RequestState.PARSE_ERROR) {
//
//                        return state;
//                    }
//
//                    if (!rs.isFinishHeader()) {
//                        state = parseHttpRequestHeader(rs, bytes);
//                    }
//                    if (state == RequestState.PARSE_OK ||
//                            state == RequestState.PARSE_ERROR) {
//
//                        return state;
//                    }
//                }
//            }
//        } catch (IOException ioe) {
//            ioe.printStackTrace();
//        }
//
//        return state;
//    }


    /**
     * @Description: 解析HTTP请求行, 请求的信息流存放于buf中
     * @Date  : 2019/1/10
     * @Param : httpContext 请求上下文
     *          buf         函数一次处理的缓冲区大小
     * @Return: 请求的状态
     * TODO: 带?的uri请求格式
     */
    public RequestState parseHttpRequestLine(RequestMessage rs, byte[] buf) {
//        RequestMessage rs       = httpContext.getRequest().getRequestMessage();
        RequestState state = rs.getState();
        RequestState error = RequestState.PARSE_ERROR;
        ArrayList<Byte> message = rs.getMessage();
        int major = 0;
        int minor = 0;
        int len   = buf.length;
        int pos   = rs.getPos();    // 当前请求信息流的处理位置
        char ch;
        int   p;                    // 当前缓冲区buf的处理位置

        for (p = rs.getPbuf(); p < len; ++p) {
            byte b = buf[p];
            message.add(b);
            ch = (char)b;

            switch (state) {
                case LINE_START:
                    rs.setMethodBeg(pos);
                    if (ch == '\r' || ch == '\n') {
                        break;
                    }
                    if (!legalMethodAlpha(ch)) {
                        return error;
                    }
                    state = RequestState.LINE_METHOD;
                    break;

                case LINE_METHOD:
                    if (ch == ' ') {
                        rs.setMethodEnd(pos);
                        if (!legalAndSetMethod(rs)) {
                            return error;
                        }
                        state = RequestState.LINE_SPACES_BEFORE_URI;
                        break;
                    }
                    if (!legalMethodAlpha(ch)) {
                        return error;
                    }
                    break;

                case LINE_SPACES_BEFORE_URI:
                    switch (ch) {
                        case ' ':
                            break;
                        case '/':
                            rs.setUriBeg(pos);
                            state = RequestState.LINE_AFTER_SLASH_IN_URI;
                            break;
                        default:
                            return error;
                    }
                    break;

                case LINE_AFTER_SLASH_IN_URI:
                    if (ch == ' ') {
                        rs.setUriEnd(pos);
                        if (!legalAndSetURI(rs)) {
                            return error;
                        }
                        state = RequestState.LINE_HTTP;
                    }
                    break;

                case LINE_HTTP:
                    switch (ch) {
                        case ' ':
                            break;
                        case 'H':
                            state = RequestState.LINE_HTTP_H;
                            break;
                        default:
                            return error;
                    }
                    break;

                case LINE_HTTP_H:
                    switch (ch) {
                        case 'T':
                            state = RequestState.LINE_HTTP_HT;
                            break;
                        default:
                            return error;
                    }
                    break;

                case LINE_HTTP_HT:
                    switch (ch) {
                        case 'T':
                            state = RequestState.LINE_HTTP_HTT;
                            break;
                        default:
                            return error;
                    }
                    break;

                case LINE_HTTP_HTT:
                    switch (ch) {
                        case 'P':
                            state = RequestState.LINE_HTTP_HTTP;
                            break;
                        default:
                            return error;
                    }
                    break;

                case LINE_HTTP_HTTP:
                    switch (ch) {
                        case '/':
                            state = RequestState.LINE_FIRST_MAJOR_DIGIT;
                            break;
                        default:
                            return error;
                    }
                    break;

                case LINE_FIRST_MAJOR_DIGIT:
                    if (!digit1to9(ch)) {
                        return error;
                    }
                    major = ch - '0';
                    rs.setMajor(major);
                    state = RequestState.LINE_MAJOR_DIGIT;
                    break;

                case LINE_MAJOR_DIGIT:
                    if (ch == '.') {
                        state = RequestState.LINE_FIRST_MINOR_DIGIT;
                    }
                    else if(digit0to9(ch)) {
                        major = major * 10 + (ch - '0');
                        rs.setMajor(major);
                    }
                    else {
                        return error;
                    }
                    break;

                case LINE_FIRST_MINOR_DIGIT:
                    if (!digit0to9(ch)) {
                        return error;
                    }
                    minor = ch - '0';
                    rs.setMinor(minor);
                    state = RequestState.LINE_MINOR_DIGIT;
                    break;

                case LINE_MINOR_DIGIT:
                    if (digit0to9(ch)) {
                        minor = minor * 10 + (ch - '0');
                        rs.setMinor(minor);
                    }
                    else {
                        switch (ch) {
                            case ' ':
                                state = RequestState.LINE_SPACES_AFTER_DIGIT;
                                break;
                            case '\r':
                                state = RequestState.LINE_CR;
                                break;
                            case '\n':
                                return finishParseRequest(rs, RequestState.HEADER_START, pos+1, p+1);
                        }
                    }

                case LINE_SPACES_AFTER_DIGIT:
                    switch (ch) {
                        case ' ':
                            break;
                        case '\r':
                            state = RequestState.LINE_CR;
                            break;
                        case '\n':
                            return finishParseRequest(rs, RequestState.HEADER_START, pos+1, p+1);
                        default:
                            return error;
                    }
                    break;

                case LINE_CR:
                    switch (ch) {
                        case '\n':
                            return finishParseRequest(rs, RequestState.HEADER_START, pos+1, p+1);
                        default:
                            return error;
                    }
            }   // end of switch(state)
            ++pos;
        }

        /* 从此处返回肯定为 buf缓冲区信息解析完毕
         * pbuf 设置为 0, 等待下次缓冲区到来 ?????
         * state设置为 待解析的状态
         * pos  设置为 指向信息流的下一位置
         */
        rs.setPbuf(p);
        rs.setPos(pos);
        rs.setState(state);

        return state;
    }

    /**
     * @Description: 解析HTTP请求头, 请求的信息流存放于buf中
     * @Date  : 2019/1/10
     * @Param : httpContext 请求上下文
     *          buf         函数一次处理的缓冲区大小
     * @Return: 请求解析的状态
     */
    public RequestState parseHttpRequestHeader(RequestMessage rs, byte[] buf) {
//        RequestMessage       rs = httpContext.getRequest().getRequestMessage();
        RequestState state = rs.getState();
        RequestState error = RequestState.PARSE_ERROR;
        ArrayList<Byte> message = rs.getMessage();
        int len = buf.length;
        int pos = rs.getPos();
        char ch;
        int   p;

        for (p = rs.getPbuf(); p < len; ++p) {
            byte b = buf[p];
            message.add(b);
            ch = (char)b;

            switch (state) {
                case HEADER_START:
                    switch (ch) {
                        case ' ' :
                        case '\r':
                        case '\n':
                            break;

                        default:
                            rs.setKeyBeg(pos);
                            state = RequestState.HEADER_KEY;
                            break;

                    }
                    break;

                case HEADER_KEY:

                    switch (ch) {
                        case ' ':
                            rs.setKeyEnd(pos);
                            state = RequestState.HEADER_SPACES_BEFORE_COLON;
                            break;
                        case ':':
                            rs.setKeyEnd(pos);
                            state = RequestState.HEADER_SPACES_AFTER_COLON;
                            break;
                    }
                    break;

                case HEADER_SPACES_BEFORE_COLON:
                    switch (ch) {
                        case ' ':
                            break;
                        case ':':
                            state = RequestState.HEADER_SPACES_AFTER_COLON;
                            break;
                        default:
                            return error;
                    }
                    break;

                case HEADER_SPACES_AFTER_COLON:
                    switch (ch) {
                        case ' ' :
                            break;
                        default:
                            rs.setValueBeg(pos);
                            state = RequestState.HEADER_VALUE;
                            break;
                    }
                    break;

                case HEADER_VALUE:
                    switch (ch) {
                        case '\r':
                            rs.setValueEnd(pos);
                            state = RequestState.HEADER_CR;
                            addHeader(rs);
                            break;
                         default:
                             break;
                    }
                    break;

                case HEADER_CR:
                    switch (ch) {
                        case '\n':
                            state = RequestState.HEADER_CRLF;
                            break;
                        default:
                            return error;
                    }
                    break;

                case HEADER_CRLF:
                    switch (ch) {
                        case '\r':
                            state = RequestState.HEADER_CRLFCR;
                            break;
                        default:
                            rs.setKeyBeg(pos);
                            state = RequestState.HEADER_KEY;
                            break;
                    }
                    break;

                case HEADER_CRLFCR:
                    switch (ch) {
                        case '\n':
                            return finishParseRequest(rs, RequestState.PARSE_OK, pos+1, p+1);
                        default:
                            return error;
                    }

            }   // end of switch(state)
            ++pos;
        }

        rs.setPbuf(p);
        rs.setPos(pos);
        rs.setState(state);

        return state;
    }

    /**
     * @Description: 结束当前解析的请求行、请求头或者请求体
     * @Date  : 2019/1/10
     * @Param : rs   请求结构体
     *          next 待的解析状态
     *          pos  指向下次访问的请求信息流
     *          pbuf 指向下次访问的请求缓冲区
     * @Return: 返回待解析的状态
     *
     */
    private RequestState finishParseRequest(RequestMessage rs, RequestState next, int pos, int pbuf) {
        switch (next) {
            case HEADER_START:
                rs.setFinishLine(true);
                break;

//            case BODY_START:
//                rs.setFinishHeader(true);
//                break;
        }

        rs.setState(next);
        rs.setPos(pos);
        rs.setPbuf(pbuf);

        return next;
    }

    /**
     * @Description: 判断当前处理的字符是否为合法的方法字符之一
     * @Date  : 2019/1/10
     * @Param : ch 当前处理的字符
     * @Return: 合法返回true
     */
    private boolean legalMethodAlpha(char ch) {
        ch = Character.toUpperCase(ch);
        return ch > 'A' && ch < 'Z';
    }

    /**
     * @Description: 判断解析的方法是否符合规范, 如果是设置RequestMessage中的方法
     * @Date  : 2019/1/10
     * @Param : rs 请求流
     * @Return: 请求方法合法返回true
     */
    private boolean legalAndSetMethod(RequestMessage rs) {
        ArrayList<Byte> message = rs.getMessage();
        int beg  = rs.getMethodBeg();
        int end  = rs.getMethodEnd();
        char []a = new char[end - beg];

        for (int i = beg; i < end; ++i) {
            byte b = message.get(i);
            a[i-beg] = (char)b;
        }

        String method = new String(a);
        for (MethodType methodType : MethodType.values()) {
            if (methodType.getMethod().equals(method)) {
                rs.setMethodType(methodType);
                return true;
            }
        }
        return false;
    }

    /**
     * @Description: 判断解析的uri是否符合规范, 如果是设置RequestMessage中的uri
     * @Date  : 2019/1/10
     * @Param : rs 请求流
     * @Return: 请求uri合法返回true
     */
    private boolean legalAndSetURI(RequestMessage rs) {
        ArrayList<Byte> message = rs.getMessage();
        int beg  = rs.getUriBeg();
        int end  = rs.getUriEnd();
        char []a = new char[end - beg];

        for (int i = beg; i < end; ++i) {
            byte b = message.get(i);
            a[i-beg] = Character.toLowerCase((char) b);
        }

        String uri = new String(a);
        rs.setUri(uri);

        return uri.startsWith("/");
    }

    private boolean digit0to9(char ch) {
        return ch >= '0' && ch <= '9';
    }

    private boolean digit1to9(char ch) {
        return ch >= '1' && ch <= '9';
    }

    /**
     * @Description: 封装请求头属性
     * @Date  : 2019/1/10
     */
    private void addHeader(RequestMessage rs) {
        ArrayList<Byte> message     = rs.getMessage();
        Map<String, String> headers = rs.getHeaders();
        int keyBeg = rs.getKeyBeg();
        int keyEnd = rs.getKeyEnd();
        int valBeg = rs.getValueBeg();
        int valEnd = rs.getValueEnd();
        char []key = new char[keyEnd - keyBeg];
        char []val = new char[valEnd - valBeg];

        for (int i = keyBeg; i < keyEnd; ++i) {
            byte b = message.get(i);
            key[i-keyBeg] = Character.toLowerCase((char)b);
        }

        for (int i = valBeg; i < valEnd; ++i) {
            byte b = message.get(i);
            val[i-valBeg] = Character.toLowerCase((char)b);
        }

        String sKey = new String(key).trim();
        String sVal = new String(val).trim();

        headers.put(sKey, sVal);
    }
}
