package com.xkenmon.share.server.handler;

import com.xkenmon.share.common.request.FileDownloadRequest;
import com.xkenmon.share.common.response.DownloadResponse;
import com.xkenmon.share.common.response.ErrorResponse;
import com.xkenmon.share.util.DigestUtil;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.extern.java.Log;

@Log
@Sharable
class FileDownloadRequestHandler extends SimpleChannelInboundHandler<FileDownloadRequest> {

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, FileDownloadRequest msg) throws Exception {
    log.info("FileDownloadRequest received: " + msg);
    Path path = Paths.get(msg.getPath());
    if (!Files.exists(path)) {
      String err = String.format("path %s not exists.", path.toAbsolutePath().toString());
      log.warning(err);
      ctx.writeAndFlush(new ErrorResponse(err));
    }
    try (RandomAccessFile file = new RandomAccessFile(path.toFile(), "r")) {
      long start = msg.getOffset() + msg.getBlockSize() * msg.getBlockIndex();
      long end = Math.min(file.length(), start + msg.getBlockSize());
      byte[] data = new byte[Math.toIntExact(end - start)];
      file.skipBytes((int) start);
      file.readFully(data);
      byte[] md5 = DigestUtil.md5(data);
      DownloadResponse response = new DownloadResponse(msg.getOffset(), msg.getBlockSize(),
          msg.getBlockIndex(), data, md5);
      log.info("write DownloadResponse - len: " + response.getData().length);
      ctx.pipeline().writeAndFlush(response);
    }
  }
}
