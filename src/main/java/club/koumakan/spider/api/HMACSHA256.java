package club.koumakan.spider.api;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class HMACSHA256 {

  private final Mac mac;

  public HMACSHA256(byte[] key) {
    try {
      SecretKeySpec signingKey = new SecretKeySpec(key, "HmacSHA256");
      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(signingKey);
      this.mac = mac;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public String encode(byte[] data) {
    byte[] b = mac.doFinal(data);

    StringBuilder hs = new StringBuilder();
    String stmp;
    for (int n = 0; b != null && n < b.length; n++) {
      stmp = Integer.toHexString(b[n] & 0XFF);
      if (stmp.length() == 1) {
        hs.append('0');
      }
      hs.append(stmp);
    }
    return hs.toString();
  }
}
