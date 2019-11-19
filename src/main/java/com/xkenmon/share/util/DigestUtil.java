package com.xkenmon.share.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

public final class DigestUtil {

  private static final char[] HEX_CHARS =
      {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

  public static byte[] md5(byte[] data) {
    Objects.requireNonNull(data, "data must not be null");
    MessageDigest digest = md5Instance();
    return digest.digest(data);
  }

  public static String toHexStr(byte[] bytes) {
    return new String(toHex(bytes));
  }

  private static char[] toHex(byte[] bytes) {
    char[] chars = new char[bytes.length << 1];
    for (int i = 0; i < chars.length; i = i + 2) {
      byte b = bytes[i / 2];
      chars[i] = HEX_CHARS[(b >>> 0x4) & 0xf];
      chars[i + 1] = HEX_CHARS[b & 0xf];
    }
    return chars;
  }

  public static byte[] md5(Path file) throws IOException {
    Objects.requireNonNull(file, "file must not be null");
    if (!Files.exists(file)) {
      throw new FileNotFoundException(file.toAbsolutePath() + " not found.");
    }
    MessageDigest digest = md5Instance();
    try (InputStream in = Files.newInputStream(file)) {
      byte[] buf = new byte[4 * 1024];
      int len;
      while ((len = in.read(buf)) != -1) {
        digest.update(buf, 0, len);
      }
    }
    return digest.digest();
  }

  private static MessageDigest instance(String algorithm) {
    try {
      return MessageDigest.getInstance(algorithm);
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }

  private static MessageDigest md5Instance() {
    return instance("MD5");
  }

  public static MessageDigest sha1Instance() {
    return instance("SHA-1");
  }

  public static MessageDigest sha256Instance() {
    return instance("SHA-256");
  }

}
