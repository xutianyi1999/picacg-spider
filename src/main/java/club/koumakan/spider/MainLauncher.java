package club.koumakan.spider;

import io.vertx.core.Launcher;

public class MainLauncher {

  public static void main(String[] args) {
    System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.SLF4JLogDelegateFactory");
    Launcher.executeCommand("run", MainVerticle.class.getName());
  }
}
