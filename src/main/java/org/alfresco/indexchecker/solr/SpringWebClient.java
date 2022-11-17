package org.alfresco.indexchecker.solr;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.security.KeyStore;

/**
 * Spring Web Client includes specific configuration for all different
 * Alfresco Search Service communication mode:
 * - NONE: Plain HTTP
 * - HTTPS: mTLS
 * - SECRET: Secret in HTTP Header
 */
@Component
public class SpringWebClient
{

    public static final String HTTP_HEADER_SECRET = "X-Alfresco-Search-Secret";

    public enum CommMode
    {
        NONE, HTTPS, SECRET
    }

    @Value("${solr.comms}")
    CommMode solrComms;

    @Value("${solr.secret}")
    String solrSecret;

    @Value("${solr.mtls.keystore.path}")
    String keyStorePath;

    @Value("${solr.mtls.keystore.pass}")
    String keyStorePass;

    @Value("${solr.mtls.keystore.type}")
    String keyStoreType;

    @Value("${solr.mtls.truststore.path}")
    String trustStorePath;

    @Value("${solr.mtls.truststore.pass}")
    String trustStorePass;

    @Value("${solr.mtls.truststore.type}")
    String trustStoreType;

    /**
     * Build Spring WebClient according to configuration (NONE, HTTPS, SECRET)
     * @param baseUrl Base URL for SOLR Server
     * @return Spring WebClient with applied configuration
     */
    public WebClient getWebClient(String baseUrl) {

        HttpClient httpClient = HttpClient.create(ConnectionProvider.create("web-client"));

        if (solrComms == CommMode.SECRET) {
            httpClient = httpClient.headers(header -> {
                header.add(HTTP_HEADER_SECRET, solrSecret);
            });
        }

        if (solrComms == CommMode.HTTPS) {
            httpClient = httpClient.secure(sslContextSpec -> sslContextSpec.sslContext(getMTLSContext()));
        }

        return WebClient
                .builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .baseUrl(baseUrl)
                .build();
    }

    /**
     * Build SSLContext from keystores described in configuration
     * @return SSLContext ready to use
     */
    private SslContext getMTLSContext()
    {
        try {

            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(new FileInputStream(ResourceUtils.getFile(keyStorePath)), keyStorePass.toCharArray());

            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
            keyManagerFactory.init(keyStore, keyStorePass.toCharArray());

            KeyStore trustStore = KeyStore.getInstance(trustStoreType);
            trustStore.load(new FileInputStream(ResourceUtils.getFile(trustStorePath)), trustStorePass.toCharArray());

            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
            trustManagerFactory.init(trustStore);

            return SslContextBuilder.forClient()
                    .keyManager(keyManagerFactory)
                    .trustManager(trustManagerFactory)
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

}
