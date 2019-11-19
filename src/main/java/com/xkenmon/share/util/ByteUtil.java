package com.xkenmon.share.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

public final class ByteUtil {

  private static final ByteBuf longBuf = ByteBufAllocator.DEFAULT.buffer(8);

  public static byte[] longToByte(long num) {
    resetLongBuf();
    longBuf.writeLong(num);
    byte[] res = new byte[8];
    longBuf.readBytes(res);
    return res;
  }

  public static long byteToLong(byte[] bytes) {
    resetLongBuf();
    longBuf.writeBytes(bytes);
    return longBuf.readLong();
  }

  private static void resetLongBuf() {
    longBuf.resetReaderIndex();
    longBuf.resetWriterIndex();
  }
}
