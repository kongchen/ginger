package com.github.kongchen.ginger.command.impl;

import com.github.kongchen.ginger.command.UnExcutableCommand;

/**
 * Created by chekong on 13-6-14.
 */
public class Comment extends UnExcutableCommand {

    private final String line;

    public Comment(String line) {
        this.line = line;
    }

    @Override
    public void consumeCommentBuffer(StringBuffer buffer) {
        buffer.append(line.trim()).append('\n');
    }
}
