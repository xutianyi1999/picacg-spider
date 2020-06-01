package club.koumakan.spider.api.verticle.service.impl;

import club.koumakan.spider.api.HttpCallbackCommons;
import club.koumakan.spider.api.PicHttpHeaderUtil;
import club.koumakan.spider.api.verticle.service.SpiderService;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.codec.BodyCodec;

public class SpiderServiceImpl implements SpiderService {

  private final WebClient client;
  private final PicHttpHeaderUtil picHttpHeaderUtil;

  public SpiderServiceImpl(WebClient client, PicHttpHeaderUtil picHttpHeaderUtil) {
    this.client = client;
    this.picHttpHeaderUtil = picHttpHeaderUtil;
  }

  @Override
  public void getMyFavoriteBooks(int pageNum, Handler<AsyncResult<JsonObject>> handler) {
    String url = "/users/favourite";

    client.get(443, PicHttpHeaderUtil.HOST, url)
      .as(BodyCodec.jsonObject())
      .putHeaders(picHttpHeaderUtil.getHttpHeaders(url, HttpMethod.GET))
      .addQueryParam("s", "dd")
      .addQueryParam("page", String.valueOf(pageNum))
      .send(HttpCallbackCommons.standardHttpCallback(handler));
  }
}
