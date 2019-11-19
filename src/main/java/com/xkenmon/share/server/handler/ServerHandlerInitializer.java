package com.xkenmon.share.server.handler;

import com.xkenmon.share.codec.CmdRequestDecoder;
import com.xkenmon.share.codec.CmdResponseEncoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.traffic.ChannelTrafficShapingHandler;
import io.netty.handler.traffic.GlobalTrafficShapingHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;

public class ServerHandlerInitializer extends ChannelInitializer<SocketChannel> {

  private static final EventExecutorGroup EXECUTOR_GROUP = new DefaultEventExecutorGroup(
      Runtime.getRuntime().availableProcessors() * 2);
  private final FileInfoRequestHandler fileInfoRequestHandler = new FileInfoRequestHandler();
  private final FileDownloadRequestHandler fileDownloadRequestHandler = new FileDownloadRequestHandler();
  private final GlobalTrafficShapingHandler globalTrafficShapingHandler;
  private final long channelLimit;

  public ServerHandlerInitializer(long globalLimit, long channelLimit) {
    globalTrafficShapingHandler = new GlobalTrafficShapingHandler(EXECUTOR_GROUP, globalLimit,
        globalLimit);
    this.channelLimit = channelLimit;
  }

  @Override
  protected void initChannel(SocketChannel ch) {
    ch.pipeline()
//        .addLast(new LoggingHandler(LogLevel.INFO))
        .addLast(globalTrafficShapingHandler)
        .addLast(new ChannelTrafficShapingHandler(channelLimit, channelLimit))
        .addLast(new CmdRequestDecoder())
        .addLast(fileInfoRequestHandler)
        .addLast(fileDownloadRequestHandler)
        .addLast(new CmdResponseEncoder());
  }
}
