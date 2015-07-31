package org.genia.HTTPClient;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
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
        String url = "https://service2.diplo.de/rktermin/extern/appointment_showMonth.do?locationCode=kiew&realmId=357&categoryId=584&dateStr=";
        String imageUrl = "https://service2.diplo.de/rktermin/extern/captcha.jpg?locationCode=kiew";
        URI uri = new URI(url);
        URI imageUri = new URI(imageUrl);
        HttpGet httpget = new HttpGet(uri);
        HttpGet getImage = new HttpGet(imageUri);

        HttpClient httpclient = new DefaultHttpClient();

        HttpResponse response = httpclient.execute(httpget);
        // check response headers.
        String reasonPhrase = response.getStatusLine().getReasonPhrase();
        int statusCode = response.getStatusLine().getStatusCode();

        System.out.println(String.format("statusCode: %d", statusCode));
        System.out.println(String.format("reasonPhrase: %s", reasonPhrase));

        HttpResponse response2 = httpclient.execute(getImage);
        
        HttpEntity entity2 = response2.getEntity();
        InputStream is = entity2.getContent();
        String filePath = "D:\\capcha.jpg";
        
        FileOutputStream fos = new FileOutputStream(new File(filePath));
        int inByte;
        while((inByte = is.read()) != -1) 
        	fos.write(inByte);
        is.close();
        fos.close();
        
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
