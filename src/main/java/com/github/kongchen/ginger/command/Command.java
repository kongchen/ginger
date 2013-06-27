package com.github.kongchen.ginger.command;

import com.github.kongchen.ginger.exception.GingerException;

public interface Command {

    /**
     * this buffer buffers comments,
     * use this method to consume the buffer or clear the buffer which depends on the command's requirement.
     *
     * @param buffer
     */
    public abstract void consumeCommentBuffer(StringBuffer buffer);

    public abstract void execute() throws GingerException;
}
