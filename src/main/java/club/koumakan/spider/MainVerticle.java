package club.koumakan.spider;

import club.koumakan.spider.api.UserVerticle;
import club.koumakan.spider.api.service.UserService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;

public class MainVerticle extends AbstractVerticle {

  @Override
  public void start(Promise<Void> startPromise) {

    vertx.deployVerticle(new UserVerticle(), res -> {
      if (res.succeeded()) {
        UserService proxy = UserService.createProxy(vertx, UserService.ebAddress);
        proxy.login("xutianyi1999", "QQ8510063", r -> {
          if (r.succeeded()) {
            System.out.println(r.result().encodePrettily());
          } else {
            r.cause().printStackTrace();
          }
        });
      } else {
        startPromise.fail(res.cause());
      }
    });
  }
}
