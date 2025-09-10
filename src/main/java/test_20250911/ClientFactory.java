package test_20250911;

import java.nio.file.Paths;
import java.security.KeyStore;

import javax.net.ssl.SSLContext;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.ssl.DefaultHostnameVerifier;
import org.apache.hc.core5.ssl.SSLContextBuilder;

public class ClientFactory {
    public static CloseableHttpClient create() throws Exception {
        // 追加の社内CAがある場合だけ読み込む（無ければ extraKs = null）
        KeyStore extraKs = DelegatingTmTrustStrategy.loadKeyStore(
                "JKS", Paths.get("config/truststore.jks"), "changeit".toCharArray());

        SSLContext ssl = SSLContextBuilder.create()
                .loadTrustMaterial(null, DelegatingTmTrustStrategy.of(extraKs))
                .build();

        return HttpClients.custom()
                .setSSLContext(ssl)
                .setSSLHostnameVerifier(new DefaultHostnameVerifier()) // ★必須：ホスト名検証
                .build();
    }
}
