package com.github.kongchen.ginger.command;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import com.github.kongchen.ginger.exception.GingerException;
import com.github.kongchen.ginger.exception.SyntaxErrorException;
import com.github.kongchen.ginger.samplecontroller.SampleLauncher;
import com.github.kongchen.ginger.samplecontroller.SampleRequest;
import com.github.kongchen.ginger.samplecontroller.SampleResponse;
import com.github.kongchen.ginger.sequence.SequenceContext;

/**
 * Created by chekong on 13-6-14.
 */
public abstract class ExecutableCommand implements Command {

    protected final SequenceContext context;

    protected String left;

    protected List<Argument> args;

    protected String description;

    protected int expectCode;

    public ExecutableCommand(SequenceContext context, String left, int expCode, List<Argument> arguments) {
        this.context = context;
        this.left = left;
        this.expectCode = expCode;
        this.args = arguments;
    }

    @Override
    public void consumeCommentBuffer(StringBuffer buffer) {
        this.description = buffer.toString().trim();
        buffer.setLength(0);
    }

    protected void handleAssignment(String varName, List<Argument> args,
                                    boolean needRecord, String description,
                                    int expectStatusCode) throws GingerException {
        SampleResponse response = parseExpression(args);
        if (expectStatusCode != -1 && response.getCode() != expectStatusCode) {
            response.toOutput(new File(context.getBaseDir(), varName + ".failed"));
            throw new GingerException("expecting code is " + expectStatusCode
                    + ", actually is " + response.getCode() + ". See more in file '" + varName + ".failed'.");
        }
        response.getRequest().setDescription(description);
        context.putResponse(varName, response);

        if (needRecord) {
            context.addSample(varName);
        }
    }

    private SampleResponse parseExpression(List<Argument> args) throws GingerException {

        SampleRequest exampleReq;
        String file = args.get(0).getValue();
        File requestFile = new File(file);

        if (!requestFile.isAbsolute()) {
            requestFile = new File(context.getBaseDir(), file);
        }
        if (!requestFile.exists()) {
            throw new SyntaxErrorException("File [" + file + "]not exists");
        }
        if (args.size() > 1) {
            calculateArgValue(args.subList(1, args.size()));
            String[] actualArgs = new String[args.size() - 1];
            int i = 0;
            for (Argument arg : args.subList(1, args.size())) {
                actualArgs[i++] = arg.getValue();
            }
            exampleReq = new SampleRequest(requestFile, actualArgs);
            System.out.println("Request " + requestFile.getName() + " " + Arrays.toString(actualArgs));
        } else {
            exampleReq = new SampleRequest(requestFile);
            System.out.println("Request " + requestFile.getName());
        }

        SampleLauncher launcher = new SampleLauncher(context.getBaseUrl());

        return launcher.execute(exampleReq);
    }

    private void calculateArgValue(List<Argument> arguments) throws GingerException {
        for (Argument argument : arguments) {
            switch (argument.getType()) {

                case STRING_VALUE:
                    break;
                case HEADER_VALUE:
                    argument.setType(Argument.ArgType.STRING_VALUE);
                    argument.setValue(getHeaderValue(argument.getValue()));
                    break;
                case BODY_VALUE:
                    argument.setType(Argument.ArgType.STRING_VALUE);
                    argument.setValue(getBodyValue(argument.getValue()));
                    break;
                case UNKNOWN_VALUE:
                    throw new GingerException("error format arg:" + argument.getValue());
            }
        }
    }

    private String getBodyValue(String arg) throws GingerException {
        int idx = arg.indexOf(".");
        int idx1 = arg.indexOf("[");

        if (idx < 0 || idx >= arg.length() - 1) {
            throw new GingerException("error body arg:" + arg);
        }
        if (idx1 > 0 && idx1 < idx) {
            idx = idx1;
        }
        SampleResponse response = context.getResponse(arg.substring(0, idx));
        if (response == null) {
            throw new GingerException("Cannot get value of " + arg + ", "
                    + arg.substring(0, idx) + " is not defined");
        }
        return response.getBodyValue("$" + arg.substring(idx));
    }

    private String getHeaderValue(String arg) throws GingerException {
        String[] headerInVar = arg.split(":");
        if (headerInVar.length != 2) {
            throw new GingerException("error header arg:" + arg);
        }
        String varName = headerInVar[0];
        String headerKey = headerInVar[1];
        SampleResponse response = context.getResponse(varName);
        if (response == null) {
            throw new GingerException("error varName:" + varName);
        }
        return response.getHeader(headerKey);
    }
}
