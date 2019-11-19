package com.xkenmon.share.client.cli;

public class CliContext {

  private String remotePwd;

  public CliContext() {
    remotePwd = ".";
  }

  public CliContext(String remotePwd){
    this.remotePwd = remotePwd;
  }

  public String remotePwd() {
    return remotePwd;
  }

  public void setRemotePwd(String remotePwd) {
    this.remotePwd = remotePwd;
  }

}
