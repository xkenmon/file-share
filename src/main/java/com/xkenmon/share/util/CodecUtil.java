package com.xkenmon.share.util;

import io.netty.buffer.ByteBuf;
import io.netty.util.CharsetUtil;
import javax.annotation.Nullable;
import lombok.extern.java.Log;

@Log
public final class CodecUtil {

  public static void encodeShortString(String str, ByteBuf out) {
    byte[] message = str.getBytes(CharsetUtil.UTF_8);
    if (message.length > Short.MAX_VALUE) {
      log.warning(String
          .format("string too long(%d), excepted less than %d", message.length, Short.MAX_VALUE));
    }
    out.writeShort(message.length);
    out.writeBytes(message);
  }

  @Nullable
  public static String decodeShortString(ByteBuf in) {
    byte[] bytes = readShortLenBytes(in);
    if (bytes == null) {
      return null;
    }

    return new String(bytes, CharsetUtil.UTF_8);
  }

  @Nullable
  private static byte[] readShortLenBytes(ByteBuf in) {
    if (in.readableBytes() < 2) {
      return null;
    }
    short len = in.readShort();
    return readVarBytes(in, len);
  }

  @Nullable
  public static byte[] readIntLenBytes(ByteBuf in) {
    if (in.readableBytes() < 4) {
      return null;
    }
    int len = in.readInt();
    return readVarBytes(in, len);
  }

  @Nullable
  private static byte[] readVarBytes(ByteBuf in, int len) {
    if (in.readableBytes() < len) {
      return null;
    }
    byte[] bytes = new byte[len];
    in.readBytes(bytes);
    return bytes;
  }

}
