package com.nonBlocking.common;

import lombok.Data;
import org.springframework.stereotype.Component;

@Component
@Data
public class ResponseData {
    int errorCode = ErrorCode.SUCCESS;
    String errorMsg = "";
    Object body = null;
}
