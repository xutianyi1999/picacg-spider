package club.koumakan.spider.api.service;

import club.koumakan.spider.api.HttpCallbackCommons;
import club.koumakan.spider.api.PicHttpHeaderUtil;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.codec.BodyCodec;

public class SpiderService {

  private final WebClient client;
  private final PicHttpHeaderUtil picHttpHeaderUtil = new PicHttpHeaderUtil();

  public SpiderService(WebClient client) {
    this.client = client;
  }

  public void login(String email, String password, Handler<AsyncResult<Void>> handler) {
    String url = "/auth/sign-in";

    JsonObject data = new JsonObject()
      .put("email", email)
      .put("password", password);

    Handler<AsyncResult<JsonObject>> f1 = r -> {
      if (r.succeeded()) {
        String token = r.result()
          .getJsonObject("data")
          .getString("token");

        picHttpHeaderUtil.setToken(token);
        handler.handle(Future.succeededFuture());
      } else handler.handle(Future.failedFuture(r.cause()));
    };

    client.post(443, PicHttpHeaderUtil.HOST, url)
      .as(BodyCodec.jsonObject())
      .putHeaders(picHttpHeaderUtil.getHttpHeaders(url, HttpMethod.POST))
      .sendJsonObject(data, HttpCallbackCommons.standardHttpCallback(f1));
  }

  public void getMyFavoriteBooks(int pageNum, Handler<AsyncResult<JsonObject>> handler) {
    String url = String.format("/users/favourite?page=%d", pageNum);

    client.get(443, PicHttpHeaderUtil.HOST, url)
      .as(BodyCodec.jsonObject())
      .putHeaders(picHttpHeaderUtil.getHttpHeaders(url, HttpMethod.GET))
      .send(HttpCallbackCommons.standardHttpCallback(handler));
  }
}
