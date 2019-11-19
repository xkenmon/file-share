package com.xkenmon.share.common.constant;

public final class FileType {

  public static final byte FILE = 0x01;
  public static final byte DIRECTORY = 0x02;

  public static String toString(byte type) {
    switch (type) {
      case FILE:
        return "File";
      case DIRECTORY:
        return "Dir";
      default:
        return "Unknown";
    }
  }
}
