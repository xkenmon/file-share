package com.xkenmon.share.client.cli.commands;

import java.io.PrintWriter;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "", description = "file-share client CLI", footer = {"", "Press Ctrl-D to exit."})
public class CliCommands implements Runnable {

  private final PrintWriter out;

  public CliCommands(PrintWriter out) {
    this.out = out;
  }

  @Override
  public void run() {
    new CommandLine(this).usage(out);
  }
}
