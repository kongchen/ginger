package com.github.kongchen.ginger.command;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.github.kongchen.ginger.command.impl.Comment;
import com.github.kongchen.ginger.command.impl.NoopCommand;
import com.github.kongchen.ginger.command.impl.RequestCommand;
import com.github.kongchen.ginger.command.impl.SampleCommand;
import com.github.kongchen.ginger.command.impl.SnapshotCommand;
import com.github.kongchen.ginger.exception.GingerException;
import com.github.kongchen.ginger.sequence.SequenceContext;
import com.github.kongchen.ginger.InputConfiguration;
import junit.framework.Assert;

/**
 * Created by chekong on 13-6-14.
 */
public class CommandParserTest {
    CommandParser parser;

    private File tmpFile;

    private File emptyFile;

    String reqstr = "GET /foo/bar/api/{{0}}\n" +
            "> Host: example.com\n" +
            "> Accept: application/json\n" +
            "> Authorization: xxxxxxxxxx";

    @BeforeClass
    public void setUp() throws Exception, GingerException {
        InputConfiguration inputConfiguration = new InputConfiguration();
        inputConfiguration.setSamplePackage(new File("sample"));
        inputConfiguration.setSwaggerBaseURL(new URI("http://localhost/apd/sd/api-docs.json"));
        inputConfiguration.setApiBasePath("/apd");
        inputConfiguration.setWithFormatSuffix(true);

        SequenceContext context = new SequenceContext(inputConfiguration);
        parser = new CommandParser(context);



    }

    @DataProvider
    public Object[][] commandProvider(Method method) throws IOException {
        tmpFile = File.createTempFile("valid", ".tmp");
        FileWriter fileWriter = new FileWriter(tmpFile);
        fileWriter.write(reqstr);
        fileWriter.close();

        emptyFile = File.createTempFile("empty", ".tmp");
        return new Object[][]{
                {"abc << sdf sdf sdf", SampleCommand.class, "not exists"},
                {"abc <123< sdf sdf sdf", SampleCommand.class, "not exists"},
                {"abc >> sdf sdf sdf", SnapshotCommand.class, "Cannot find var"},
                {"abc >345> sdf sdf sdf", SnapshotCommand.class, "Cannot find var"},
                {"abc == sdf sdf sdf", RequestCommand.class, "not exists"},
                {"abc =sdf= sdf sdf sdf", RequestCommand.class, "Syntax error"},
                {"//abc == sdf sdf sdf", Comment.class, null},
                {"//xxx\n", Comment.class, null},
                {"\n", NoopCommand.class, null},
                {"abc << \"<<\" d s", SampleCommand.class, "not exists"},
                {"/", GingerException.class, null},
                {"/ /", GingerException.class, null},
                //valid seq here
                {"a << " + tmpFile.getAbsolutePath() + " foo", SampleCommand.class, "Connection"},
                {"a =34= " + emptyFile.getAbsolutePath(), RequestCommand.class, "Failed to load request file"},
                {"a < " + tmpFile.getAbsolutePath() + " foo", null, "Syntax error"},
                {"a < < " + tmpFile.getAbsolutePath() + " foo", null, "Syntax error"},
                {"a \"<<\" <<" + tmpFile.getAbsolutePath() + " foo", SampleCommand.class, "Syntax error"},
                {"a\"<<\" <<" + tmpFile.getAbsolutePath() + " foo", SampleCommand.class, "Syntax"},
                {"\"y<1< x\" <123<" + tmpFile.getAbsolutePath() + " \"foo bar\"", SampleCommand.class, "Syntax"},
        };
    }

    @Test(dataProvider = "commandProvider")
    public void testParse(String line, Class aClass, String errorMessage) {
        try {
            Command command = parser.parse(line);
            Assert.assertTrue(command.getClass() == aClass || command.getClass().getSuperclass() == aClass);
            command.execute();
        } catch (GingerException e) {
            if (errorMessage != null && errorMessage.trim().length() > 0) {
                Assert.assertTrue(e.getMessage(), e.getMessage().contains(errorMessage));
            } else {
                Assert.assertTrue(e.getClass() == aClass || e.getClass().getSuperclass() == aClass);
            }
        }
    }

    @DataProvider
    public Object[][] argsProvider(Method method) throws IOException {

        return new Object[][]{
                {"abc << def def def", new Argument[]{
                        new Argument(Argument.ArgType.STRING_VALUE, "abc"),
                        new Argument(Argument.ArgType.STRING_VALUE, "<<"),
                        new Argument(Argument.ArgType.STRING_VALUE, "def"),
                        new Argument(Argument.ArgType.STRING_VALUE, "def"),
                        new Argument(Argument.ArgType.STRING_VALUE, "def")}
                },
                {"abc << \"def\" \"$def def\"", new Argument[]{
                        new Argument(Argument.ArgType.STRING_VALUE, "abc"),
                        new Argument(Argument.ArgType.STRING_VALUE, "<<"),
                        new Argument(Argument.ArgType.STRING_VALUE, "def"),
                        new Argument(Argument.ArgType.STRING_VALUE, "$def def")}
                },
                {"abc << \"def\\\"\" \\\\def def", new Argument[]{
                        new Argument(Argument.ArgType.STRING_VALUE, "abc"),
                        new Argument(Argument.ArgType.STRING_VALUE, "<<"),
                        new Argument(Argument.ArgType.STRING_VALUE, "def\""),
                        new Argument(Argument.ArgType.STRING_VALUE, "\\\\def"),
                        new Argument(Argument.ArgType.STRING_VALUE, "def")}
                },
                {"$abc <\"< \"def \\\\def def\"", new Argument[]{
                        new Argument(Argument.ArgType.UNKNOWN_VALUE, "$abc"),
                        new Argument(Argument.ArgType.STRING_VALUE, "<\"<"),
                        new Argument(Argument.ArgType.STRING_VALUE, "def \\\\def def")}
                },
                {"abc << \"a b c d e f\" \"\" $d", new Argument[]{
                        new Argument(Argument.ArgType.STRING_VALUE, "abc"),
                        new Argument(Argument.ArgType.STRING_VALUE, "<<"),
                        new Argument(Argument.ArgType.STRING_VALUE, "a b c d e f"),
                        new Argument(Argument.ArgType.STRING_VALUE, ""),
                        new Argument(Argument.ArgType.UNKNOWN_VALUE, "$d")}
                },
                {"abc $def $def:abc $def:abc.id $def.id \"$def.id\" $def.$id \"$def:abc\"", new Argument[]{
                        new Argument(Argument.ArgType.STRING_VALUE, "abc"),
                        new Argument(Argument.ArgType.UNKNOWN_VALUE, "$def"),
                        new Argument(Argument.ArgType.HEADER_VALUE, "$def:abc"),
                        new Argument(Argument.ArgType.HEADER_VALUE, "$def:abc.id"),
                        new Argument(Argument.ArgType.BODY_VALUE, "$def.id"),
                        new Argument(Argument.ArgType.STRING_VALUE, "$def.id"),
                        new Argument(Argument.ArgType.BODY_VALUE, "$def.$id"),
                        new Argument(Argument.ArgType.STRING_VALUE, "$def:abc")}
                },
                {"\\abc \"def\" \"\\abc\" \"\\\"\"", new Argument[]{
                        new Argument(Argument.ArgType.STRING_VALUE, "\\abc"),
                        new Argument(Argument.ArgType.STRING_VALUE, "def"),
                        new Argument(Argument.ArgType.STRING_VALUE, "\\abc"),
                        new Argument(Argument.ArgType.STRING_VALUE, "\"")}
                },
                {"//\\abc \"def\" \"\\abc\" \"\\\"\"", new Argument[]{
                        new Argument(Argument.ArgType.STRING_VALUE, "//"),
                        new Argument(Argument.ArgType.STRING_VALUE, "\\abc \"def\" \"\\abc\" \"\\\"\"")}
                },
                {"//  \\abc \"def\" \"\\abc\" \"\\\"\"", new Argument[]{
                        new Argument(Argument.ArgType.STRING_VALUE, "//"),
                        new Argument(Argument.ArgType.STRING_VALUE, "  \\abc \"def\" \"\\abc\" \"\\\"\"")}
                },
                {"          ", new Argument[]{}
                },
                {"abc << \"def\" \"def def", "Uncompleted string"}
        };
    }

    @Test(dataProvider = "argsProvider")
    public void testParseArgs(String cmd, Object expect) throws Exception {
        try {
            List<Argument> res = CommandParser.parseCommandLine(cmd);
            org.testng.Assert.assertTrue(expect instanceof Argument[]);
            Argument[] result = (Argument[]) expect;
            org.testng.Assert.assertEquals(result.length, res.size());
            for (int i = 0; i < res.size(); i++) {
                org.testng.Assert.assertEquals(res.get(i).toString(), result[i].toString());
            }
        } catch (GingerException e) {
            org.testng.Assert.assertTrue(expect instanceof String, e.getMessage());
            org.testng.Assert.assertTrue(e.getMessage().contains((String) expect));
        }
    }
}
