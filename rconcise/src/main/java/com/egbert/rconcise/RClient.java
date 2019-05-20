package com.egbert.rconcise;

import com.egbert.rconcise.interceptor.Interceptor;
import com.egbert.rconcise.internal.Utils;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

/**
 * 网络请求客户端，用于配置网络请求的公用{@code baseUrl}和添加拦截器（可用于拦截请求，对请求和响应进行附加操作）.
 * <p>
 * 如果app内有多个{@code baseUrl}（比如会调用不同的web服务端提供的接口），可以创建多个{@code RClient}实例，
 * 通过{@code Request}(参见{@link com.egbert.rconcise.internal.http.Request Request})在请求时指定具体使用的
 * {@code RClient}，适配多{@code baseUrl}的场景.
 * <p><br>
 * Created by Egbert on 3/5/2019.
 */
public class RClient {
    /**
     * baseUrl 为请求地址的公共前半部分，后边追加具体接口的路径，只需在app中设置一次，必须以'/'结尾；
     */
    private String baseUrl;

    /**
     * 是否是自签名或非权威机构签名的证书 true 是，false 否,
     */
    private boolean isSelfCert;

    private SSLSocketFactory sslSocketFactory;

    /**
     * 客户端配置的拦截器
     */
    private ArrayList<Interceptor> interceptors;

    /**
     * @param baseUrl 设置baseUrl
     */
    public void setBaseUrl(String baseUrl) {
        if (Utils.verifyUrl(baseUrl, true)) {
            this.baseUrl = baseUrl;
        } else {
            throw new IllegalArgumentException("The BaseUrl is illegal.");
        }
    }

    public String getBaseUrl() {
        return this.baseUrl;
    }

    public boolean isSelfCert() {
        return isSelfCert;
    }

    /**
     * @param selfCert true 为非权威CA机构签名证书（未知CA机构或自签名证书），默认为false
     * @param certFileIs 如果{@code selfCert}为true, 证书文件输入流{@code certFileIs}不能为null
     */
    public void setSelfCert(boolean selfCert, InputStream certFileIs) {
        isSelfCert = selfCert;
        if (selfCert) {
            if (certFileIs != null) {
                createSSLFactory(certFileIs);
            } else {
                throw new IllegalArgumentException("The param certFileIs cannot be null.");
            }
        }
    }

    public SSLSocketFactory getSSlSocketFactory() {
        return sslSocketFactory;
    }

    private void createSSLFactory(InputStream certIs) {
        Certificate ca;
        try {
            // Load CAs from an InputStream
            // (could be from a resource or ByteArrayInputStream or ...)
            CertificateFactory cf = CertificateFactory.getInstance("X.509", "BC");
            ca = cf.generateCertificate(certIs);

            // Create a KeyStore containing our trusted CAs
            String keyStoreType = KeyStore.getDefaultType();
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", ca);
            // Create a TrustManager that trusts the CAs in our KeyStore
            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);

            //Create an SSLContext that uses our TrustManager
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, tmf.getTrustManagers(), null);
            sslSocketFactory = sslContext.getSocketFactory();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (certIs != null) {
                    certIs.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @param interceptor 添加拦截器
     */
    public void addInterceptor(Interceptor interceptor) {
        if (interceptor != null) {
            if (interceptors == null) {
                interceptors = new ArrayList<>();
            }
            interceptors.add(interceptor);
        }
    }

    public ArrayList<Interceptor> getInterceptors() {
        return interceptors;
    }
}
