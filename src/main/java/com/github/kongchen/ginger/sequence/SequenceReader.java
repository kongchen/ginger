package com.github.kongchen.ginger.sequence;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import com.github.kongchen.ginger.command.Command;
import com.github.kongchen.ginger.command.CommandParser;
import com.github.kongchen.ginger.command.impl.NoopCommand;
import com.github.kongchen.ginger.exception.GingerException;

/**
 * Created by chekong on 13-6-14.
 */
public class SequenceReader {
    private final File sampleSeqFile;

    private BufferedReader bufferedReader;

    private final CommandParser commandParser;

    private int lineCount = 0;

    private boolean isEOF = false;

    public SequenceReader(SequenceContext context) throws GingerException {
        this.sampleSeqFile = context.getSampleFile();
        commandParser = new CommandParser(context);
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(
                    new FileInputStream(sampleSeqFile), Charset.forName("UTF-8")));
        } catch (FileNotFoundException e) {
            throw new GingerException(e);
        }
        lineCount = 0;
    }

    public Command readOneCommand() throws GingerException {
        try {
            ++lineCount;
            String line = bufferedReader.readLine();
            if (line == null) {
                isEOF = true;
                return new NoopCommand();
            }
            return commandParser.parse(line);
        } catch (IOException e) {
            throw new GingerException(e);
        } catch (GingerException e) {
            e.printStackTrace();
            throw new GingerException(e.getMessage() + " at line" + lineCount
                    + " in file [" + sampleSeqFile + "]");
        }
    }

    public boolean isEOF() {
        return isEOF;
    }

    public int getLineCount() {
        return lineCount;
    }

    public String getFile() {
        return sampleSeqFile.getPath();
    }
}
