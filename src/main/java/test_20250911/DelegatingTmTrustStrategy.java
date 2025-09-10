package test_20250911;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.apache.hc.core5.ssl.TrustStrategy; // HttpClient 5（4.5系はorg.apache.http.ssl.TrustStrategy）

/**
 * システム既定 + 任意のTrustStore の X509TrustManager に委譲して
 * サーバ証明書チェーンを PKIX で検証する最小TrustStrategy。
 */
public final class DelegatingTmTrustStrategy implements TrustStrategy {

    private final X509TrustManager sysTm;   // システム既定
    private final X509TrustManager extraTm; // 追加TrustStore（任意、null可）

    private DelegatingTmTrustStrategy(X509TrustManager sysTm, X509TrustManager extraTm) {
        this.sysTm = sysTm;
        this.extraTm = extraTm;
    }

    /** 追加のTrustStore（JKS/PKCS12）を使いたい時だけ渡す。不要ならnull */
    public static DelegatingTmTrustStrategy of(KeyStore extraTrustStore) {
        return new DelegatingTmTrustStrategy(
                tmFrom(null),                                // system default anchors
                extraTrustStore != null ? tmFrom(extraTrustStore) : null
        );
    }

    @Override
    public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        try {
            sysTm.checkServerTrusted(chain, authType); // 既定の信頼ストアで検証（チェーン構築含む）
            return true;
        } catch (CertificateException e) {
            if (extraTm != null) {
                // 社内CAなど追加TrustStoreでもう一度検証
                extraTm.checkServerTrusted(chain, authType);
                return true;
            }
            throw e;
        }
    }

    // ---- helpers ----
    private static X509TrustManager tmFrom(KeyStore ks) {
        try {
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(
                    TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(ks); // null=システム既定
            return (X509TrustManager) java.util.Arrays.stream(tmf.getTrustManagers())
                    .filter(tm -> tm instanceof X509TrustManager)
                    .findFirst().orElseThrow(() -> new IllegalStateException("No X509TrustManager"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /** 追加 TrustStore 用ローダ（例: "JKS" / "PKCS12"） */
    public static KeyStore loadKeyStore(String type, Path path, char[] password) {
        try (InputStream in = Files.newInputStream(path)) {
            KeyStore ks = KeyStore.getInstance(type);
            ks.load(in, password);
            return ks;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
