package com.xkenmon.share.client.handler;

import com.xkenmon.share.client.cli.CommandLineRunner;
import com.xkenmon.share.common.response.CmdResponse;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;

class CommandLineHandler extends ChannelDuplexHandler {

  private ChannelHandlerContext ctx;
  private final CommandLineRunner runner;

  CommandLineHandler(CommandLineRunner runner) {
    this.runner = runner;
  }

  @Override
  public void channelActive(ChannelHandlerContext ctx) throws Exception {
    runner.setHandlerContext(ctx);
    new Thread(runner).start();
    super.channelActive(ctx);
  }


  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) {
    if (msg instanceof CmdResponse) {
      runner.handlerResponse((CmdResponse) msg);
    } else {
      System.out.println("not cmd response.");
    }
  }
}
