package com.xkenmon.share.client.cli.commands;

import com.xkenmon.share.common.response.CmdResponse;
import io.netty.channel.ChannelHandlerContext;
import java.io.PrintWriter;
import java.util.concurrent.BlockingQueue;
import picocli.CommandLine.Command;

@Command(name = "exit", description = "exit cli")
public
class ExitCommand implements Runnable {

  private final PrintWriter out;
  private final ChannelHandlerContext context;

  public ExitCommand(PrintWriter out, ChannelHandlerContext context,
      BlockingQueue<CmdResponse> responseQueue) {
    this.context = context;
    this.out = out;
  }

  @Override
  public void run() {
    context.close();
    out.println("Bye...");
    System.exit(0);
  }
}
