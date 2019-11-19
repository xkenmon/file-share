package com.xkenmon.share.codec;

import com.xkenmon.share.common.request.CmdRequest;
import com.xkenmon.share.common.request.FileDownloadRequest;
import com.xkenmon.share.common.request.InfoRequest;
import com.xkenmon.share.util.CodecUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.EncoderException;
import io.netty.handler.codec.MessageToByteEncoder;

public class CmdRequestEncoder extends MessageToByteEncoder<CmdRequest> {

  @Override
  protected void encode(ChannelHandlerContext ctx, CmdRequest msg, ByteBuf out) {
    if (msg instanceof InfoRequest) {
      encodeInfoRequest((InfoRequest) msg, out);
    } else if (msg instanceof FileDownloadRequest) {
      encodeDownloadRequest((FileDownloadRequest) msg, out);
    } else {
      throw new EncoderException("unsupported cmd request type: " + msg.getClass());
    }
  }

  private void encodeInfoRequest(InfoRequest request, ByteBuf out) {
    out.writeByte(request.getRequestType());
    out.writeByte(request.getOptions());
    CodecUtil.encodeShortString(request.getPath(), out);
  }

  private void encodeDownloadRequest(FileDownloadRequest request, ByteBuf out) {
    out.writeByte(request.getRequestType());
    CodecUtil.encodeShortString(request.getPath(), out);
    out.writeLong(request.getOffset());
    out.writeInt(request.getBlockSize());
    out.writeLong(request.getBlockIndex());
  }
}
