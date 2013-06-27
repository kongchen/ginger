package com.github.kongchen.ginger.samplecontroller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;

import com.github.kongchen.ginger.GingerConstants;
import com.github.kongchen.ginger.exception.GingerException;

/**
 * Created with IntelliJ IDEA.
 * User: kongchen
 * Date: 6/5/13
 */
public class SampleRequest {

    HttpRequestBase request = null;

    private String content = "";

    private GeneratorState state;

    private File file;

    private String path;

    private String description = "";

    public SampleRequest(File reqFile, String... args) throws GingerException {
        this.file = reqFile;
        parseFileToExampleRequest(args);
    }

    private static String replacePlaceHolderWithActualValue(String oriStr, String[] values) {
        String finalS = "";
        String[] abs = oriStr.split("\\{\\{\\d+}}");

        int pos = 0;
        for (String s : abs) {
            finalS += s;
            int start = pos + s.length();
            pos = getPlcaeHolderEndPositionFrom(oriStr, start);
            String placeHolder = oriStr.substring(start, pos);
            finalS += getPlaceHolderValue(placeHolder, values);
        }
        while (pos < oriStr.length()) {
            int start = pos;
            pos = getPlcaeHolderEndPositionFrom(oriStr, start);
            String placeHolder = oriStr.substring(start, pos);
            finalS += getPlaceHolderValue(placeHolder, values);
        }
        return finalS;
    }

    private static int getPlcaeHolderEndPositionFrom(String oriStr, int startOffset) {
        int endoff = startOffset;
        for (int i = startOffset; i < oriStr.length(); i++) {
            if (oriStr.charAt(i) == '}' && oriStr.charAt(i + 1) == '}') {
                endoff += 2;
                break;
            }
            endoff++;
        }

        return endoff;
    }

    /**
     * use actual value to replace place holder
     *
     * @param placeHolder {{\d+}}
     * @param values      args value pool
     * @return "" if the place holder point to an invalid value
     */
    public static String getPlaceHolderValue(String placeHolder, String[] values) {
        Matcher matcher = Pattern.compile("\\{\\{(\\d+)}}").matcher(placeHolder);
        if (matcher.matches()) {
            int idx = Integer.parseInt(matcher.group(1));
            if (idx >= values.length) {
                return "";
            }
            return values[idx];
        } else {
            return "";
        }
    }

    public HttpRequestBase getRequest() {
        return request;
    }

    public void setRequest(HttpRequestBase request) {
        this.request = request;
    }

    public GeneratorState getState() {
        return state;
    }

    public void setState(GeneratorState state) {
        this.state = state;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return content;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    private void parseFileToExampleRequest(String... args) throws GingerException {

        String body = "";
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(file), Charset.forName("UTF-8")));
        } catch (FileNotFoundException e) {
            throw new GingerException(e);
        }
        String line = null;
        try {
            line = bufferedReader.readLine();
        } catch (IOException e) {
            throw new GingerException(e);
        }

        state = GeneratorState.description;
        int linecount = 1;

        boolean escape = false;
        try {

            while (line != null && !escape) {
                line = replacePlaceHolderWithActualValue(line, args);

                switch (state) {
                    case description:
                        if (line.startsWith("//")) {
                            description += (line.substring(1) + "\n").trim();
                            break;
                        } else {
                            state = GeneratorState.method;
                        }

                    case method:
                        int endIdx = line.indexOf(" ");

                        String method = (line.substring(0, endIdx).trim());

                        request = getHttpRequestByMethod(method);
                        path = line.substring(endIdx + 1);

                        state = GeneratorState.header;
                        break;
                    case header:
                        if (!line.startsWith(GingerConstants.REQ_HEADER_PREFIX)) {
                            if (request instanceof HttpEntityEnclosingRequestBase) {
                                state = GeneratorState.body;
                                continue;
                            } else {
                                escape = true;
                                break;
                            }
                        }
                        int startIdx = GingerConstants.REQ_HEADER_PREFIX.length();
                        String headerExp = line.substring(startIdx);
                        int idx = headerExp.indexOf(":");
                        if (idx < 0) {
                            throw new GingerException("error format header:" + line);
                        }
                        request.setHeader(headerExp.substring(0, idx), headerExp.substring(idx + 1));
                        break;
                    case body:
                        body += line;
                        break;
                }
                try {
                    if (state != GeneratorState.description) {
                        content += line;
                        content += '\n';
                    }
                    line = bufferedReader.readLine();
                } catch (IOException e) {
                    throw new GingerException(e);
                }
                linecount++;
            }
        } catch (GingerException e) {
            throw new GingerException(e.getMessage() + " at line " + linecount + " in request file: " +
                    this.file);
        }
        if (body.length() != 0 && request instanceof HttpEntityEnclosingRequestBase) {
            ((HttpEntityEnclosingRequestBase) request).setEntity(new StringEntity(body, Charset.forName("UTF-8")));
        }
        if (request == null) {
            throw new GingerException("Failed to load request file " + file.getAbsolutePath());
        }
    }

    private HttpRequestBase getHttpRequestByMethod(String method) throws GingerException {
        HttpRequestBase request;
        if (method.equalsIgnoreCase("post")) {
            request = new HttpPost();
        } else if (method.equalsIgnoreCase("get")) {
            request = new HttpGet();
        } else if (method.equalsIgnoreCase("patch")) {
            request = new HttpPatch();
        } else if (method.equalsIgnoreCase("delete")) {
            request = new HttpDelete();
        } else if (method.equalsIgnoreCase("put")) {
            request = new HttpPut();
        } else if (method.equalsIgnoreCase("options")) {
            request = new HttpOptions();
        } else {
            throw new GingerException("unknown method:" + method);
        }
        return request;
    }

    public String getMethod() {
        return request.getMethod();
    }

    private enum GeneratorState {description, method, header, body}
}
