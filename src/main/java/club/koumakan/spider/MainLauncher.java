package club.koumakan.spider;

import io.vertx.core.Launcher;

public class MainLauncher {

  public static void main(String[] args) {
    Launcher.executeCommand("run", MainVerticle.class.getName());
  }
}
