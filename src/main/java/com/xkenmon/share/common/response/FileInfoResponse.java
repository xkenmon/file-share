package com.xkenmon.share.common.response;

import com.xkenmon.share.common.constant.CmdResponseType;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class FileInfoResponse extends CmdResponse {

  private byte options;

  /**
   * 2 byte string len
   */
  private String path;

  private byte fileType;

  // optional for dir
  private long size;

  private int blockSize;

  // optional
  private byte[] md5;

  @Override
  public byte getResponseType() {
    return CmdResponseType.FILE_INFO_RESPONSE;
  }
}
