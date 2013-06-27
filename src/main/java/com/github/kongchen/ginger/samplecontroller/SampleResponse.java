package com.github.kongchen.ginger.samplecontroller;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.kongchen.ginger.GingerConstants;
import com.github.kongchen.ginger.exception.GingerException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * Created with IntelliJ IDEA.
 * User: kongchen
 * Date: 6/5/13
 */
public class SampleResponse {

    public static final String ARRAY_ACCESS_REGEX = "^(\\$|\\w+)\\[(\\d+)]$";

    private static final Pattern ARRAY_ACCESS_PATTERN = Pattern.compile(ARRAY_ACCESS_REGEX);

    private String content = "";

    private final SampleRequest request;

    private HttpResponse response;

    private Map<String, String> headers = new HashMap<String, String>();

    private String body;

    private int code;

    public SampleResponse(SampleRequest request, HttpResponse response) throws GingerException {
        this.request = request;
        this.response = response;
        handle(response);
    }

    public SampleRequest getRequest() {
        return request;
    }

    public int getCode() {
        return code;
    }

    private void handle(HttpResponse response) throws GingerException {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            OutputStreamWriter writer = new OutputStreamWriter(outputStream, Charset.forName("UTF-8"));
            this.code = response.getStatusLine().getStatusCode();

            writer.write(response.getStatusLine().toString() + "\n");
            for (Header h : response.getAllHeaders()) {
                headers.put(h.getName(), h.getValue());
                writer.write(GingerConstants.RSP_HEADER_PREFIX + h.getName() + ": " + h.getValue() + "\n");
            }
            if (response.getEntity() != null) {
                try {
                    this.body = EntityUtils.toString(response.getEntity());
                } catch (IOException e) {
                    throw new GingerException(e);
                }
                if (body != null && body.trim().length() != 0) {
                    JsonParser parser = new JsonParser();
                    Gson gson = new GsonBuilder().setPrettyPrinting().create();
                    JsonElement el = parser.parse(body);
                    writer.write(gson.toJson(el));
                }
            }
            writer.close();
            content = new String(outputStream.toByteArray(), "UTF-8");
        } catch (FileNotFoundException e) {
            throw new GingerException(e);
        } catch (IOException e) {
            throw new GingerException(e);
        }
    }

    public void toOutput(File outputFile) throws GingerException {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
            OutputStreamWriter writer = new OutputStreamWriter(fileOutputStream, Charset.forName("UTF-8"));
            writer.write(getContext());

            writer.close();
        } catch (FileNotFoundException e) {
            throw new GingerException(e);
        } catch (IOException e) {
            throw new GingerException(e);
        }
    }

    @Override
    public String toString() {
        return content;
    }

    public String getContext() {
        return request.toString() + toString();
    }

    public String getHeader(String headerKey) {
        return headers.get(headerKey);
    }

    public String getBodyValue(String chain) throws GingerException {
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode tree = mapper.readTree(body);
            for (String key : chain.split("\\.")) {
                if (tree == null) {
                    throw new GingerException(String.format("get %s from body failed[%s].", chain, body));
                }
                Matcher m = ARRAY_ACCESS_PATTERN.matcher(key);
                if (m.matches()) {
                    String prop = m.group(1);
                    if (!prop.equals("$")) {
                        tree = tree.get(prop);
                    }
                    int idx = Integer.parseInt(m.group(2));
                    tree = tree.get(idx);
                } else {
                    if (!key.equals("$")) {
                        tree = tree.get(key);
                    }
                }
            }
            if (tree == null) {
                throw new GingerException(String.format("get %s from body failed[%s].", chain, body));
            }
            return tree.asText();
        } catch (IOException e) {
            throw new GingerException(e);
        }
    }
}
