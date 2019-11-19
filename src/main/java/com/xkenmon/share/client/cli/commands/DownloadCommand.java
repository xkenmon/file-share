package com.xkenmon.share.client.cli.commands;

import com.xkenmon.share.client.cli.CliContext;
import com.xkenmon.share.client.cli.RemotePathHolder;
import com.xkenmon.share.client.cli.converter.ByteSizeConverter;
import com.xkenmon.share.common.constant.InfoOptions;
import com.xkenmon.share.common.request.FileDownloadRequest;
import com.xkenmon.share.common.request.InfoRequest;
import com.xkenmon.share.common.response.CmdResponse;
import com.xkenmon.share.common.response.DirectoryInfoResponse;
import com.xkenmon.share.common.response.DownloadResponse;
import com.xkenmon.share.common.response.ErrorResponse;
import com.xkenmon.share.common.response.FileInfoResponse;
import com.xkenmon.share.util.ByteUtil;
import com.xkenmon.share.util.DigestUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.traffic.ChannelTrafficShapingHandler;
import io.netty.handler.traffic.TrafficCounter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import lombok.Data;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "download", aliases = "down", description = "download special file from remote server")
@Data
public class DownloadCommand implements Runnable {

  private final PrintWriter out;
  private final ChannelHandlerContext handlerContext;
  private final CliContext cliContext;
  private final BlockingQueue<CmdResponse> responsesQueue;

  @Option(names = {"-blockSize", "-bs"}, defaultValue = "0",
      description = "download block size, e.g. 2M, 20M",
      converter = ByteSizeConverter.class)
  private int blockSize;

  @Option(names = {"--limit", "-l"}, defaultValue = "-1",
      description = "download/up speed limit, e.g. 2M, 512K, 0 unlimited.",
      converter = ByteSizeConverter.class)
  private int limit;

  @Option(names = {"--force", "-f"}, defaultValue = "false",
      description = "overwrite if local file exists.")
  private boolean force;

  @Option(names = {"--help", "-h"}, usageHelp = true, description = "display this message")
  private boolean help;

  @Parameters(index = "0", description = "remote file path")
  private String remotePath;
  @Parameters(index = "1", description = "local file path")
  private Path localPath;

  private TrafficCounter counter;


  public DownloadCommand(PrintWriter out, ChannelHandlerContext handlerContext,
      CliContext cliContext,
      BlockingQueue<CmdResponse> responseQueue) {
    this.out = out;
    this.handlerContext = handlerContext;
    this.cliContext = cliContext;
    this.responsesQueue = responseQueue;
    this.counter = handlerContext.pipeline().get(ChannelTrafficShapingHandler.class)
        .trafficCounter();
  }

  @Override
  public void run() {

    // set connection limit
    if (limit != -1) {
      ChannelTrafficShapingHandler trafficShapingHandler = handlerContext.pipeline()
          .get(ChannelTrafficShapingHandler.class);
      trafficShapingHandler.setReadLimit(limit);
      trafficShapingHandler.setWriteLimit(limit);
    }

    // check local path
    if (Files.exists(localPath) && !Files.isDirectory(localPath)) {
      if (force) {
        try {
          Files.deleteIfExists(localPath);
          out.println("delete exists file: " + localPath.toAbsolutePath());
        } catch (IOException e) {
          out.println("unable to delete: " + localPath.toAbsolutePath() + " - " + e.getMessage());
          out.flush();
          return;
        }
      } else {
        out.println(localPath.toAbsolutePath().toString() + " exists!");
        return;
      }
    }
    if (Files.isDirectory(localPath)) {
      localPath = localPath.resolve(Paths.get(remotePath).getFileName());
    }
    if (!Files.exists(localPath.getParent())) {
      try {
        Files.createDirectories(localPath.getParent());
      } catch (IOException e) {
        out.println("can not create parent dir of " + localPath.toAbsolutePath().toString());
        out.flush();
        return;
      }
    }

    // assemble remote path
    this.remotePath = Paths.get(cliContext.remotePwd()).resolve(remotePath).toString();

    // get remote path info
    InfoRequest request = new InfoRequest(remotePath, InfoOptions.FILE_MD5);
    handlerContext.pipeline().writeAndFlush(request);
    try {
      CmdResponse response = responsesQueue.take();
      if (response instanceof FileInfoResponse) {
        FileInfoResponse msg = (FileInfoResponse) response;
        RemotePathHolder.addPath(msg.getPath());
        if (blockSize == 0) {
          blockSize = msg.getBlockSize();
        }
        new FileDownloader(localPath, remotePath, msg.getSize(), msg.getMd5()).download();
      } else if (response instanceof DirectoryInfoResponse) {
        DirectoryInfoResponse msg = (DirectoryInfoResponse) response;

        out.println("directory download unsupported");
      } else if (response instanceof ErrorResponse) {
        ErrorResponse msg = (ErrorResponse) response;
        out.println("Error: " + msg.getMessage());
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch (IOException e) {
      out.println("IO exception: " + e.getMessage());
      e.printStackTrace();
    }
    out.flush();
  }

  class FileDownloader {

    private final Path localPath;
    private final String remotePath;
    private final long fileSize;
    private final byte[] md5;
    private final long offset;
    private final Path metaDataPath;

    FileDownloader(Path localPath, String remotePath, long size, byte[] md5) throws IOException {
      this.localPath = localPath;
      this.remotePath = remotePath;
      this.fileSize = size;
      this.md5 = md5;
      metaDataPath = this.localPath.getParent().resolve(".fs_meta_" + localPath.getFileName());
      if (Files.exists(metaDataPath) && Files.exists(localPath)) {
        offset = readMetaOffset();
      } else {
        offset = 0;
        writeMetaOffset(offset);
      }
      if (Files.notExists(localPath)) {
        out.println("create file: " + localPath.toAbsolutePath());
        Files.createFile(localPath);
      }
    }

    void download() {
      try {
        RandomAccessFile localFile = new RandomAccessFile(localPath.toFile(), "rw");

        if (localFile.skipBytes((int) offset) != offset) {
          out.println("unable to skip offset bytes.");
          return;
        }
        long maxIndex = (fileSize - offset) / blockSize;
        if ((fileSize - offset) % blockSize != 0) {
          maxIndex += 1;
        }
        long index = 0;

        out.println(String
            .format("downloading '%s' to '%s'", remotePath, localPath.toAbsolutePath().toString()));
        out.println(
            String.format("download offset %d, bs: %d, bc = %d", offset, blockSize, maxIndex));
        out.flush();

        while (index < maxIndex) {
          FileDownloadRequest downloadRequest = new FileDownloadRequest(remotePath, offset,
              getBlockSize(), index);
          handlerContext.pipeline().writeAndFlush(downloadRequest);
          CmdResponse cmdResponse = responsesQueue.take();
          if (cmdResponse instanceof DownloadResponse) {
            DownloadResponse response = (DownloadResponse) cmdResponse;
            if (response.getData().length != blockSize && index != maxIndex - 1) {
              throw new IllegalStateException(String
                  .format("download response data size (%s) not equal block size (%s)",
                      response.getData().length,
                      blockSize));
            }
            out.println(String.format("receive - offset: %d, bs: %d, bi: %d, len: %d",
                response.getOffset(),
                response.getBlockSize(),
                response.getBlockIndex(),
                response.getData().length));
            out.println(counter);
            if (!Arrays.equals(response.getMd5(), DigestUtil.md5(response.getData()))) {
              out.println("response data checksum not correct.");
            }
            out.flush();
            localFile.write(response.getData());
            writeMetaOffset(offset + index * blockSize);
            index++;
          } else if (cmdResponse instanceof ErrorResponse) {
            out.println(((ErrorResponse) cmdResponse).getMessage());
            return;
          } else {
            out.println("unexpected response type: " + cmdResponse.getClass());
            out.println(cmdResponse);
            return;
          }
        }
        if (Arrays.equals(DigestUtil.md5(localPath), md5)) {
          out.println("download success");
        } else {
          out.println("checksum not correct.");
        }
        Files.deleteIfExists(metaDataPath);
      } catch (FileNotFoundException e) {
        out.println("File not found: " + localPath.toFile().getPath());
      } catch (InterruptedException | IOException e) {
        out.println(e.getMessage());
        e.printStackTrace();
      } finally {
        out.flush();
      }
    }

    private void writeMetaOffset(long offset) throws IOException {
      Files.write(metaDataPath, ByteUtil.longToByte(offset), StandardOpenOption.CREATE,
          StandardOpenOption.WRITE);
    }

    private long readMetaOffset() throws IOException {
      byte[] bytes = Files.readAllBytes(metaDataPath);
      return ByteUtil.byteToLong(bytes);
    }
  }
}
