package club.koumakan.spider.api.verticle;

import club.koumakan.spider.api.PicHttpHeaderUtil;
import club.koumakan.spider.api.verticle.service.UserService;
import club.koumakan.spider.api.verticle.service.impl.UserServiceImpl;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.net.ProxyOptions;
import io.vertx.core.net.ProxyType;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.serviceproxy.ServiceBinder;

public class UserVerticle extends AbstractVerticle {

  @Override
  public void start(Promise<Void> startPromise) {
    ProxyOptions proxyOptions = new ProxyOptions()
      .setType(ProxyType.SOCKS5)
      .setHost("192.168.199.210")
      .setPort(19999);

    WebClientOptions options = new WebClientOptions()
      .setUserAgent(PicHttpHeaderUtil.USER_AGENT)
      .setSsl(true)
      .setTrustAll(true)
      .setProxyOptions(proxyOptions);

    new ServiceBinder(vertx)
      .setAddress(UserService.ebAddress)
      .register(UserService.class, new UserServiceImpl(WebClient.create(vertx, options)));

    startPromise.complete();
  }
}
