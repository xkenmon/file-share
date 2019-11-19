package com.xkenmon.share.client.handler;

import com.xkenmon.share.client.cli.CommandLineRunner;
import com.xkenmon.share.codec.CmdRequestEncoder;
import com.xkenmon.share.codec.CmdResponseDecoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.traffic.ChannelTrafficShapingHandler;

public class ClientHandlerInitializer extends ChannelInitializer<SocketChannel> {

  private final long limit;

  public ClientHandlerInitializer(long limit) {
    this.limit = limit;
  }

  @Override
  protected void initChannel(SocketChannel ch) {
    ch.pipeline()
        //入站起点
//        .addLast(new LoggingHandler(LogLevel.INFO))
        .addLast(new ChannelTrafficShapingHandler(limit, limit))
        .addLast(new CmdResponseDecoder())
        .addLast(new CmdRequestEncoder())
        .addLast(new CommandLineHandler(new CommandLineRunner()));
  }
}
