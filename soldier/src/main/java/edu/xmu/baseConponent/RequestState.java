package edu.xmu.baseConponent;

/**
 * @Program: soldier
 * @Description: 用于保存HTTP请求过程中的状态
 *               针对于解决半包和粘包的情况
 * @Author: Ackerman
 * @Create: 2019-01-10 18:10
 */
public enum RequestState {
    /** 解析完毕 */
    PARSE_OK,

    /** 其他状态 */
    PARSE_MORE, //需要更多的数据, 半包和粘包可能会触发此状态
    PARSE_ERROR,

    /** 请求行 */
    LINE_START, // 默认初始状态
    LINE_METHOD,
    LINE_HTTP,
    LINE_HTTP_H,
    LINE_HTTP_HT,
    LINE_HTTP_HTT,
    LINE_HTTP_HTTP,
    LINE_MAJOR_DIGIT,
    LINE_MINOR_DIGIT,
    LINE_FIRST_MAJOR_DIGIT,
    LINE_FIRST_MINOR_DIGIT,
    LINE_SPACES_AFTER_DIGIT,
    LINE_SPACES_BEFORE_URI,
    LINE_AFTER_SLASH_IN_URI,
    LINE_CR,

    /** 请求头 */
    HEADER_START,
    HEADER_CR,
    HEADER_CRLF,    // cr回车 lf换行
    HEADER_CRLFCR,
    HEADER_KEY,
    HEADER_VALUE,
    HEADER_SPACES_BEFORE_COLON,
    HEADER_SPACES_AFTER_COLON,

    /** 请求体 */

    ;

}
