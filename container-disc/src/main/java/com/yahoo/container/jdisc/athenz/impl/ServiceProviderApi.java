// Copyright 2017 Yahoo Holdings. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
package com.yahoo.container.jdisc.athenz.impl;

import com.yahoo.vespa.defaults.Defaults;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.eclipse.jetty.http.HttpStatus;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

/**
 * @author mortent
 * @author bjorncs
 */
public class ServiceProviderApi {

    private final URI identityDocumentApiUri;

    public ServiceProviderApi(String configServerHostname) {
        this.identityDocumentApiUri = createIdentityDocumentApiUri(configServerHostname);
    }

    /**
     * Get signed identity document from config server
     */
    public String getSignedIdentityDocument() {
        try (CloseableHttpClient httpClient = createHttpClient()) {
            CloseableHttpResponse idDocResponse = httpClient.execute(new HttpGet(identityDocumentApiUri));
            String responseContent = EntityUtils.toString(idDocResponse.getEntity());
            if (HttpStatus.isSuccess(idDocResponse.getStatusLine().getStatusCode())) {
                return responseContent;
            } else {
                // TODO make sure we have retried a few times (AND logged) before giving up
                throw new RuntimeException(
                        "Failed to initialize Athenz instance provider: " +
                                idDocResponse.getStatusLine() + ": " + responseContent);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed getting signed identity document", e);
        }
    }

    // TODO Use client side auth to establish trusted secure channel
    // TODO Validate TLS certifcate of config server
    private static CloseableHttpClient createHttpClient() {
        try {
            SSLContextBuilder sslContextBuilder = new SSLContextBuilder();
            sslContextBuilder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
            SSLConnectionSocketFactory sslSocketFactory =
                    new SSLConnectionSocketFactory(sslContextBuilder.build(),
                                                   SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            return HttpClientBuilder.create().setSSLSocketFactory(sslSocketFactory).build();
        } catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException e) {
            throw new RuntimeException(e);
        }
    }

    private static URI createIdentityDocumentApiUri(String providerHostname) {
        try {
            // TODO Figure out a proper way of determining the hostname matching what's registred in node-repository
            return new URIBuilder()
                    .setScheme("https")
                    .setHost(providerHostname)
                    .setPort(8443)
                    .setPath("/athenz/v1/provider/identity-document")
                    .addParameter("hostname", Defaults.getDefaults().vespaHostname())
                    .build();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

}
