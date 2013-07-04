package com.github.kongchen.ginger.sequence;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.kongchen.ginger.InputConfiguration;
import com.github.kongchen.ginger.exception.GingerException;
import com.github.kongchen.swagger.docgen.GenerateException;
import com.github.kongchen.swagger.docgen.LogAdapter;
import com.github.kongchen.swagger.docgen.mustache.MustacheDocument;
import com.github.kongchen.swagger.docgen.mustache.OutputTemplate;
import com.github.kongchen.swagger.docgen.remote.RemoteDocumentSource;

/**
 * Created by chekong on 13-6-6.
 */
public class SequenceInterpreter {
    private static final Logger LOG = Logger.getLogger(SequenceInterpreter.class);

    private final SequenceReader commandReader;

    public SequenceInterpreter(SequenceContext context) throws GingerException {
        this.commandReader = new SequenceReader(context);
    }

    public void interpret() throws GingerException {
        while (!commandReader.isEOF()) {
            try {
                commandReader.readOneCommand().execute();
            } catch (GingerException e) {
                throw new GingerException(e + " at line:" + commandReader.getLineCount() + " of file[" +
                        commandReader.getFile() + "]");
            }
        }
    }

    public static void main(String args[]) throws GingerException, Exception, GenerateException {
        if (args.length != 1 || !new File(args[0]).exists()) {
            System.err.println("Configuration file not found!");
            printUsage();
            return;
        }
        ObjectMapper objectMapper = new ObjectMapper();
        InputConfiguration input = null;
        try {
            input = objectMapper.readValue(new File(args[0]), InputConfiguration.class);
            if (input.getApiBasePath() == null || input.getOutputPath() == null || input.getOutputTemplatePath() == null
                    || input.getSwaggerBaseURL() == null) {
                throw new IOException("");
            }
        } catch (IOException e) {
            System.err.println("Bad format of configuration file!");
            printUsage();
            return;
        }
        BasicConfigurator.configure();
        RemoteDocumentSource docSource = new RemoteDocumentSource(
                new LogAdapter(LOG), input.getSwaggerBaseURL(),
                input.getOutputTemplatePath().toString(), input.getOutputPath().getPath(), null);
        docSource.withFormatSuffix(input.isWithFormatSuffix());
        docSource.loadDocuments();



        if (input.getSamplePackage() != null) {
            OutputTemplate outputTemplate = docSource.prepareMustacheTemplate();
            SequenceContext context = new SequenceContext(input);
            SequenceInterpreter interpreter = new SequenceInterpreter(context);
            interpreter.interpret();



            Set<String> usedSamples = new HashSet<String>();
            for (MustacheDocument doc : outputTemplate.getApiDocuments()) {
                usedSamples.addAll(context.populateSamplesToDoc(doc));
            }
            LOG.info(usedSamples.size() + "/" + context.getSamples().size() + " samples are populated in document.");
            for (String s : context.getSamples()) {
                if (!usedSamples.contains(s)) {
                    LOG.info("Sample [" + s + "] is not used.");
                }
            }

        }
        docSource.toDocuments();
        docSource.toSwaggerDocuments();
    }

    private static void printUsage() {
        System.out.println("Usage: command <cfg-file-path>");
        System.out.println("The <cfg-file> is a JSON file and its format is:\n" +
                "{\n" +
                "   \"sampleBaseURL\":\"http://www.example.com:8080/your/api/path\",\n" +
                "   \"swaggerBaseURL\":\"http://www.example.com:8080/your/api/path/api-docs.json\",\n" +
                "   \"samplePackage\":\"samples\",\n" +
                "   \"outputTemplatePath\":\"template/strapdown.html.mustache\",\n" +
                "   \"outputPath\":\"target/doc.html\"\n" +
                "}\n\n" +
                "samplePackage can omit.");
    }
}

