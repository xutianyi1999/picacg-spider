package club.koumakan.spider.api.service;

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

  void login(String email, String password, Handler<AsyncResult<JsonObject>> handler);
}
