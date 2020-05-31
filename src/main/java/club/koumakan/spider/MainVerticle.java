package club.koumakan.spider;

import club.koumakan.spider.api.SpiderVerticle;
import club.koumakan.spider.api.service.SpiderService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;

public class MainVerticle extends AbstractVerticle {

  @Override
  public void start(Promise<Void> startPromise) {

    vertx.deployVerticle(new SpiderVerticle(), res -> {
      if (res.succeeded()) {
        SpiderService proxy = SpiderService.createProxy(vertx, SpiderService.ebAddress);
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
