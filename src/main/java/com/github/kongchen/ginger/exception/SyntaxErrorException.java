package com.github.kongchen.ginger.exception;

/**
 * Created by chekong on 13-6-18.
 */
public class SyntaxErrorException extends GingerException {

    public SyntaxErrorException(String errorMessage) {
        super("Syntax error: " + errorMessage);
    }

    public SyntaxErrorException(Throwable e) {
        super(e);
    }
}
