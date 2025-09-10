# ファイル交換の動作確認PJ

## 🗄️PJ概要

サーバ・クライアント間のファイル交換の動作確認を、ローカルで行うための練習用PJ。
サーバは、クライアントからファイル(カンマ区切りのcsvファイル)を受け取り、ファイルに書き込みを行う。

## 🗃️手順概要

１．サーバの基本機能を持ったクラスを実装する。

２．クライアントの基本機能を持ったクラスを実装する。

３．クライアントからサーバにファイルを送れるようにする。

４．クライアントを複数立てて、接続できるような実装を作成する(シェルスクリプト？)。

５．簡単な性能テストを行う。

以下、詳細な手順は「手順詳細」で説明する。

## 📁手順詳細

### １．サーバの基本機能を持ったクラスを実装する。

ここで言うサーバの基本機能は以下の通り。

- ipとポート番号のバインド処理
- socket作成
- クライアントからの待受処理(accept)
- データの受信処理(標準出力経由)

サーバはポート単位で待受(accept)を行い、クラアントから要求が来たらsocketを作成する。
それぞれのsocketはポートの数(=10個)だけ作成される。それぞれのsocketからさらに最大接続数(=10個)分だけスレッドを作成する。この時点では、ファイルの受信処理は実装せず、標準出力からデータを受け取るよう実装する。

### ２．クライアントの基本機能を持ったクラスを実装する。

ここで言うクライアントの基本機能とは以下の通り。

- ソケットの作成
- データの送信処理(標準入力経由)

ソケットの作成処理後、ローカルのcsvファイルを読み込みサーバに送信する。この時点では、ファイルの送信処理は実装せず、標準入力経由でデータを送信するよう実装する。

### ３．クライアントからサーバにファイルを送れるようにする。

標準入力/出力でデータをやり取りしている実装について、csvファイルを送受信するように実装し直す。InputStream, OutputStreamにsocketを利用してファイルの送受信を行う。

**■サーバ側実装**

サーバ側のsocketごとに、接続待機用のスレッド(accept)を10個作成して、そのスレッド毎にファイル受信処理(handler)を持たせる。

**■クライアント側実装**

ファイル送信処理を実装する。

### ５．簡単な性能テストを行う。

<aside>
✅

性能の目安

> socket用スレッド10個 × handler用スレッド10個 = 100個のファイル受信処理
> 

クライアントから最大100個のファイル受信処理のリクエスト処理を受信・実行できるサーバを作成する。

</aside>

###




OK！.properties 派でいきましょう。
YAMLで書いていた内容をそのまま application.properties 版に置き換えます。コード側は前回の「クライアント（RestTemplate）」のままでOKです。


---

1) application.properties の例

A. 公開CAのみ（社内CAなし／最小構成）

# クライアントアプリなので自由
server.port=0

# Moodle 接続先
moodle.base-url=https://moodle.example.com
moodle.token=${MOODLE_TOKEN}   # 環境変数から注入推奨

> 公開CAだけなら SSL Bundles 設定は不要。JREの信頼ストアで証明書検証されます。



B. 社内CAを信頼させたい（サーバ証明書が私設CAで発行）

# 上の基本設定に加えて ↓ を追加
spring.ssl.bundle.pem.moodle-ca.truststore.certificate=classpath:ca/moodle-rootCA.crt

> src/main/resources/ca/moodle-rootCA.crt を置きます。
コード側では SslBundles#getBundle("moodle-ca") を使います（下のコード参照）。



C. mTLS（双方向TLS）を使う（クライアント証明書が必要）

# 上の基本設定に加えて ↓ を追加（PEM鍵が暗号化の場合は key.password も）
spring.ssl.bundle.pem.moodle-mtls.key.certificate=classpath:client.crt
spring.ssl.bundle.pem.moodle-mtls.key.private-key=classpath:client.key
spring.ssl.bundle.pem.moodle-mtls.truststore.certificate=classpath:ca/moodle-rootCA.crt
# spring.ssl.bundle.pem.moodle-mtls.key.password=changeit   # 必要なら

> コード側では SslBundles#getBundle("moodle-mtls") を使います。




---

2) クライアント側コード（RestTemplate）※そのまま使えます

// src/main/java/com/example/http/HttpClientConfig.java
package com.example.http;

import java.time.Duration;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.ssl.ClientTlsStrategyBuilder;
import org.apache.hc.client5.http.ssl.TlsSocketStrategy;
import org.apache.hc.client5.http.ssl.DefaultHostnameVerifier;
import org.apache.hc.core5.http.ssl.TLS;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.springframework.boot.ssl.SslBundle;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class HttpClientConfig {

  @Bean
  RestTemplate moodleRestTemplate(RestTemplateBuilder builder, SslBundles bundles) {
    // ▼ ここで使いたいバンドル名を切り替える：
    //   - 公開CAのみ → 定義なしなので getBundle せず null のまま
    //   - 社内CA      → "moodle-ca"
    //   - mTLS        → "moodle-mtls"
    SslBundle bundle = null;
    try {
      bundle = bundles.getBundle("moodle-ca");     // 社内CAなら "moodle-ca"
      // bundle = bundles.getBundle("moodle-mtls"); // mTLSならこちら
    } catch (Exception ignore) {}

    TlsSocketStrategy tls = ClientTlsStrategyBuilder.create()
        .setSslContext(bundle != null ? bundle.createSslContext() : null) // 公開CAのみなら null（JSSEの既定を使用）
        .setTlsVersions(TLS.V_1_3, TLS.V_1_2) // 旧版は使わない
        .setHostnameVerifier(new DefaultHostnameVerifier())
        .build();

    HttpClientConnectionManager cm = PoolingHttpClientConnectionManagerBuilder.create()
        .setTlsSocketStrategy(tls)
        .build();

    RequestConfig rc = RequestConfig.custom()
        .setConnectTimeout(Timeout.ofSeconds(5))
        .setConnectionRequestTimeout(Timeout.ofSeconds(5))
        .setResponseTimeout(Timeout.ofSeconds(30))
        .build();

    CloseableHttpClient http = HttpClients.custom()
        .setConnectionManager(cm)
        .setDefaultRequestConfig(rc)
        .evictExpiredConnections()
        .evictIdleConnections(TimeValue.ofMinutes(1))
        .build();

    return builder
        .requestFactory(() -> new HttpComponentsClientHttpRequestFactory(http))
        .setConnectTimeout(Duration.ofSeconds(5))
        .setReadTimeout(Duration.ofSeconds(30))
        .build();
  }
}

// src/main/java/com/example/moodle/MoodleRestClient.java
package com.example.moodle;

import java.util.Map;
import java.util.Objects;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

@Component
public class MoodleRestClient {
  private final RestTemplate rt;
  private final ObjectMapper om = new ObjectMapper();

  @Value("${moodle.base-url}") private String baseUrl;
  @Value("${moodle.token}")    private String token;

  public MoodleRestClient(RestTemplate moodleRestTemplate) { this.rt = moodleRestTemplate; }

  public JsonNode call(String wsFunction, Map<String, ?> params) {
    Objects.requireNonNull(baseUrl, "moodle.base-url is required");
    Objects.requireNonNull(token,   "moodle.token is required");

    String url = baseUrl + "/webservice/rest/server.php";
    MultiValueMap<String,String> form = new LinkedMultiValueMap<>();
    form.add("wstoken", token);
    form.add("wsfunction", wsFunction);
    form.add("moodlewsrestformat", "json");
    params.forEach((k,v) -> form.add(k, String.valueOf(v)));

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    try {
      ResponseEntity<String> res = rt.postForEntity(url, new HttpEntity<>(form, headers), String.class);
      if (!res.getStatusCode().is2xxSuccessful() || res.getBody() == null) {
        throw new RuntimeException("Moodle API HTTP status=" + res.getStatusCode());
      }
      JsonNode json = om.readTree(res.getBody());
      if (json.has("exception") || json.has("errorcode")) {
        throw new RuntimeException("Moodle error: " + json.toString());
      }
      return json;
    } catch (HttpStatusCodeException e) {
      throw new RuntimeException("HTTP " + e.getStatusCode() + " body=" + e.getResponseBodyAsString(), e);
    } catch (Exception e) {
      throw new RuntimeException("Moodle call failed", e);
    }
  }
}


---

使い分け早見表

公開CAのみ → application.properties に moodle. だけ書く（SSL Bundle不要）。

社内CA → spring.ssl.bundle.pem.moodle-ca.truststore.certificate=… を追加し、コードの getBundle("moodle-ca") を有効に。

mTLS → spring.ssl.bundle.pem.moodle-mtls.* を設定し、コードの getBundle("moodle-mtls") を有効に。


> いずれの場合も、証明書検証は“無効化しない” のがベストプラクティスです（TrustAllやNoopHostnameVerifierはNG）。



必要なら、.properties でプロキシ設定やリトライなども追記できます。欲しいオプションがあれば、その形で追記版をすぐ用意します。


