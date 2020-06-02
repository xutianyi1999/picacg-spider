package club.koumakan.spider;

import club.koumakan.spider.api.PicHttpHeaderUtil;
import club.koumakan.spider.api.service.SpiderService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.ProxyOptions;
import io.vertx.core.net.ProxyType;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

import java.util.function.Consumer;
import java.util.function.Function;

public class MainVerticle extends AbstractVerticle {

  @Override
  public void start(Promise<Void> startPromise) {
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

    SpiderService spiderService = new SpiderService(client, vertx.fileSystem(), "D:\\pic-test");

    Consumer<MonoSink<Void>> login = sink -> spiderService.login(email, password, r -> {
      if (r.succeeded()) {
        sink.success();
        System.out.println("login success");
      } else sink.error(r.cause());
    });

    Consumer<FluxSink<JsonObject>> getMyFavoriteBooks = sink -> {
      Consumer<Integer> getBooksByPage = YFact.yConsumer(f ->
        pageNum -> spiderService.getMyFavoriteBooks(pageNum, res -> {
          if (res.succeeded()) {
            JsonObject comics = res.result()
              .getJsonObject("data")
              .getJsonObject("comics");

            comics.getJsonArray("docs").forEach(v -> sink.next((JsonObject) v));
            Integer totalPages = comics.getInteger("pages");

            if (pageNum < totalPages) {
              f.accept(pageNum + 1);
            } else sink.complete();
          } else sink.error(res.cause());
        })
      );
      getBooksByPage.accept(51);
    };

    Function<JsonObject, Flux<Tuple3<JsonObject, Integer, JsonObject>>> getBookResult = doc -> {
      String bookId = doc.getString("_id");
      Integer epsCount = doc.getInteger("epsCount");

      // tuple <doc, epsId, pictureJson>
      Flux<Tuple3<JsonObject, Integer, JsonObject>> flux = Flux.range(1, epsCount)
        .flatMap(epsId -> Flux.create(sink -> {
          Consumer<Integer> getImagesByPage = YFact.yConsumer(f -> pageNum ->
            spiderService.getBookResult(bookId, epsId, pageNum, res -> {
              if (res.succeeded()) {
                JsonObject pagesJson = res.result()
                  .getJsonObject("data")
                  .getJsonObject("pages");

                pagesJson.getJsonArray("docs").forEach(v -> {
                  JsonObject pictureJson = (JsonObject) v;
                  sink.next(Tuples.of(doc, epsId, pictureJson));
                });

                Integer totalPages = pagesJson.getInteger("pages");

                if (pageNum < totalPages) {
                  f.accept(pageNum + 1);
                } else sink.complete();
              } else sink.error(res.cause());
            })
          );
          getImagesByPage.accept(1);
        }));

      return flux;
    };

    Function<Tuple3<JsonObject, Integer, JsonObject>, Flux<String>> downloadImg = tuple -> {
      JsonObject bookDoc = tuple.getT1();
      String bookTitle = bookDoc.getString("title");

      JsonObject imgInfo = tuple.getT3();

      JsonObject media = imgInfo.getJsonObject("media");
      String originalName = media.getString("originalName");
      String ImgPath = media.getString("path");
      String fileServer = media.getString("fileServer").replace("https://", "");

      Flux<String> flux = Flux.create(sink -> spiderService.downloadImg(fileServer, ImgPath, bookTitle, tuple.getT2(), originalName,
        res -> {
          if (res.succeeded()) {
            if (res.result()) {
              sink.next(bookTitle + "-" + tuple.getT2() + "-" + originalName).complete();
            } else sink.complete();
          } else sink.error(res.cause());
        }
      ));
      return flux;
    };

    Mono.create(login)
      .thenMany(Flux.create(getMyFavoriteBooks))
      .flatMap(getBookResult)
      .flatMap(downloadImg)
      .doOnNext(filename -> System.out.println("download: " + filename))
      .count()
      .subscribe(count -> {
        System.out.println("download " + count + "img");
        System.out.println("done");
      }, Throwable::printStackTrace);
  }
}
