package com.github.kongchen.ginger.command;

import java.util.LinkedList;
import java.util.List;

import com.github.kongchen.ginger.GingerConstants;
import com.github.kongchen.ginger.command.impl.Comment;
import com.github.kongchen.ginger.command.impl.NoopCommand;
import com.github.kongchen.ginger.command.impl.RequestCommand;
import com.github.kongchen.ginger.command.impl.SampleCommand;
import com.github.kongchen.ginger.command.impl.SnapshotCommand;
import com.github.kongchen.ginger.exception.GingerException;
import com.github.kongchen.ginger.exception.SyntaxErrorException;
import com.github.kongchen.ginger.sequence.SequenceContext;

public class CommandParser {

    private final SequenceContext context;

    private StringBuffer commentBuffer;

    public CommandParser(SequenceContext context) {
        this.context = context;
        commentBuffer = new StringBuffer();
    }

    public Command parse(String line) throws GingerException {
        Command command = createCommand(line);
        command.consumeCommentBuffer(commentBuffer);
        return command;
    }

    private Command createCommand(String line) throws GingerException {
        List<Argument> args = parseCommandLine(line);
        return buildCommandByArgs(context, args);
    }

    private Command buildCommandByArgs(SequenceContext context, List<Argument> args) throws SyntaxErrorException {
        Command command = new NoopCommand();
        if (args.size() == 0) {
            return command;
        }

        Argument leftArg = args.get(0);
        if (leftArg.getType() != Argument.ArgType.STRING_VALUE) {
            throw new SyntaxErrorException("Left operand[" + leftArg.getValue() + "] is invalid");
        }
        String left = leftArg.getValue();

        if (left.equals(GingerConstants.OP_COMMENT)) {
            return new Comment(args.get(1).getValue());
        } else if (left.trim().length() == 0) {
            return new NoopCommand();
        }
        if (args.size() < 2) {
            throw new SyntaxErrorException(left + " is invalid");
        }

        Argument operatorArg = args.get(1);
        if (operatorArg.getType() == Argument.ArgType.STRING_VALUE) {
            String operator = operatorArg.getValue();

            if (operator.matches(GingerConstants.OP_SAMPLE)) {
                int expCode = getExpCodeInOperator(operator);
                return new SampleCommand(context, left, expCode, args.subList(2, args.size()));
            } else if (operator.matches(GingerConstants.OP_REQUEST)) {
                int expCode = getExpCodeInOperator(operator);
                return new RequestCommand(context, left, expCode, args.subList(2, args.size()));
            } else if (operator.matches(GingerConstants.OP_SNAPSHOT)) {
                int expCode = getExpCodeInOperator(operator);
                return new SnapshotCommand(context, left, expCode, args.subList(2, args.size()));
            }
        }
        throw new SyntaxErrorException("Operator[" + operatorArg.getValue() + "] is invalid");
    }
    private int getExpCodeInOperator(String op) throws SyntaxErrorException {
        int idx1 = 1;
        int idx2 = op.length() - 1;
        if (idx1 < idx2) {
            try {
                return Integer.parseInt(op.substring(idx1, idx2));
            } catch (NumberFormatException e) {
                throw new SyntaxErrorException("Expect code error in operator[" + op + "]");
            }
        } else {
            return -1;
        }
    }

    protected static List<Argument> parseCommandLine(String line) throws GingerException {
        List<Argument> args = new LinkedList<Argument>();
        StringBuilder sb = new StringBuilder();
        Argument.ArgType currentType = Argument.ArgType.STRING_VALUE;
        boolean isInString = false;
        if (line.startsWith(GingerConstants.OP_COMMENT)) {
            args.add(new Argument(Argument.ArgType.STRING_VALUE, GingerConstants.OP_COMMENT));
            args.add(new Argument(Argument.ArgType.STRING_VALUE, line.substring(2)));
            return args;
        }
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            char c1 = i + 1 < line.length() ? line.charAt(i + 1) : 0;

            if (Character.isSpaceChar(c)) {
                if (isInString) {
                    sb.append(c);
                } else if (sb.length() != 0) {

                    Argument argument = new Argument(currentType, sb.toString());
                    args.add(argument);
                    sb.setLength(0);
                    currentType = Argument.ArgType.STRING_VALUE;
                }
            } else {
                if (c == GingerConstants.OP_STRING_EMBRACE) {
                    if (sb.length() == 0 && !isInString) {
                        isInString = true;
                        currentType = Argument.ArgType.STRING_VALUE;
                    } else if (isInString && (Character.isSpaceChar(c1) || c1 == 0)) {
                        isInString = false;
                        if (currentType == Argument.ArgType.UNKNOWN_VALUE) {
                            currentType = Argument.ArgType.STRING_VALUE;
                        }
                        Argument argument = new Argument(currentType, sb.toString());
                        args.add(argument);
                        i++;
                        sb.setLength(0);
                    } else {
                        sb.append(c);
                    }
                } else if (c == GingerConstants.OP_ESCAPE) {
                    if (c1 == GingerConstants.OP_STRING_EMBRACE) {
                        sb.append(c1);
                        i++;
                    } else {
                        sb.append(c);
                    }
                } else {
                    if (sb.length() == 0) {
                        if (c == GingerConstants.OP_VAR_PREFIX && !isInString) {
                            currentType = Argument.ArgType.UNKNOWN_VALUE;
                        }
                    } else if (!isInString && c == GingerConstants.OP_ACCESS_HEADER && currentType == Argument.ArgType.UNKNOWN_VALUE) {
                        currentType = Argument.ArgType.HEADER_VALUE;
                    } else if (!isInString && c == GingerConstants.OP_ACCESS_BODY && currentType == Argument.ArgType.UNKNOWN_VALUE) {
                        currentType = Argument.ArgType.BODY_VALUE;
                    }
                    sb.append(c);
                }
            }
        }
        if (sb.length() > 0) {
            if (isInString) {
                throw new SyntaxErrorException("Uncompleted string[" + sb.toString() + "]");
            } else {
                args.add(new Argument(currentType, sb.toString()));
            }
        }
        return args;
    }
}
