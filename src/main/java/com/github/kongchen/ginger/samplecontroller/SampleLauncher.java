package com.github.kongchen.ginger.samplecontroller;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.DefaultHttpClient;

import com.github.kongchen.ginger.exception.GingerException;

/**
 * Created with IntelliJ IDEA.
 * User: kongchen
 * Date: 6/5/13
 */
public class SampleLauncher {
    private URI httpUrl;

    private HttpClient client = new DefaultHttpClient();

    public SampleLauncher(URI httpUrl) {
        this.httpUrl = httpUrl;
    }

    public SampleResponse execute(SampleRequest exampleReq) throws GingerException {
        try {
            URIBuilder uriBuilder = new URIBuilder(httpUrl);
            String path = exampleReq.getPath();
            if (path != null) {
                int idx = path.indexOf("?");
                if (idx >= 0) {
                    String query = path.substring(idx + 1);
                    String p = path.substring(0, idx);
                    uriBuilder.setPath(p);
                    uriBuilder.setQuery(query);
                } else {
                    uriBuilder.setPath(path);
                }
            }

            exampleReq.getRequest().setURI(uriBuilder.build().normalize());
            HttpResponse response = client.execute(exampleReq.getRequest());
            return new SampleResponse(exampleReq, response);
        } catch (URISyntaxException e) {
            throw new GingerException(e);
        } catch (ClientProtocolException e) {
            throw new GingerException(e);
        } catch (IOException e) {
            throw new GingerException(e);
        }
    }
}
