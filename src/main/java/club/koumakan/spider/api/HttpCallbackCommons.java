package club.koumakan.spider.api;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.ext.web.client.HttpResponse;

public interface HttpCallbackCommons {

  static <T> Handler<AsyncResult<HttpResponse<T>>> standardHttpCallback(
    Handler<AsyncResult<T>> handler) {

    return res -> {
      if (res.succeeded()) {
        HttpResponse<T> response = res.result();

        if (response.statusCode() != 200) {
          handler.handle(Future.failedFuture(response.statusMessage() + " -> " + response.body()));
        } else {
          handler.handle(Future.succeededFuture(response.body()));
        }
      } else {
        handler.handle(Future.failedFuture(res.cause()));
      }
    };
  }
}
