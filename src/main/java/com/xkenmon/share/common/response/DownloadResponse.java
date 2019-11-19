package com.xkenmon.share.common.response;

import com.xkenmon.share.common.constant.CmdResponseType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DownloadResponse extends CmdResponse {

  private long offset;

  private int blockSize;

  private long blockIndex;

  private byte[] data;

  private byte[] md5;

  @Override
  public byte getResponseType() {
    return CmdResponseType.DOWNLOAD_RESPONSE;
  }
}
