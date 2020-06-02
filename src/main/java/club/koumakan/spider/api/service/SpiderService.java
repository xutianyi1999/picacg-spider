package club.koumakan.spider.api.service;

import club.koumakan.spider.api.HttpCallbackCommons;
import club.koumakan.spider.api.PicHttpHeaderUtil;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.file.FileSystem;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.codec.BodyCodec;

import java.nio.file.Paths;

public class SpiderService {

  private final OpenOptions openOptions = new OpenOptions();

  private final WebClient client;
  private final FileSystem fileSystem;
  private final String imgDirectory;
  private final PicHttpHeaderUtil picHttpHeaderUtil = new PicHttpHeaderUtil();

  public SpiderService(WebClient client, FileSystem fileSystem, String imgDirectory) {
    this.client = client;
    this.fileSystem = fileSystem;
    this.imgDirectory = imgDirectory;
  }

  public void login(String email, String password, Handler<AsyncResult<Void>> handler) {
    String uri = "/auth/sign-in";

    JsonObject data = new JsonObject()
      .put("email", email)
      .put("password", password);

    Handler<AsyncResult<JsonObject>> f1 = r -> {
      if (r.succeeded()) {
        String token = r.result()
          .getJsonObject("data")
          .getString("token");

        picHttpHeaderUtil.setToken(token);
        handler.handle(Future.succeededFuture());
      } else handler.handle(Future.failedFuture(r.cause()));
    };

    client.post(443, PicHttpHeaderUtil.HOST, uri)
      .as(BodyCodec.jsonObject())
      .putHeaders(picHttpHeaderUtil.getHttpHeaders(uri, HttpMethod.POST))
      .sendJsonObject(data, HttpCallbackCommons.standardHttpCallback(f1));
  }

  public void getMyFavoriteBooks(int pageNum, Handler<AsyncResult<JsonObject>> handler) {
    String uri = String.format("/users/favourite?page=%d", pageNum);

    client.get(443, PicHttpHeaderUtil.HOST, uri)
      .as(BodyCodec.jsonObject())
      .putHeaders(picHttpHeaderUtil.getHttpHeaders(uri, HttpMethod.GET))
      .send(HttpCallbackCommons.standardHttpCallback(handler));
  }

  public void getBookResult(String bookId, int epsId, int pageNum, Handler<AsyncResult<JsonObject>> handler) {
    String uri = String.format("/comics/%s/order/%d/pages?page=%d", bookId, epsId, pageNum);

    client.get(443, PicHttpHeaderUtil.HOST, uri)
      .as(BodyCodec.jsonObject())
      .putHeaders(picHttpHeaderUtil.getHttpHeaders(uri, HttpMethod.GET))
      .send(HttpCallbackCommons.standardHttpCallback(handler));
  }

  public void downloadImg(String fileServer, String imgPath,
                          String bookTitle, int epsId, String imgOriginalName,
                          Handler<AsyncResult<Boolean>> handler) {

    Paths.get(imgDirectory, bookTitle, String.valueOf(epsId)).toFile().mkdirs();
    String filePath = Paths.get(imgDirectory, bookTitle, String.valueOf(epsId), imgOriginalName).toString();

    String uri = "/static/" + imgPath;

    fileSystem.exists(filePath, res -> {
      if (res.succeeded()) {
        if (!res.result()) {
          Future.<AsyncFile>future(promise -> fileSystem.open(filePath, openOptions, r -> {
            if (r.succeeded()) promise.complete(r.result());
            else promise.fail(r.cause());
          })).compose(file -> Future.future(promise ->
            client.get(443, fileServer, uri)
              .as(BodyCodec.pipe(file))
              .send(HttpCallbackCommons.standardHttpCallback(r -> {
                file.close();

                if (r.succeeded()) promise.complete();
                else {
                  fileSystem.delete(filePath, t -> {
                    if (t.failed()) t.cause().printStackTrace();
                  });
                  promise.fail(r.cause());
                }
              }))
          )).onSuccess(none -> handler.handle(Future.succeededFuture(true)))
            .onFailure(cause -> handler.handle(Future.failedFuture(cause)));
        } else handler.handle(Future.succeededFuture(false));
      } else handler.handle(Future.failedFuture(res.cause()));
    });
  }
}
