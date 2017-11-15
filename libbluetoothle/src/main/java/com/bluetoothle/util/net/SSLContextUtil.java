package com.bluetoothle.util.net;

import android.annotation.SuppressLint;
import android.content.Context;

import com.yolanda.nohttp.download.DownloadRequest;
import com.yolanda.nohttp.rest.Request;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 * Created by yanfa on 2016/6/22.
 */
public class SSLContextUtil {
    private static SSLContext sslContext = null;

    /**
     * 拿到https证书, SSLContext (NoHttp已经修补了系统的SecureRandom的bug)。
     */
    @SuppressLint("TrulyRandom")
    public static SSLContext getSSLContext(Context application) {
        if (sslContext != null) {
            return sslContext;
        }
        try {
            // 加载证书流, 这里证书需要放在assets下
            InputStream inputStream = application.getAssets().open("server.cer");
            // 生成证书
            CertificateFactory cerFactory = CertificateFactory.getInstance("X.509");
            Certificate cer = cerFactory.generateCertificate(inputStream);
            // 加载证书到KeyStore
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(null, null);
            keyStore.setCertificateEntry("trust", cer);

            sslContext = SSLContext.getInstance("TLS");
            // KeyStore加入TrustManagerFactory
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);

            //初始化clientKeyStore
            KeyStore clientKeyStore = KeyStore.getInstance("BKS");
            clientKeyStore.load(application.getAssets().open("client.bks"), "clientkey@dsm2017".toCharArray());

            // 把初始化clientKeyStore放入keyManagerFactory
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(clientKeyStore, "clientkey@dsm2017".toCharArray());

            // 初始化SSLContext
            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), new SecureRandom());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return sslContext;
    }

    /**
     * 设置证书
     *
     * @param request
     * @param <T>
     * @return
     */
    public static <T> Request doHttps(Request<T> request, Context application) {
        SSLContext sslContext = getSSLContext(application);
        if (sslContext != null) {
            request.setSSLSocketFactory(sslContext.getSocketFactory());
        }
        request.setHostnameVerifier(SSLContextUtil.HOSTNAME_VERIFIER);
        return request;
    }

    /**
     * 设置证书
     * @param request
     * @param application
     * @return
     */
    public static DownloadRequest doHttps(DownloadRequest request, Context application) {
        SSLContext sslContext = getSSLContext(application);
        if (sslContext != null) {
            request.setSSLSocketFactory(sslContext.getSocketFactory());
        }
        request.setHostnameVerifier(SSLContextUtil.HOSTNAME_VERIFIER);
        return request;
    }

    /**
     * 如果不需要https证书.(NoHttp已经修补了系统的SecureRandom的bug)。
     */
    private static SSLContext getDefaultSLLContext() {
        SSLContext sslContext = null;
        try {
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{trustManagers}, new SecureRandom());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sslContext;
    }

    private static final TrustManager trustManagers = new X509TrustManager() {

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {

        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    };

    public static final HostnameVerifier HOSTNAME_VERIFIER = new HostnameVerifier() {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };
}
