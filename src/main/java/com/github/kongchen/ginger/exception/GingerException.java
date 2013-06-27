package com.github.kongchen.ginger.exception;

/**
 * Created with IntelliJ IDEA.
 * User: kongchen
 * Date: 6/5/13
 */
public class GingerException extends Throwable {
    public GingerException(String errorMessage) {
        super(errorMessage);
    }

    public GingerException(Throwable e) {
        super(e);
    }
}
