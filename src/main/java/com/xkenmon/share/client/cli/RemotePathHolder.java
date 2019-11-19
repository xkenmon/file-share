package com.xkenmon.share.client.cli;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RemotePathHolder {

  private static final Set<String> remotePaths = new HashSet<>();

  public static synchronized void addPaths(Collection<String> list) {
    remotePaths.addAll(list);
  }

  public static synchronized void addPath(String path) {
    remotePaths.add(path);
  }

  public static List<String> getRemotePaths() {
    return new ArrayList<>(remotePaths);
  }

}
