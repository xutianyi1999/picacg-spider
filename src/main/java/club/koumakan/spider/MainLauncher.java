package club.koumakan.spider;

import java.nio.file.Paths;

public class MainLauncher {

  public static void main(String[] args) {
//    Launcher.executeCommand("run", MainVerticle.class.getName());
    String s = Paths.get("/abc", "a1", "b2").toString();
    System.out.println(s);
  }
}
