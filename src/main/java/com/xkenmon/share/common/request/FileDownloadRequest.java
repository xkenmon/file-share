package com.xkenmon.share.common.request;

import com.xkenmon.share.common.constant.CmdRequestType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileDownloadRequest extends CmdRequest {

  private String path;

  private long offset;

  private int blockSize;

  private long blockIndex;

  @Override
  public byte getRequestType() {
    return CmdRequestType.FILE_DOWNLOAD;
  }
}
