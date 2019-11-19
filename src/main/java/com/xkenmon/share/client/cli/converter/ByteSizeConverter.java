package com.xkenmon.share.client.cli.converter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import picocli.CommandLine.ITypeConverter;

public class ByteSizeConverter implements ITypeConverter<Integer> {

  private final Pattern pattern = Pattern.compile("^([-+]?\\d+)([BbKkMm]?)$");

  @Override
  public Integer convert(String value) {
    Matcher matcher = pattern.matcher(value);
    if (matcher.find()) {
      if (matcher.groupCount() == 2) {
        Integer size = Integer.valueOf(matcher.group(1));
        String unit = matcher.group(2);
        if (unit.isEmpty() || unit.equalsIgnoreCase("B")) {
          return size;
        }
        if (unit.equalsIgnoreCase("K")) {
          return size << 10;
        }
        if (unit.equalsIgnoreCase("M")) {
          return size << 20;
        }
      }
    }
    throw new IllegalArgumentException("invalid argument: " + value);
  }
}
