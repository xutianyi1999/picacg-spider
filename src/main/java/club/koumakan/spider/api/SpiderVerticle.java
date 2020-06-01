package club.koumakan.spider.api;

import club.koumakan.spider.api.service.SpiderService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.ProxyOptions;
import io.vertx.core.net.ProxyType;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;

import java.util.function.Function;

public class SpiderVerticle extends AbstractVerticle {

  @Override
  public void start() {
    WebClientOptions options = new WebClientOptions()
      .setUserAgent(PicHttpHeaderUtil.USER_AGENT)
      .setSsl(true)
      .setTrustAll(true);

    JsonObject config = vertx.fileSystem().readFileBlocking("./config.json").toJsonObject();

    JsonObject socks5 = config.getJsonObject("socks5");

    if (socks5.getBoolean("isEnable")) {
      ProxyOptions proxyOptions = new ProxyOptions()
        .setType(ProxyType.SOCKS5)
        .setHost(socks5.getString("host"))
        .setPort(socks5.getInteger("port"));

      options.setProxyOptions(proxyOptions);
    }

    WebClient client = WebClient.create(vertx, options);

    String email = config.getString("email");
    String password = config.getString("password");

    SpiderService spiderService = new SpiderService(client);

    Handler<Promise<Void>> login = promise -> spiderService.login(email, password, r -> {
      if (r.succeeded()) {
        promise.complete();
        System.out.println("login success");
      } else promise.fail(r.cause());
    });

    Function<Void, Future<Void>> getMyFavoriteBooks = none ->
      Future.future(promise -> spiderService.getMyFavoriteBooks(1, res -> {
        if (res.succeeded()) {
          System.out.println(res.result().encodePrettily());
          promise.complete();
        } else promise.fail(res.cause());
      }));

    Future
      .future(login)
      .compose(getMyFavoriteBooks)
      .onSuccess(none -> {
        System.out.println("download success");
        vertx.close();
      })
      .onFailure(cause -> {
        cause.printStackTrace();
        vertx.close();
      });
  }
}
