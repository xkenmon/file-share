package com.xkenmon.share.bootstrap;

import com.xkenmon.share.client.cli.converter.ByteSizeConverter;
import com.xkenmon.share.server.handler.ServerHandlerInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.Data;
import lombok.extern.java.Log;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Data
@Log
@Command(name = "server", description = "run as server")
public class Server implements Runnable {

  @Option(names = {"--listen",
      "-l"}, defaultValue = "0.0.0.0", description = "server bind address")
  private String listen;

  @Option(names = {"--port", "-p"}, defaultValue = "9999", description = "server bind port")
  private Integer port;

  @Option(names = {"--channel-limit"}, defaultValue = "0",
      description = "r/w speed limit per connection. e.g. 1M", converter = ByteSizeConverter.class)
  private Integer channelLimit;

  @Option(names = {"--global-limit"}, defaultValue = "0",
      description = "global r/w speed limit. e.g. 1G", converter = ByteSizeConverter.class)
  private Integer globalLimit;

  @Option(names = {"--help", "-h"}, usageHelp = true, description = "display this message")
  private boolean help;

  public static void main(String[] args) {
    new Server().start("0.0.0.0", 9999);
  }

  @Override
  public void run() {
    this.start(listen, port);
  }

  private void start(String listen, Integer port) {
    ServerBootstrap bootstrap = new ServerBootstrap();
    bootstrap.group(new NioEventLoopGroup())
        .channel(NioServerSocketChannel.class)
        .childHandler(new ServerHandlerInitializer(globalLimit, channelLimit))
        .bind(listen, port)
        .syncUninterruptibly()
        .addListener(future -> log.info(String.format("server started at %s:%d", listen, port)));
  }

}
