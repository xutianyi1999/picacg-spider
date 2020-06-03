package club.koumakan.spider.api.service;

import club.koumakan.spider.MainVerticle;
import club.koumakan.spider.api.HttpCallback;
import club.koumakan.spider.api.PicHttpHeaderUtil;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.file.FileSystem;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.codec.BodyCodec;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.BiConsumer;

public class SpiderService {

  private final OpenOptions openOptions = new OpenOptions();

  private final WebClient client;
  private final FileSystem fileSystem;
  private final String imgDirectory;
  private final HttpCallback httpCallback;
  private final PicHttpHeaderUtil picHttpHeaderUtil = new PicHttpHeaderUtil();

  public SpiderService(WebClient client, FileSystem fileSystem,
                       String imgDirectory, BiConsumer<Long, Handler<Long>> timer) {
    this.client = client;
    this.fileSystem = fileSystem;
    this.imgDirectory = imgDirectory;
    this.httpCallback = new HttpCallback(timer);
  }

  public void login(String email, String password, Handler<AsyncResult<Void>> handler) {
    String uri = "/auth/sign-in";

    JsonObject data = new JsonObject()
      .put("email", email)
      .put("password", password);

    HttpRequest<JsonObject> req = client.post(443, PicHttpHeaderUtil.HOST, uri)
      .as(BodyCodec.jsonObject())
      .putHeaders(picHttpHeaderUtil.getHttpHeaders(uri, HttpMethod.POST));

    httpCallback.<JsonObject>httpResponseHandler(f -> req.sendJsonObject(data, f),
      body -> {
        String token = body
          .getJsonObject("data")
          .getString("token");

        picHttpHeaderUtil.setToken(token);
        handler.handle(Future.succeededFuture());
      }, cause -> handler.handle(Future.failedFuture(cause)));
  }

  private void common(Handler<AsyncResult<JsonObject>> handler, String uri) {
    HttpRequest<JsonObject> req = client.get(443, PicHttpHeaderUtil.HOST, uri)
      .as(BodyCodec.jsonObject())
      .putHeaders(picHttpHeaderUtil.getHttpHeaders(uri, HttpMethod.GET));

    httpCallback.httpResponseHandler(req::send,
      body -> handler.handle(Future.succeededFuture(body)),
      err -> handler.handle(Future.failedFuture(err))
    );
  }

  public void getMyFavoriteBooks(int pageNum, Handler<AsyncResult<JsonObject>> handler) {
    String uri = String.format("/users/favourite?page=%d", pageNum);
    common(handler, uri);
  }

  public void getBookResult(String bookId, int epsId, int pageNum, Handler<AsyncResult<JsonObject>> handler) {
    String uri = String.format("/comics/%s/order/%d/pages?page=%d", bookId, epsId, pageNum);
    common(handler, uri);
  }

  public void downloadImg(String fileServer, String imgPath,
                          String bookTitle, int epsId, String imgOriginalName,
                          Handler<AsyncResult<Boolean>> handler) {

    Path filePath = Paths.get(
      imgDirectory,
      URLEncoder.encode(bookTitle, StandardCharsets.UTF_8),
      String.valueOf(epsId),
      URLEncoder.encode(imgOriginalName, StandardCharsets.UTF_8)
    );

    String filePathStr = filePath.toString();
    String fileParentStr = filePath.getParent().toString();
    String uri = "/static/" + imgPath;

    fileSystem.exists(filePathStr, res -> {
      if (res.succeeded()) {
        if (!res.result()) {
          Future.future(promise -> fileSystem.mkdirs(fileParentStr, r -> {
            if (r.succeeded()) promise.complete();
            else promise.fail(r.cause());
          })).compose(none -> Future.<AsyncFile>future(promise -> fileSystem.open(filePathStr, openOptions, r -> {
            if (r.succeeded()) promise.complete(r.result());
            else promise.fail(r.cause());
          }))).compose(file -> Future.future(promise -> {
              HttpRequest<Void> req = client.get(443, fileServer, uri)
                .as(BodyCodec.pipe(file));

              httpCallback.httpResponseHandler(req::send,
                none -> promise.complete(),
                error -> {
                  fileSystem.delete(filePathStr, t -> {
                    if (t.failed()) MainVerticle.logger.error("file del error", t.cause());
                  });
                  promise.fail(error);
                }
              );
            }
          )).onSuccess(none -> handler.handle(Future.succeededFuture(true)))
            .onFailure(cause -> handler.handle(Future.failedFuture(cause)));
        } else handler.handle(Future.succeededFuture(false));
      } else handler.handle(Future.failedFuture(res.cause()));
    });
  }
}
