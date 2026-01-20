package com.crm.project.exception;

import com.crm.project.dto.response.MyApiResponse;
import com.crm.project.internal.AppErrorInfo;
import jakarta.validation.ConstraintViolation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Map;
import java.util.Objects;
import java.util.List;
import java.util.Comparator;
import java.util.ArrayList;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(value = Exception.class)
    ResponseEntity<MyApiResponse> handlingException(Exception exception) {
        log.error("Exception: ", exception);
        MyApiResponse apiResponse = new MyApiResponse();

        apiResponse.setCode(ErrorCode.UNCATEGORIZED_EXCEPTION.getCode());
        apiResponse.setMessage(ErrorCode.UNCATEGORIZED_EXCEPTION.getMessage());
        apiResponse.setError(exception.getMessage());

        return ResponseEntity.badRequest().body(apiResponse);
    }

    @ExceptionHandler(value = AppException.class)
    ResponseEntity<MyApiResponse> handlingAppException(AppException exception) {
        ErrorCode errorCode = exception.getErrorCode();
        AppErrorInfo apiErrorInfo = com.crm.project.internal.AppErrorInfo.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .errorField(exception.getErrorAt())
                .build();
        MyApiResponse apiResponse = MyApiResponse.builder()
                .code(errorCode.getStatusCode().value())
                .message("Process Failed")
                .error(apiErrorInfo)
                .build();
        return ResponseEntity.status(errorCode.getStatusCode()).body(apiResponse);
    }

    @ExceptionHandler(value = AccessDeniedException.class)
    ResponseEntity<MyApiResponse> handlingAccessDeniedException(AccessDeniedException exception) {
        ErrorCode errorCode = ErrorCode.ACCESS_DENIED;
        AppErrorInfo apiErrorInfo = AppErrorInfo.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();
        MyApiResponse apiResponse = MyApiResponse.builder()
                .code(errorCode.getStatusCode().value())
                .message("Process Failed")
                .error(apiErrorInfo)
                .build();
        return ResponseEntity.status(errorCode.getStatusCode()).body(apiResponse);
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<MyApiResponse> handlingMissingServletRequestPartException(
            MissingServletRequestPartException exception) {

        String part = exception.getRequestPartName();
        ErrorCode errorCode = ErrorCode.INVALID_REQUEST_PART;

        Map<String, Object> attributes = Map.of(
                "part", part
        );

        // build dynamic message
        String finalMessage = mapAttributes(errorCode.getMessage(), attributes);

        AppErrorInfo apiErrorInfo = com.crm.project.internal.AppErrorInfo.builder()
                .code(errorCode.getCode())
                .message(finalMessage)
                .build();

        MyApiResponse apiResponse = MyApiResponse.builder()
                .code(errorCode.getStatusCode().value())
                .message("Missing request part")
                .error(apiErrorInfo)
                .build();

        return ResponseEntity.status(errorCode.getStatusCode()).body(apiResponse);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<MyApiResponse> handlingHttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException exception) {
        ErrorCode errorCode = ErrorCode.INVALID_FILE_TYPE;
        AppErrorInfo apiErrorInfo = com.crm.project.internal.AppErrorInfo.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();
        MyApiResponse apiResponse = MyApiResponse.builder()
                .code(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value())
                .message("Process Failed")
                .error(apiErrorInfo)
                .build();
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(apiResponse);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<MyApiResponse> handlingMaxUploadSizeExceededException(MaxUploadSizeExceededException exception) {
        ErrorCode errorCode = ErrorCode.INVALID_FILE_SIZE;
        AppErrorInfo appErrorInfo = AppErrorInfo.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();
        MyApiResponse apiResponse = MyApiResponse.builder()
                .code(errorCode.getStatusCode().value())
                .message("Process Failed")
                .error(appErrorInfo)
                .build();
        return ResponseEntity.status(errorCode.getStatusCode()).body(apiResponse);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<MyApiResponse> handlingNoResourceFoundException(NoResourceFoundException exception) {
        ErrorCode errorCode = ErrorCode.INVALID_API_ENDPOINT;
        AppErrorInfo appErrorInfo = AppErrorInfo.builder()
                .code(errorCode.getCode())
                .build();
        MyApiResponse apiResponse = MyApiResponse.builder()
                .code(errorCode.getStatusCode().value())
                .message(errorCode.getMessage())
                .build();
        return ResponseEntity.status(errorCode.getStatusCode()).body(apiResponse);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<MyApiResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {

        String fieldName = null;
        String expectedType = null;

        Throwable cause = ex.getCause();
        if (cause instanceof com.fasterxml.jackson.databind.exc.InvalidFormatException ife) {
            // Lấy tên field bị lỗi
            if (!ife.getPath().isEmpty()) {
                fieldName = ife.getPath().get(0).getFieldName();
            }
            // Lấy kiểu dữ liệu mong đợi
            expectedType = ife.getTargetType() != null ? ife.getTargetType().getSimpleName() : "unknown";
        }

        String errorMessage;
        if (fieldName != null && expectedType != null) {
            errorMessage = String.format("Field '%s' must be of type %s", fieldName, expectedType);
        } else {
            errorMessage = "Invalid request body format";
        }

        MyApiResponse response = MyApiResponse.builder()
                .code(400)
                .message("Invalid input type")
                .error(errorMessage)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<MyApiResponse> handleValidation(MethodArgumentNotValidException exception) {
        List<AppErrorInfo> errors = new ArrayList<>(exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> {
                    ValidationError validationError = parseEnum(fieldError.getDefaultMessage());

                    var violationConstraint = fieldError.unwrap(ConstraintViolation.class);

                    Map attributes = violationConstraint.getConstraintDescriptor().getAttributes();

                    String message = Objects.nonNull(attributes)
                            ? mapAttributes(validationError.getMessage(), attributes)
                            : validationError.getMessage();

                    return com.crm.project.internal.AppErrorInfo.builder()
                            .code(validationError.getCode())
                            .errorField(fieldError.getField())
                            .message(message)
                            .build();
                })
                .toList());
        errors.sort(Comparator.comparing(com.crm.project.internal.AppErrorInfo::getCode));


        MyApiResponse response = MyApiResponse.builder()
                .code(HttpStatus.BAD_REQUEST.value())
                .message("Processed Failed")
                .error(errors)
                .build();

        return ResponseEntity.badRequest().body(response);
    }

    private ValidationError parseEnum(String key) {
        try {
            return ValidationError.valueOf(key);
        } catch (IllegalArgumentException e) {
            return ValidationError.INVALID_KEY;
        }
    }


    private String mapAttributes(String template, Map<String, Object> attributes) {
        if (attributes == null || attributes.isEmpty()) {
            return template;
        }

        String result = template;
        for (var entry : attributes.entrySet()) {
            String placeholder = "{" + entry.getKey() + "}";
            result = result.replace(placeholder, String.valueOf(entry.getValue()));
        }
        return result;
    }
}
