package com.xkenmon.share;

import com.xkenmon.share.bootstrap.Client;
import com.xkenmon.share.bootstrap.Server;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "", subcommands = {Client.class, Server.class})
public class Main implements Runnable {

  public static void main(String[] args) {
    CommandLine commandLine = new CommandLine(new Main());
    commandLine.execute(args);
  }

  @Override
  public void run() {
    System.out.println(new CommandLine(this).getUsageMessage());
  }
}
