package com.xkenmon.share.codec;

import static com.xkenmon.share.common.constant.InfoOptions.hasDirSize;
import static com.xkenmon.share.common.constant.InfoOptions.hasFileMd5;

import com.xkenmon.share.common.constant.CmdResponseType;
import com.xkenmon.share.common.constant.FileType;
import com.xkenmon.share.common.response.CmdResponse;
import com.xkenmon.share.common.response.DirectoryInfoResponse;
import com.xkenmon.share.common.response.DirectoryInfoResponse.ItemInfo;
import com.xkenmon.share.common.response.DownloadResponse;
import com.xkenmon.share.common.response.ErrorResponse;
import com.xkenmon.share.common.response.FileInfoResponse;
import com.xkenmon.share.util.CodecUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.EncoderException;
import io.netty.handler.codec.MessageToByteEncoder;
import java.util.Objects;
import lombok.extern.java.Log;

@Log
public class CmdResponseEncoder extends MessageToByteEncoder<CmdResponse> {

  @Override
  protected void encode(ChannelHandlerContext ctx, CmdResponse msg, ByteBuf out) {
    byte type = msg.getResponseType();
    switch (type) {
      case CmdResponseType.DOWNLOAD_RESPONSE:
        encodeDownloadResponse((DownloadResponse) msg, out);
        break;
      case CmdResponseType.FILE_INFO_RESPONSE:
        encodeFileInfoResponse((FileInfoResponse) msg, out);
        break;
      case CmdResponseType.DIR_INFO_RESPONSE:
        encodeDirInfoResponse((DirectoryInfoResponse) msg, out);
        break;
      case CmdResponseType.ERROR:
        encodeErrorResponse((ErrorResponse) msg, out);
        break;
      default:
        throw new EncoderException("unknown response type: " + type);
    }
    ctx.flush();
  }


  private void encodeDownloadResponse(DownloadResponse msg, ByteBuf out) {
    out.writeByte(msg.getResponseType());
    out.writeLong(msg.getOffset());
    out.writeInt(msg.getBlockSize());
    out.writeLong(msg.getBlockIndex());
    out.writeInt(msg.getData().length);
    out.writeBytes(msg.getData());
    out.writeBytes(msg.getMd5());
  }

  private void encodeFileInfoResponse(FileInfoResponse msg, ByteBuf out) {
    out.writeByte(msg.getResponseType());
    out.writeByte(msg.getOptions());
    CodecUtil.encodeShortString(msg.getPath(), out);
    out.writeByte(msg.getFileType());
    if (FileType.FILE == msg.getFileType() || (hasDirSize(msg.getOptions()))) {
      out.writeLong(msg.getSize());
    }
    out.writeInt(msg.getBlockSize());
    if (hasFileMd5(msg.getOptions())) {
      out.writeBytes(msg.getMd5());
    }
  }

  private void encodeDirInfoResponse(DirectoryInfoResponse msg, ByteBuf out) {
    out.writeByte(msg.getResponseType());
    out.writeByte(msg.getOptions());
    CodecUtil.encodeShortString(msg.getPath(), out);
    if (hasDirSize(msg.getOptions())) {
      out.writeLong(msg.getTotalSize());
    }
    out.writeInt(msg.getItemInfoList().size());
    msg.getItemInfoList().forEach(info -> this.encodeItemInfo(info, out, msg.getOptions()));
  }

  private void encodeErrorResponse(ErrorResponse msg, ByteBuf out) {
    out.writeByte(msg.getResponseType());
    CodecUtil.encodeShortString(msg.getMessage(), out);
  }

  private void encodeItemInfo(ItemInfo info, ByteBuf out, byte options) {
    CodecUtil.encodeShortString(info.getPath(), out);
    out.writeByte(info.getType());
    if (FileType.FILE == info.getType()) {
      out.writeLong(info.getSize());
      if (hasFileMd5(options)) {
        Objects.requireNonNull(info.getMd5(), "FileMd5 option set but itemInfo.md5 is null");
        out.writeBytes(info.getMd5());
      }
    }
  }
}
