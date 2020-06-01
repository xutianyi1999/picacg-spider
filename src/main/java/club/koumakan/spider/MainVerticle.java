package club.koumakan.spider;

import club.koumakan.spider.api.SpiderVerticle;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;

public class MainVerticle extends AbstractVerticle {

  @Override
  public void start(Promise<Void> startPromise) {
    vertx.deployVerticle(new SpiderVerticle(), res -> {
      if (res.succeeded()) startPromise.complete();
      else {
        res.cause().printStackTrace();
        vertx.close();
      }
    });
  }
}
