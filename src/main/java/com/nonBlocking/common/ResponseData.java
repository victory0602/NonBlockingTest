package com.nonBlocking.common;

import lombok.Data;

@Data
public class ResponseData {
    int errorCode = ErrorCode.SUCCESS;
    String errorMsg = "";
    Object body = null;

    public static void setResponseError(ResponseData responseData, int errorCode, String errorMsg, Object object) {
        responseData.setErrorCode(errorCode);
        responseData.setErrorMsg(errorMsg);
        responseData.setBody(object);
    }
}
