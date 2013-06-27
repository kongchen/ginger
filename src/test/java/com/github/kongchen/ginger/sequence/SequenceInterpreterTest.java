package com.github.kongchen.ginger.sequence;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.github.kongchen.ginger.InputConfiguration;
import com.github.kongchen.ginger.exception.GingerException;
import com.github.kongchen.swagger.docgen.LogAdapter;
import com.github.kongchen.swagger.docgen.mustache.MustacheApi;
import com.github.kongchen.swagger.docgen.mustache.MustacheDocument;
import com.github.kongchen.swagger.docgen.mustache.MustacheOperation;
import com.github.kongchen.swagger.docgen.mustache.OutputTemplate;
import com.github.kongchen.swagger.docgen.remote.RemoteDocumentSource;

/**
 * Created by chekong on 13-6-25.
 */
public class SequenceInterpreterTest {
    private static final Logger LOG = Logger.getLogger(SequenceInterpreterTest.class);

    String swaggerURL = "http://petstore.swagger.wordnik.com/api/api-docs.json?api_key=special-key";

    SequenceInterpreter interpreter;

    private SequenceContext context;

    InputConfiguration inputConfiguration = new InputConfiguration();

    @BeforeClass
    private void init() throws URISyntaxException, GingerException {

        inputConfiguration.setSwaggerBaseURL(new URI(swaggerURL));
        inputConfiguration.setApiBasePath("api");
        inputConfiguration.setSamplePackage(new File(this.getClass().getResource("/samplepack").getFile()));
        inputConfiguration.setOutputPath(new File("out"));
        inputConfiguration.setWithFormatSuffix(true);
        inputConfiguration.setOutputTemplatePath(new URI(""));
        inputConfiguration.setOutputPath(new File(""));

        context = new SequenceContext(inputConfiguration);
        interpreter = new SequenceInterpreter(context);
    }

    @Test
    public void pp() throws Exception, GingerException {
        interpreter.interpret();

        RemoteDocumentSource docSource = new RemoteDocumentSource(
                new LogAdapter(LOG), inputConfiguration.getSwaggerBaseURL(),
                inputConfiguration.getOutputTemplatePath().getPath(), inputConfiguration.getOutputPath().getPath(),
                null);
        docSource.withFormatSuffix(inputConfiguration.isWithFormatSuffix());
        docSource.loadDocuments();

        OutputTemplate outputTemplate = docSource.prepareMustacheTemplate();

        Map<String, Boolean> expectMap = new HashMap<String, Boolean>();
        expectMap.put("get one Pet", false);
        expectMap.put("Get pending pets", false);
        expectMap.put("update a pet\n" +
                "this is a test", false);
        for (MustacheDocument doc : outputTemplate.getApiDocuments()) {
            context.populateSamplesToDoc(doc);
            for (MustacheApi api : doc.getApis()) {
                for (MustacheOperation op : api.getOperations()) {
                    if (op.getHttpMethod().equals("GET") && api.getUrl().equals("http://petstore.swagger.wordnik" +
                            ".com/api/pet.{format}/{petId}")) {
                        Assert.assertEquals(1, op.getSamples().size());
                        expectMap.put(op.getSamples().get(0).getSampleDescription(), true);
                    } else if (op.getHttpMethod().equals("GET") && api.getUrl().equals("http://petstore.swagger" +
                            ".wordnik.com/api/pet.{format}/findByStatus")) {
                        Assert.assertEquals(1, op.getSamples().size());
                        expectMap.put(op.getSamples().get(0).getSampleDescription(), true);
                    } else if (op.getHttpMethod().equals("PUT") && api.getUrl().equals("http://petstore.swagger" +
                            ".wordnik.com/api/pet.{format}")) {
                        Assert.assertEquals(1, op.getSamples().size());
                        expectMap.put(op.getSamples().get(0).getSampleDescription(), true);
                    }
                }
            }
        }
        for (String key : expectMap.keySet()) {
            Assert.assertTrue(expectMap.get(key), key);
        }
    }
}
