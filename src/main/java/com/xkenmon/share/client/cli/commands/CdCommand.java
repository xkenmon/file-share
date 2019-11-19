package com.xkenmon.share.client.cli.commands;

import com.xkenmon.share.client.cli.CliContext;
import com.xkenmon.share.common.request.InfoRequest;
import com.xkenmon.share.common.response.CmdResponse;
import com.xkenmon.share.common.response.DirectoryInfoResponse;
import com.xkenmon.share.common.response.ErrorResponse;
import com.xkenmon.share.common.response.FileInfoResponse;
import io.netty.channel.ChannelHandlerContext;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.BlockingQueue;
import lombok.Data;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "cd", description = "change remote work dir")
@Data
public class CdCommand implements Runnable {

  private final ChannelHandlerContext handlerContext;
  private final PrintWriter out;
  private final BlockingQueue<CmdResponse> responseQueue;

  @Parameters(index = "0")
  private String path;

  private final CliContext cliContext;

  public CdCommand(PrintWriter out,
      ChannelHandlerContext handlerContext, CliContext cliContext,
      BlockingQueue<CmdResponse> responseQueue) {
    this.out = out;
    this.handlerContext = handlerContext;
    this.cliContext = cliContext;
    this.responseQueue = responseQueue;
  }

  @Override
  public void run() {
    Path remotePath = Paths.get(cliContext.remotePwd());
    String realPath = remotePath.resolve(path).toString();
    InfoRequest request = new InfoRequest(realPath, (byte) 0x00);

    handlerContext.pipeline().writeAndFlush(request);
    try {
      CmdResponse cmdResponse = responseQueue.take();
      if (cmdResponse instanceof FileInfoResponse) {
        FileInfoResponse response = (FileInfoResponse) cmdResponse;
        out.println(String.format("%s is a file", response.getPath()));
      } else if (cmdResponse instanceof DirectoryInfoResponse) {
        DirectoryInfoResponse response = (DirectoryInfoResponse) cmdResponse;
        cliContext.setRemotePwd(response.getPath());
      } else if (cmdResponse instanceof ErrorResponse) {
        out.println(((ErrorResponse) cmdResponse).getMessage());
      } else {
        out.println("unknown response type: " + cmdResponse.getResponseType());
        out.println(cmdResponse.toString());
      }
      out.flush();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}
