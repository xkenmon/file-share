package com.xkenmon.share.client.cli;

import com.xkenmon.share.client.cli.commands.CdCommand;
import com.xkenmon.share.client.cli.commands.CliCommands;
import com.xkenmon.share.client.cli.commands.DownloadCommand;
import com.xkenmon.share.client.cli.commands.ExitCommand;
import com.xkenmon.share.client.cli.commands.InfoCommand;
import com.xkenmon.share.client.cli.commands.PwdCommand;
import com.xkenmon.share.client.cli.completer.CliCompleter;
import com.xkenmon.share.common.response.CmdResponse;
import io.netty.channel.ChannelHandlerContext;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.ParsedLine;
import org.jline.reader.UserInterruptException;
import org.jline.reader.impl.DefaultParser;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import picocli.CommandLine;

public class CommandLineRunner implements Runnable {

  private ChannelHandlerContext handlerContext;
  private CliContext cliContext;
  private PrintWriter out;

  private final BlockingQueue<CmdResponse> responseQueue = new LinkedBlockingQueue<>();

  @Override
  public void run() {
    this.cliContext = new CliContext();

    try {
      Terminal terminal = TerminalBuilder.builder()
          .system(true)
          .build();
      this.out = terminal.writer();
      CommandLine commandLine = initCommands();
      LineReader reader = LineReaderBuilder.builder()
          .terminal(terminal)
          .parser(new DefaultParser())
          .completer(new CliCompleter(commandLine.getCommandSpec()))
          .build();

      for (; ; ) {
        String prompt = getPrompt();
        String line = reader.readLine(prompt);
        ParsedLine pl = reader.getParser().parse(line, 0);
        String[] arguments = pl.words().toArray(new String[0]);
        commandLine.execute(arguments);
      }
    } catch (IOException e) {
      e.printStackTrace();
    } catch (EndOfFileException | UserInterruptException e) {
      System.out.println("Bye...");
      handlerContext.close();
      System.exit(0);
    }
  }

  private String getPrompt() {

    Path curPath = Paths.get(cliContext.remotePwd()).getFileName();
    String curPathName = null;
    if (curPath == null) {
      if (cliContext.remotePwd().equals("/")) {
        curPathName = "/";
      }
    } else {
      curPathName = curPath.toString();
    }

    return String.format("[%s %s]> ",
        handlerContext.channel().remoteAddress().toString(),
        curPathName);
  }

  private CommandLine initCommands() {
    CliCommands commands = new CliCommands(out);
    return new CommandLine(commands)
        .addSubcommand(new ExitCommand(out, handlerContext, responseQueue))
        .addSubcommand(new InfoCommand(out, handlerContext, cliContext, responseQueue))
        .addSubcommand(new DownloadCommand(out, handlerContext, cliContext, responseQueue))
        .addSubcommand(new PwdCommand(out, cliContext))
        .addSubcommand(new CdCommand(out, handlerContext, cliContext, responseQueue));
  }

  public void handlerResponse(CmdResponse response) {
    responseQueue.offer(response);
  }

  public void setHandlerContext(ChannelHandlerContext handlerContext) {
    this.handlerContext = handlerContext;
  }

}
