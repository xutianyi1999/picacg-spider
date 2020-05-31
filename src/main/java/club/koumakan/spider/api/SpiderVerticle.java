package club.koumakan.spider.api;

import club.koumakan.spider.api.service.SpiderService;
import club.koumakan.spider.api.service.SpiderServiceImpl;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.net.ProxyOptions;
import io.vertx.core.net.ProxyType;
import io.vertx.serviceproxy.ServiceBinder;

import java.util.Optional;

public class SpiderVerticle extends AbstractVerticle {

  @Override
  public void start(Promise<Void> startPromise) {
    ProxyOptions proxyOptions = new ProxyOptions()
      .setType(ProxyType.SOCKS5)
      .setHost("192.168.199.210")
      .setPort(19999);

    new ServiceBinder(vertx)
      .setAddress(SpiderService.ebAddress)
      .register(SpiderService.class, new SpiderServiceImpl(vertx, Optional.of(proxyOptions)));

    startPromise.complete();
  }
}
