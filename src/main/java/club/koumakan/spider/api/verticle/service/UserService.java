package club.koumakan.spider.api.verticle.service;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

@ProxyGen
public interface UserService {

  String ebAddress = "UserService";

  static UserService createProxy(Vertx vertx, String address) {
    return new UserServiceVertxEBProxy(vertx, address);
  }

  /**
   * 登录
   *
   * @param email    邮箱/用户名
   * @param password 密码
   * @param handler  回调
   */
  void login(String email, String password, Handler<AsyncResult<JsonObject>> handler);
}
