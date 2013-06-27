package com.github.kongchen.ginger.command.impl;

import java.io.File;
import java.util.List;

import com.github.kongchen.ginger.command.Argument;
import com.github.kongchen.ginger.command.ExecutableCommand;
import com.github.kongchen.ginger.exception.GingerException;
import com.github.kongchen.ginger.samplecontroller.SampleResponse;
import com.github.kongchen.ginger.sequence.SequenceContext;

/**
 * Created by chekong on 13-6-14.
 */
public class SnapshotCommand extends ExecutableCommand {

    public SnapshotCommand(SequenceContext context, String left, int expCode, List<Argument> arguments) {
        super(context, left, expCode, arguments);
    }

    @Override
    public void execute() throws GingerException {
        SampleResponse response = context.getResponseMap().get(left);
        if (response == null) {
            throw new GingerException("Cannot find var: " + left);
        }
        response.toOutput(new File(context.getBaseDir(), args.get(0).getValue()));
    }
}
