package com.xkenmon.share.codec;

import com.xkenmon.share.common.constant.CmdRequestType;
import com.xkenmon.share.common.request.FileDownloadRequest;
import com.xkenmon.share.common.request.InfoRequest;
import com.xkenmon.share.util.CodecUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.ReplayingDecoder;
import java.util.List;

public class CmdRequestDecoder extends ReplayingDecoder {

  @Override
  protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
    byte type = in.readByte();
    switch (type) {
      case CmdRequestType.FILE_DOWNLOAD: {
        decodeDownloadRequest(in, out);
        break;
      }
      case CmdRequestType.FILE_INFO: {
        decodeInfoRequest(in, out);
        break;
      }
      default:
        throw new DecoderException("unsupported cmd type: " + type);
    }

  }

  private void decodeDownloadRequest(ByteBuf in, List<Object> out) {
    String path = CodecUtil.decodeShortString(in);
    if (path == null) {
      return;
    }

    long offset = in.readLong();
    int blockSize = in.readInt();
    long blockIndex = in.readLong();

    FileDownloadRequest downloadRequest = new FileDownloadRequest(path, offset, blockSize,
        blockIndex);
    out.add(downloadRequest);
  }

  private void decodeInfoRequest(ByteBuf in, List<Object> out) {
    byte options = in.readByte();
    String path = CodecUtil.decodeShortString(in);
    if (path == null) {
      return;
    }
    InfoRequest infoRequest = new InfoRequest();
    infoRequest.setOptions(options);
    infoRequest.setPath(path);
    out.add(infoRequest);
  }
}
