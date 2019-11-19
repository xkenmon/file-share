package com.xkenmon.share.common.request;

import com.xkenmon.share.common.constant.CmdRequestType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InfoRequest extends CmdRequest {

  private String path;

  private byte options;

  @Override
  public byte getRequestType() {
    return CmdRequestType.FILE_INFO;
  }
}
