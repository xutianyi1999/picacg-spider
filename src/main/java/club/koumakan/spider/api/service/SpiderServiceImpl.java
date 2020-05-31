package club.koumakan.spider.api.service;

import club.koumakan.spider.api.HttpCallbackCommons;
import club.koumakan.spider.api.PicHttpHeaderUtil;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.ProxyOptions;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.ext.web.codec.BodyCodec;

import java.util.Optional;

public class SpiderServiceImpl implements SpiderService {

  private final Vertx vertx;
  private final WebClient client;
  private final PicHttpHeaderUtil picHttpHeaderUtil = new PicHttpHeaderUtil();

  public SpiderServiceImpl(Vertx vertx, Optional<ProxyOptions> proxyOptions) {
    this.vertx = vertx;

    WebClientOptions options = new WebClientOptions()
      .setUserAgent(PicHttpHeaderUtil.USER_AGENT)
      .setSsl(true)
      .setTrustAll(true);

    proxyOptions.ifPresent(options::setProxyOptions);
    this.client = WebClient.create(vertx, options);
  }

  @Override
  public void login(String email, String password, Handler<AsyncResult<JsonObject>> handler) {
    String url = "/auth/sign-in";

    JsonObject data = new JsonObject()
      .put("email", email)
      .put("password", password);

    client.post(443, PicHttpHeaderUtil.HOST, url)
      .as(BodyCodec.jsonObject())
      .putHeaders(picHttpHeaderUtil.getHttpHeaders(url, HttpMethod.POST))
      .sendJsonObject(data, HttpCallbackCommons.standardHttpCallback(handler));
  }
}
