package com.xkenmon.share.common.constant;

public final class InfoOptions {

  public static final byte DIR_SIZE = 0x01;
  public static final byte FILE_MD5 = 0x02;

  public static byte markFileMd5(byte options) {
    return (byte) (options | FILE_MD5);
  }

  public static boolean hasFileMd5(byte options) {
    return (options & FILE_MD5) != 0x00;
  }

  public static boolean hasDirSize(byte options) {
    return (options & DIR_SIZE) != 0x00;
  }

}
