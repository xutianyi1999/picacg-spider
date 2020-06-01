package club.koumakan.spider.api;

import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.impl.headers.VertxHttpHeaders;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

public class PicHttpHeaderUtil {

  public static final String HOST = "picaapi.picacomic.com";
  public static final String USER_AGENT = "okhttp/3.8.1";

  private final String secretKey = "~d}$Q7$eIni=V)9\\RK/P.RM4;9[7|@/CA}b~OW!3?EV`:<>M7pddUBL5n|0/*Cn";
  private final String apiKey = "C69BAF41DA5ABD1FFEDC6D2FEA56B";
  private final String appVersion = "2.2.1.3.3.4";
  private final String appChannel = "1";
  private final String buildVersion = "45";
  private final String accept = "application/vnd.picacomic.com.v1+json";
  private final String appPlatform = "android";
  private final String appUUID = UUID.randomUUID().toString();
  private final String quality = "original";
  private final String nonce = UUID.randomUUID().toString().replace("-", "");

  private final HMACSHA256 hmacsha256 = new HMACSHA256(secretKey.getBytes(StandardCharsets.UTF_8));

  private Optional<String> token = Optional.empty();

  public void setToken(String token) {
    this.token = Optional.ofNullable(token);
  }

  public VertxHttpHeaders getHttpHeaders(String targetURL, HttpMethod httpMethod) {
    String currentTime = String.valueOf(System.currentTimeMillis() / 1000);
    String temp = targetURL.substring(1) + currentTime + nonce + httpMethod + apiKey;
    String signature = hmacsha256.encode(temp.toLowerCase().getBytes(StandardCharsets.UTF_8));

    VertxHttpHeaders headers = new VertxHttpHeaders()
      .add("api-key", apiKey)
      .add("app-version", appVersion)
      .add("app-channel", appChannel)
      .add("app-build-version", buildVersion)
      .add("accept", accept)
      .add("app-platform", appPlatform)
      .add("app-uuid", appUUID)
      .add("Host", HOST)
      .add("image-quality", quality)
      .add("nonce", nonce)
      .add("time", currentTime)
      .add("signature", signature)
      .add(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8");

    token.ifPresent(v -> headers.add("authorization", v));
    return headers;
  }
}
