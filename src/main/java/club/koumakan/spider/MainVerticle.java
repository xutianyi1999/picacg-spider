package club.koumakan.spider;

import club.koumakan.spider.api.verticle.UserVerticle;
import club.koumakan.spider.api.verticle.service.UserService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;

public class MainVerticle extends AbstractVerticle {

  @Override
  public void start(Promise<Void> startPromise) {
    UserService userService = UserService.createProxy(vertx, UserService.ebAddress);

    Handler<Promise<Void>> deploy = promise -> vertx.deployVerticle(new UserVerticle(), res -> {
      if (res.succeeded()) promise.complete();
      else promise.fail(res.cause());
    });

    Handler<Promise<String>> login = promise -> userService.login("xutianyi1999", "QQ85100636", r -> {
      if (r.succeeded()) {
        String token = r.result()
          .getJsonObject("data")
          .getString("token");

        promise.complete(token);
      } else promise.fail(r.cause());
    });

    Future
      .future(deploy)
      .compose(none -> Future.future(login))
      .onSuccess(token -> System.out.println("token: " + token))
      .onFailure(Throwable::printStackTrace);
  }
}
