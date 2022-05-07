package io.github.dephin.mdp;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ServerHandshake;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;

public class WebSocketEndpoint extends WebSocketClient {
    private MDPClient mdpClient;

    public WebSocketEndpoint(URI serverUri, Draft protocolDraft,
                             Map<String, String> httpHeaders,
                             int connectTimeout, MDPClient mdpClient) {
        super(serverUri, protocolDraft, httpHeaders, connectTimeout);
        this.mdpClient = mdpClient;

        if (serverUri.toString().startsWith("wss")) {
            this.initSSL();
        }
    }

    private void initSSL() {
        try {
            TrustManager[] tm = {new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

                }

                @Override
                public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            }};
            SSLContext sslContext = SSLContext.getInstance("SSL", "SunJSSE");
            sslContext.init(null, tm, new java.security.SecureRandom());
            SSLSocketFactory ssf = sslContext.getSocketFactory();
            this.setSocket(ssf.createSocket());
        } catch (NoSuchAlgorithmException
                | NoSuchProviderException
                | KeyManagementException
                | IOException e
        ) {
            e.printStackTrace();
        }
    }


    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        this.mdpClient.onOpen();
    }

    @Override
    public void onMessage(String s) {
        this.mdpClient.onMessage(s);
    }

    @Override
    public void onClose(int i, String s, boolean b) {
        this.mdpClient.onClose(i, s, b);
    }

    @Override
    public void onError(Exception e) {
        this.mdpClient.onError(e);
    }
}
