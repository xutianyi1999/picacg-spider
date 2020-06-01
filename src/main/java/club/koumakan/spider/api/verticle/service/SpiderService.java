package club.koumakan.spider.api.verticle.service;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

@ProxyGen
public interface SpiderService {

  String ebAddress = "SpiderService";

  static SpiderService createProxy(Vertx vertx, String address) {
    return new SpiderServiceVertxEBProxy(vertx, address);
  }

  /**
   * 获取收藏夹
   *
   * @param pageNum 页数
   */
  void getMyFavoriteBooks(int pageNum, Handler<AsyncResult<JsonObject>> handler);
}
