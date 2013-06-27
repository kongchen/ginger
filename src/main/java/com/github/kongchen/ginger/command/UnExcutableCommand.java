package com.github.kongchen.ginger.command;

import com.github.kongchen.ginger.exception.GingerException;

/**
 * Created by chekong on 13-6-14.
 */
public abstract class UnExcutableCommand implements Command {
    @Override
    public void execute() throws GingerException {

    }
}
