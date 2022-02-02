package com.nonBlocking.common;

import lombok.Data;

@Data
public class ResponseData {
    int errorCode = ErrorCode.SUCCESS;
    String errorMsg = "";
    Object body = null;
}
