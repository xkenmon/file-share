package com.xkenmon.share.bootstrap;

import com.xkenmon.share.client.cli.converter.ByteSizeConverter;
import com.xkenmon.share.client.handler.ClientHandlerInitializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.Data;
import lombok.extern.java.Log;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Log
@Data
@Command(name = "client", description = "run as client")
public class Client implements Runnable {

  @Option(names = {"--server", "-s"}, description = "remote ip address", defaultValue = "localhost")
  private String serverAddress;

  @Option(names = {"--port", "-p"}, description = "remote port", defaultValue = "9999")
  private Integer port;

  @Option(names = {"--limit", "-l"},
      description = "read/write limit, e.g. 512K, 2M, 0 to unlimited",
      defaultValue = "0",
      converter = ByteSizeConverter.class)
  private Integer limit = 0;

  @Option(names = {"--help", "-h"}, usageHelp = true, description = "display this message")
  private boolean help;

  public static void main(String[] args) {
    new Client().start("localhost", 9999);
  }

  @Override
  public void run() {
    start(serverAddress, port);
  }

  private void start(String serverAddress, Integer port) {
    Bootstrap bootstrap = new Bootstrap();
    bootstrap.group(new NioEventLoopGroup())
        .channel(NioSocketChannel.class)
        .handler(new ClientHandlerInitializer(limit))
        .connect(serverAddress, port)
        .addListener(future -> {
          if (future.isSuccess()) {
            log.info("connected!");
          } else {
            log.info(future.cause().getLocalizedMessage());
            System.exit(1);
          }
        });
  }
}
