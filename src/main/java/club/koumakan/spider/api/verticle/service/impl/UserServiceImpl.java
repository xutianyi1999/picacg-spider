package club.koumakan.spider.api.verticle.service.impl;

import club.koumakan.spider.api.HttpCallbackCommons;
import club.koumakan.spider.api.PicHttpHeaderUtil;
import club.koumakan.spider.api.verticle.service.UserService;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.codec.BodyCodec;

public class UserServiceImpl implements UserService {

  private final WebClient client;
  private final PicHttpHeaderUtil picHttpHeaderUtil = new PicHttpHeaderUtil();

  public UserServiceImpl(WebClient client) {
    this.client = client;
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
