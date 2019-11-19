package com.xkenmon.share.client.cli.commands;

import com.xkenmon.share.client.cli.CliContext;
import java.io.PrintWriter;
import picocli.CommandLine.Command;

@Command(name = "pwd", description = "print current remote work dir.")
public class PwdCommand implements Runnable {

  private final CliContext cliContext;
  private final PrintWriter out;

  public PwdCommand(PrintWriter out, CliContext cliContext){
    this.out = out;
    this.cliContext = cliContext;
  }

  @Override
  public void run() {
    this.out.println(cliContext.remotePwd());
    out.flush();
  }
}
