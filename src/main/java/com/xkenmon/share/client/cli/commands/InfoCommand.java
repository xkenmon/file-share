package com.xkenmon.share.client.cli.commands;

import static com.xkenmon.share.common.constant.InfoOptions.DIR_SIZE;
import static com.xkenmon.share.common.constant.InfoOptions.FILE_MD5;

import com.xkenmon.share.client.cli.CliContext;
import com.xkenmon.share.client.cli.RemotePathHolder;
import com.xkenmon.share.common.constant.FileType;
import com.xkenmon.share.common.constant.InfoOptions;
import com.xkenmon.share.common.request.InfoRequest;
import com.xkenmon.share.common.response.CmdResponse;
import com.xkenmon.share.common.response.DirectoryInfoResponse;
import com.xkenmon.share.common.response.DirectoryInfoResponse.ItemInfo;
import com.xkenmon.share.common.response.ErrorResponse;
import com.xkenmon.share.common.response.FileInfoResponse;
import com.xkenmon.share.util.DigestUtil;
import com.xkenmon.share.util.FileUtil;
import io.netty.channel.ChannelHandlerContext;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;
import lombok.Data;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "ls", aliases = {"list"}, description = "show file/dir information")
@Data
public class InfoCommand implements Runnable {

  private final AttributedStyle dirStyle = new AttributedStyle().bold()
      .foreground(AttributedStyle.YELLOW);

  private final PrintWriter out;
  private final ChannelHandlerContext context;

  private final BlockingQueue<CmdResponse> responseQueue;
  private final CliContext cliContext;

  @Option(names = {"-s", "-size"}, description = "count directory total size (only for directory)")
  private boolean dirSize;

  @Option(names = {"-c", "-md5"}, description = "calculate file checksum (only for file)")
  private boolean md5;

  @Option(names = {"-h", "--help"}, usageHelp = true, description = "display this message")
  private boolean help;

  @Parameters(defaultValue = ".", description = "remote file path")
  private String path;

  public InfoCommand(PrintWriter out, ChannelHandlerContext context,
      CliContext cliContext, BlockingQueue<CmdResponse> responseQueue) {
    this.out = out;
    this.context = context;
    this.cliContext = cliContext;
    this.responseQueue = responseQueue;
  }

  @Override
  public void run() {
    byte options = 0;
    if (dirSize) {
      options |= DIR_SIZE;
    }
    if (md5) {
      options |= FILE_MD5;
    }

    Path remotePwdPath = Paths.get(cliContext.remotePwd());
    this.path = remotePwdPath.resolve(path).toString();

    InfoRequest request = new InfoRequest(path, options);
    context.pipeline().writeAndFlush(request);
    try {
      CmdResponse cmdResponse = responseQueue.take();
      if (cmdResponse instanceof FileInfoResponse) {
        FileInfoResponse response = (FileInfoResponse) cmdResponse;
        RemotePathHolder.addPath(request.getPath());
        out.println(formatFileInfoResponse(response));
      } else if (cmdResponse instanceof DirectoryInfoResponse) {
        DirectoryInfoResponse response = (DirectoryInfoResponse) cmdResponse;
        RemotePathHolder.addPaths(response.getItemInfoList().stream()
            .map(ItemInfo::getPath)
            .collect(Collectors.toList()));
        out.println(formatDirInfoResponse(response));
      } else if (cmdResponse instanceof ErrorResponse) {
        out.println("Error: " + ((ErrorResponse) cmdResponse).getMessage());
      } else {
        out.println("unknown response: " + cmdResponse.getClass());
        out.println(cmdResponse);
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  private String formatFileInfoResponse(FileInfoResponse response) {
    StringBuilder builder = new StringBuilder();
    builder.append("Path: ").append(response.getPath()).append('\n');
    if (InfoOptions.hasFileMd5(response.getOptions())) {
      builder.append("MD5: ").append(DigestUtil.toHexStr(response.getMd5())).append('\n');
    }
    builder.append("Size: ").append(FileUtil.formatSize(response.getSize())).append('\n');
    builder.append("Block Size: ").append(FileUtil.formatSize(response.getBlockSize()));
    return builder.toString();
  }

  private String formatDirInfoResponse(DirectoryInfoResponse response) {
    StringBuilder builder = new StringBuilder();
    builder.append("Path: ").append(response.getPath()).append('\n');
    builder.append("-------------------------\n");
    if (InfoOptions.hasDirSize(response.getOptions())) {
      builder.append("Total Size: ").append(FileUtil.formatSize(response.getSize())).append('\n');
    }
    builder.append(String.format("%-10s%-10s%s\n", "Type", "Size", "Path"));
    builder.append(String.format("%-10s%-10s%s\n", "----", "----", "----"));
    for (ItemInfo info : response.getItemInfoList()) {
      builder.append(String.format("%-10s%-10s%s\n",
          FileType.toString(info.getType()),
          FileUtil.formatSize(info.getSize()),
          renderFileName(info)
      ));
    }
    return builder.toString();
  }

  private String renderFileName(ItemInfo info) {
    String name = Paths.get(info.getPath()).getFileName().toString();
    if (info.getType() == FileType.FILE) {
      return name;
    } else {
      return new AttributedString(name, dirStyle).toAnsi();
    }
  }
}
