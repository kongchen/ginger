package com.github.kongchen.ginger;

import java.io.File;
import java.net.URI;

/**
 * Created by chekong on 13-6-21.
 */
public class InputConfiguration {
    private URI swaggerBaseURL;

    private String apiBasePath;

    private URI outputTemplatePath;

    private File samplePackage;

    private File outputPath;

    private boolean withFormatSuffix;

    public String getApiBasePath() {
        return apiBasePath;
    }

    public void setApiBasePath(String apiBasePath) {
        this.apiBasePath = apiBasePath;
    }

    public URI getSwaggerBaseURL() {
        return swaggerBaseURL;
    }

    public URI getOutputTemplatePath() {
        return outputTemplatePath;
    }

    public File getSamplePackage() {
        return samplePackage;
    }

    public File getOutputPath() {
        return outputPath;
    }

    public boolean isWithFormatSuffix() {
        return withFormatSuffix;
    }

    public void setSwaggerBaseURL(URI swaggerBaseURL) {
        this.swaggerBaseURL = swaggerBaseURL;
    }

    public void setOutputTemplatePath(URI outputTemplatePath) {
        this.outputTemplatePath = outputTemplatePath;
    }

    public void setSamplePackage(File samplePackage) {
        this.samplePackage = samplePackage;
    }

    public void setOutputPath(File outputPath) {
        this.outputPath = outputPath;
    }

    public void setWithFormatSuffix(boolean withFormatSuffix) {
        this.withFormatSuffix = withFormatSuffix;
    }
}
