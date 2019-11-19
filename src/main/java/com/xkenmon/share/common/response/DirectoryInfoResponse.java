package com.xkenmon.share.common.response;

import com.xkenmon.share.common.constant.CmdResponseType;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class DirectoryInfoResponse extends CmdResponse {

  private byte options;
  private String path;
  // optional
  private long size;
  private List<ItemInfo> itemInfoList;

  @Override
  public byte getResponseType() {
    return CmdResponseType.DIR_INFO_RESPONSE;
  }

  @Data
  public static class ItemInfo {

    private String path;
    private byte type;
    private long size;
  }
}
