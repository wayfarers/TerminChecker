package org.genia.HTTPClient;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

public class HttpClientSnippet {
    public static void main(String[] args) throws Exception {
        String url = "http://magicmonster.com";
        URI uri = new URI(url);
        HttpGet httpget = new HttpGet(uri);

        HttpClient httpclient = new DefaultHttpClient();

        HttpResponse response = httpclient.execute(httpget);
        // check response headers.
        String reasonPhrase = response.getStatusLine().getReasonPhrase();
        int statusCode = response.getStatusLine().getStatusCode();

        System.out.println(String.format("statusCode: %d", statusCode));
        System.out.println(String.format("reasonPhrase: %s", reasonPhrase));

        HttpEntity entity = response.getEntity();
        InputStream content = entity.getContent();

        ByteArrayOutputStream baos = new ByteArrayOutputStream(1024 * 1024);

        // apache IO util
        try {
            System.out.println("start download");
            IOUtils.copy(content, baos);
        } finally {
            // close http network connection
            content.close();
        }
        System.out.println("end download");
        byte[] bytes = baos.toByteArray();
        System.out.println(String.format("got %d bytes", bytes.length));
        System.out.println("HTML as string:" + new String(bytes));
    }
}
