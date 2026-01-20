package com.crm.project.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AppException extends RuntimeException {
    private ErrorCode errorCode;
    private String errorAt;

    public AppException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public AppException(ErrorCode errorCode, String errorField) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.errorAt = errorField;
    }

}
