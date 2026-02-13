package com.crm.project.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {

    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    USERNAME_EXSITED(1001, "Username exsited, please fill in another username", HttpStatus.BAD_REQUEST),
    USER_NOT_FOUND(1002, "User not found", HttpStatus.NOT_FOUND),
    EMAIL_EXSITED(1003, "Email exsited, this email has been register", HttpStatus.BAD_REQUEST),
    PHONE_NUMBER_EXSITED(1004, "Phone number exsited, this phone has been register", HttpStatus.BAD_REQUEST),
    UNAUTHENTICATED(1005, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    NO_RESULTS(1006, "No results", HttpStatus.NOT_FOUND),
    PRODUCT_NOT_FOUND(1007, "Product not found", HttpStatus.NOT_FOUND),
    PRODUCT_SKU_EXISTED(1008, "Product sku already exists", HttpStatus.BAD_REQUEST),
    WRONG_MATCHING(1009, "Wrong matching, matching request is not suitable to the given file", HttpStatus.BAD_REQUEST),
    STAGE_NOT_FOUND(1010, "Stage not found", HttpStatus.NOT_FOUND),
    LEAD_NOT_FOUND(1011, "Lead not found", HttpStatus.NOT_FOUND),
    STAGE_IN_USE(1012, "Stage has constraint with some leads", HttpStatus.BAD_REQUEST),
    STAGE_EXISTED(1013, "Stage already exists", HttpStatus.BAD_REQUEST),
    DEFAULT_STAGE_IMMUTABLE(1014, "Default stage is immutable", HttpStatus.BAD_REQUEST),
    QUOTATION_NOT_FOUND(1015, "Quotation not found", HttpStatus.NOT_FOUND),
    CUSTOMER_NOT_FOUND(1016, "Customer not found", HttpStatus.NOT_FOUND),
    ACTIVITY_NOT_FOUND(1017, "Activity not found", HttpStatus.NOT_FOUND),
    INVALID_LEAD_ACTIVITY_RELATION(1018, "Invalid lead activity relation", HttpStatus.BAD_REQUEST),
    ORDER_NOT_FOUND(1019, "Order not found", HttpStatus.NOT_FOUND),
    ORDER_CODE_EXISTED(1020, "Order code already exists", HttpStatus.BAD_REQUEST),
    QUOTATION_ORDER_EXISTED(1021, "This quotation has been converted into order", HttpStatus.BAD_REQUEST),
    OUT_OF_STOCK(1022, "Products out of stock", HttpStatus.BAD_REQUEST),
    PRODUCT_CONFLICT(1023, "Order conflict, products have been updated by other transaction", HttpStatus.CONFLICT),

    INVALID_FILE_TYPE(1100, "File type is not supported", HttpStatus.UNSUPPORTED_MEDIA_TYPE),
    INVALID_FILE_SIZE(1101, "File size limit is 1MB", HttpStatus.BAD_REQUEST),
    EMPTY_FILE(1102, "Empty file", HttpStatus.BAD_REQUEST),
    LIMIT_ROWS_EXCEEDED(1103, "Number of rows limit is 100", HttpStatus.BAD_REQUEST),
    INVALID_REQUEST_PART(1104, "Required part '{part}' is not presented", HttpStatus.BAD_REQUEST),
    MISSING_FILE(1105, "Required file does not exist", HttpStatus.BAD_REQUEST),
    MAILING_FAILED(1106, "Mailing failed", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_API_ENDPOINT(1107, "Invalid API endpoint", HttpStatus.INTERNAL_SERVER_ERROR),

    INVALID_REFRESH_TOKEN(1110, "Invalid refresh token", HttpStatus.UNAUTHORIZED),
    ACCESS_DENIED(1111, "Access denied", HttpStatus.UNAUTHORIZED),
    ;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;
}
