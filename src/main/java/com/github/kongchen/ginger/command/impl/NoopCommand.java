package com.github.kongchen.ginger.command.impl;

import com.github.kongchen.ginger.command.UnExcutableCommand;

/**
 * Created by chekong on 13-6-14.
 */
public class NoopCommand extends UnExcutableCommand {

    @Override
    public void consumeCommentBuffer(StringBuffer buffer) {

    }
}
