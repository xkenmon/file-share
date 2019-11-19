package com.xkenmon.share.codec;

import static com.xkenmon.share.common.constant.InfoOptions.hasDirSize;
import static com.xkenmon.share.common.constant.InfoOptions.hasFileMd5;

import com.xkenmon.share.common.constant.CmdResponseType;
import com.xkenmon.share.common.constant.FileType;
import com.xkenmon.share.common.response.DirectoryInfoResponse;
import com.xkenmon.share.common.response.DirectoryInfoResponse.ItemInfo;
import com.xkenmon.share.common.response.DownloadResponse;
import com.xkenmon.share.common.response.ErrorResponse;
import com.xkenmon.share.common.response.FileInfoResponse;
import com.xkenmon.share.util.CodecUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.ReplayingDecoder;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.java.Log;

@Log
public class CmdResponseDecoder extends ReplayingDecoder {

  @Override
  protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
    byte type = in.readByte();
    switch (type) {
      case CmdResponseType.DOWNLOAD_RESPONSE:
        decodeDownloadResponse(in, out);
        break;
      case CmdResponseType.FILE_INFO_RESPONSE:
        decodeFileInfoResponse(in, out);
        break;
      case CmdResponseType.DIR_INFO_RESPONSE:
        decodeDirInfoResponse(in, out);
        break;
      case CmdResponseType.ERROR:
        decodeErrorResponse(in, out);
        break;
      default:
        throw new DecoderException("unknown response type: " + type);
    }
  }

  private void decodeDirInfoResponse(ByteBuf in, List<Object> out) {
    DirectoryInfoResponse response = new DirectoryInfoResponse();
    byte options = in.readByte();
    response.setOptions(options);

    String path = CodecUtil.decodeShortString(in);
    if (path == null) {
      return;
    }
    response.setPath(path);

    if (hasDirSize(options)) {
      long size = in.readLong();
      response.setSize(size);
    }
    int listSize = in.readInt();
    List<ItemInfo> infoList = new ArrayList<>(listSize);
    for (int i = 0; i < listSize; i++) {
      ItemInfo info = decodeItemInfo(in);
      if (info == null) {
        return;
      }
      infoList.add(info);
    }
    response.setItemInfoList(infoList);
    out.add(response);
  }

  private ItemInfo decodeItemInfo(ByteBuf in) {
    ItemInfo itemInfo = new ItemInfo();
    String path = CodecUtil.decodeShortString(in);
    if (path == null) {
      return null;
    }
    itemInfo.setPath(path);
    byte type = in.readByte();
    itemInfo.setType(type);
    if (type == FileType.FILE) {
      long size = in.readLong();
      itemInfo.setSize(size);
    }
    return itemInfo;
  }

  private void decodeFileInfoResponse(ByteBuf in, List<Object> out) {
    FileInfoResponse info = new FileInfoResponse();

    byte options = in.readByte();
    info.setOptions(options);
    String path = CodecUtil.decodeShortString(in);

    if (path == null) {
      return;
    }
    info.setPath(path);

    byte fileType = in.readByte();
    info.setFileType(fileType);

    if (fileType == FileType.FILE || hasDirSize(options)) {
      long fileSize = in.readLong();
      info.setSize(fileSize);
    }
    int blockSize = in.readInt();
    info.setBlockSize(blockSize);

    if (FileType.FILE == fileType && hasFileMd5(options)) {
      byte[] md5 = new byte[16];
      in.readBytes(md5);
      info.setMd5(md5);
    }

    out.add(info);
  }

  private void decodeDownloadResponse(ByteBuf in, List<Object> out) {
    long offset = in.readLong();
    int blockSize = in.readInt();
    long blockIndex = in.readLong();
    byte[] data = CodecUtil.readIntLenBytes(in);
    if (data == null) {
      return;
    }
    byte[] md5 = new byte[16];
    in.readBytes(md5);

    DownloadResponse response = new DownloadResponse(offset, blockSize, blockIndex, data, md5);
    out.add(response);
  }

  private void decodeErrorResponse(ByteBuf in, List<Object> out) {
    String message = CodecUtil.decodeShortString(in);
    if (message == null) {
      return;
    }
    ErrorResponse response = new ErrorResponse();
    response.setMessage(message);
    out.add(response);
  }

}
