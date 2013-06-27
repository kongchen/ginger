package com.github.kongchen.ginger.command.impl;

import java.util.List;

import com.github.kongchen.ginger.command.Argument;
import com.github.kongchen.ginger.command.ExecutableCommand;
import com.github.kongchen.ginger.exception.GingerException;
import com.github.kongchen.ginger.sequence.SequenceContext;

/**
 * Created by chekong on 13-6-14.
 */
public class SampleCommand extends ExecutableCommand {

    public SampleCommand(SequenceContext context, String left, int expCode, List<Argument> arguments) {
        super(context, left, expCode, arguments);
    }

    @Override
    public void execute() throws GingerException {
        handleAssignment(left, args, true, description, expectCode);
    }
}
