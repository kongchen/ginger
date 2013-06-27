package com.github.kongchen.ginger.samplecontroller;

/**
 * Created by chekong on 13-6-6.
 */
public class SampleContent {
    private String description;

    private String requestAsString;

    private String responseAsString;

    public void setDescription(String description) {
        this.description = description;
    }

    public void setRequestAsString(String requestAsString) {
        this.requestAsString = requestAsString;
    }

    public void setResponseAsString(String responseAsString) {
        this.responseAsString = responseAsString;
    }

    public String getDescription() {
        return description;
    }

    public String getRequestAsString() {
        return requestAsString;
    }

    public String getResponseAsString() {
        return responseAsString;
    }
}
