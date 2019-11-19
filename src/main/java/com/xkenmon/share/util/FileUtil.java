package com.xkenmon.share.util;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import lombok.extern.java.Log;

@Log
public final class FileUtil {

  public static long getSize(String name) throws IOException {
    Path path = Paths.get(name);
    if (!Files.isDirectory(path)) {
      return Files.size(path);
    }
    DirSizeVisitor visitor = new DirSizeVisitor();
    Files.walkFileTree(path, visitor);
    return visitor.getTotalSize();
  }

  public static String formatSize(long s) {
    if ((float) s < 1 << 10) {
      return String.format("%.1f%s", (float) s, "");
    }
    if ((float) s < 1L << 20) {
      return String.format("%.1f%s", (float) s / (1 << 10), "K");
    }
    if ((float) s < 1L << 30) {
      return String.format("%.1f%s", (float) s / (1 << 20), "M");
    }
    if ((float) s < 1L << 40) {
      return String.format("%.1f%s", (float) s / (1 << 30), "M");
    }
    return String.format("%.1f%s", (float) s / (1L << 40), "G");
  }

  private static class DirSizeVisitor extends SimpleFileVisitor<Path> {

    private long totalSize = 0;

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
      if (Files.isDirectory(file) && Files.isReadable(file)) {
        return FileVisitResult.CONTINUE;
      }
      try {
        totalSize += Files.size(file);
      } catch (IOException e) {
        log.warning("unable get size of " + file + " - " + e);
      }
      return FileVisitResult.CONTINUE;
    }

    long getTotalSize() {
      return totalSize;
    }
  }

}
