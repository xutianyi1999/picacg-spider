package club.koumakan.spider.api;

import club.koumakan.spider.YFact;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.ext.web.client.HttpResponse;

import java.io.IOException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class HttpCallback {

  private final BiConsumer<Long, Handler<Long>> timer;
  private final int retryCount = 5;
  private final long delay = 5000;

  public HttpCallback(BiConsumer<Long, Handler<Long>> timer) {
    this.timer = timer;
  }

  public <T> void httpResponseHandler(Consumer<Handler<AsyncResult<HttpResponse<T>>>> process,
                                      Consumer<T> success, Consumer<Throwable> error) {

    Consumer<Integer> c = YFact.yConsumer(f ->
      count -> process.accept(res -> {
        if (res.succeeded()) {
          HttpResponse<T> response = res.result();

          if (response.statusCode() == 200)
            success.accept(response.body());
          else
            error.accept(new IOException(response.statusMessage() + " -> " + response.body()));
        } else {
          if (count >= retryCount) error.accept(res.cause());
          else timer.accept(delay, id -> f.accept(count + 1));
        }
      })
    );
    c.accept(1);
  }
}
