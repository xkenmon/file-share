package com.xkenmon.share.server.handler;

import static com.xkenmon.share.common.constant.InfoOptions.DIR_SIZE;
import static com.xkenmon.share.common.constant.InfoOptions.FILE_MD5;
import static com.xkenmon.share.common.constant.InfoOptions.hasDirSize;
import static com.xkenmon.share.common.constant.InfoOptions.hasFileMd5;

import com.xkenmon.share.common.constant.FileType;
import com.xkenmon.share.common.request.InfoRequest;
import com.xkenmon.share.common.response.DirectoryInfoResponse;
import com.xkenmon.share.common.response.DirectoryInfoResponse.ItemInfo;
import com.xkenmon.share.common.response.ErrorResponse;
import com.xkenmon.share.common.response.FileInfoResponse;
import com.xkenmon.share.util.DigestUtil;
import com.xkenmon.share.util.FileUtil;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.java.Log;

@Log
@Sharable
class FileInfoRequestHandler extends SimpleChannelInboundHandler<InfoRequest> {

  private static final int DEFAULT_BLOCK_SIZE = 4 * (1 << 20);

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, InfoRequest msg) throws Exception {
    log.info("FileInfoRequest received: " + msg.getPath());
    byte options = msg.getOptions();
    String path = msg.getPath();
    Path file = Paths.get(path).toAbsolutePath().normalize();
    if (!Files.exists(file)) {
      ErrorResponse response = new ErrorResponse();
      response.setMessage("File Not Exists.");
      ctx.pipeline().writeAndFlush(response);
      log.warning(String.format("%s not exists.", path));
      return;
    }
    if (Files.isDirectory(file)) {
      byte respOpt = 0;
      DirectoryInfoResponse info = new DirectoryInfoResponse();
      info.setPath(file.toString());
      if (hasDirSize(options)) {
        info.setSize(FileUtil.getSize(file.toString()));
        respOpt |= DIR_SIZE;
      }
      info.setItemInfoList(listDir(msg.getPath()));
      info.setOptions(respOpt);
      log.info("write DirInfo: " + info.getPath());
      ctx.pipeline().writeAndFlush(info);
    } else {
      byte respOpt = 0;
      FileInfoResponse info = new FileInfoResponse();
      info.setPath(file.toString());
      info.setFileType(FileType.FILE);
      info.setSize(Files.size(file));
      if (hasFileMd5(options)) {
        info.setMd5(DigestUtil.md5(file));
        respOpt |= FILE_MD5;
      }
      info.setOptions(respOpt);
      info.setBlockSize(DEFAULT_BLOCK_SIZE);
      log.info("write FileInfo: " + info.getPath());
      ctx.pipeline().writeAndFlush(info);
    }
  }

  private List<ItemInfo> listDir(String pathStr) throws IOException {
    Path path = Paths.get(pathStr);
    return Files.list(path).map(this::toItemInfo).collect(Collectors.toList());
  }

  private ItemInfo toItemInfo(Path path) {
    ItemInfo info = new ItemInfo();
    info.setPath(path.toAbsolutePath().normalize().toString());
    if (Files.isDirectory(path)) {
      info.setType(FileType.DIRECTORY);
    } else {
      info.setType(FileType.FILE);
      try {
        info.setSize(Files.size(path));
      } catch (IOException e) {
        log.warning("unable to get file size: " + e.getMessage());
        e.printStackTrace();
      }
    }
    return info;
  }
}
