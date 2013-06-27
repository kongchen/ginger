package com.github.kongchen.ginger;

import java.util.regex.Pattern;

/**
 * Created by chekong on 13-6-27.
 */
public interface GingerConstants {
    String API_DOCS_JSON = "api-docs.json";
    String SEQUENCEFILE = "sample.seq";
    char OP_STRING_EMBRACE = '"';
    char OP_ESCAPE = '\\';
    char OP_VAR_PREFIX = '$';
    char OP_ACCESS_HEADER = ':';
    char OP_ACCESS_BODY = '.';
    String OP_COMMENT = "//";
    String OP_SAMPLE = "<\\d*<";
    String OP_SNAPSHOT = ">\\d*>";
    String OP_REQUEST = "=\\d*=";
    String RSP_HEADER_PREFIX = "< ";
    String REQ_HEADER_PREFIX = "> ";
}
