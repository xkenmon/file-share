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
public class ErrorResponse extends CmdResponse {

  private String message;

  @Override
  public byte getResponseType() {
    return CmdResponseType.ERROR;
  }
}
