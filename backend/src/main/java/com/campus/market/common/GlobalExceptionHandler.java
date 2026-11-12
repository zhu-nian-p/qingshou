package com.campus.market.common;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

/**
 * 全局异常处理：所有异常统一转成 {"code": ..., "message": ..., "data": null}。
 * <p>
 * 原则：能明确判断的错误给 4xx + 友好中文；未知异常统一 500，并把堆栈写进后端日志。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 业务异常：用户名重复、权限不足、状态不允许等 */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Void>> handleBusiness(BusinessException ex) {
        return build(ex.getCode(), ex.getMessage());
    }

    /** @Valid 校验失败（@RequestBody） */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Void>> handleValid(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getDefaultMessage() == null ? "参数不正确" : error.getDefaultMessage())
                .collect(Collectors.joining("；"));
        return build(400, message);
    }

    /** @Validated 校验失败（@RequestParam / @PathVariable） */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Result<Void>> handleConstraint(ConstraintViolationException ex) {
        String message = ex.getConstraintViolations().stream()
                .map(violation -> violation.getMessage())
                .collect(Collectors.joining("；"));
        return build(400, message);
    }

    /** 请求体不是合法 JSON，或字段类型对不上 */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Result<Void>> handleUnreadable(HttpMessageNotReadableException ex) {
        return build(400, "请求体不是合法的 JSON，或字段类型不正确");
    }

    /** 缺少必填参数 / 参数类型转换失败 */
    @ExceptionHandler({MissingServletRequestParameterException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<Result<Void>> handleParam(Exception ex) {
        return build(400, "请求参数不正确");
    }

    /** 上传文件超过 spring.servlet.multipart 限制 */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Result<Void>> handleUploadSize(MaxUploadSizeExceededException ex) {
        return build(400, "上传文件过大，单张图片不能超过 5MB");
    }

    /** 请求方法不支持，例如对只支持 GET 的接口发 POST */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Result<Void>> handleMethod(HttpRequestMethodNotSupportedException ex) {
        return build(405, "请求方式不支持：" + ex.getMethod());
    }

    /** 接口或静态资源不存在 */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Result<Void>> handleNotFound(NoResourceFoundException ex) {
        return build(404, "接口或资源不存在");
    }

    /** 唯一索引冲突（用户名、学号、同一订单重复评价等） */
    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<Result<Void>> handleDuplicate(DuplicateKeyException ex) {
        log.warn("唯一索引冲突: {}", ex.getMessage());
        return build(400, "数据已存在，可能存在重复提交");
    }

    /** 兜底：未预料的异常 */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleOther(Exception ex) {
        log.error("未处理异常", ex);
        return build(500, "服务器内部错误，请稍后重试");
    }

    private ResponseEntity<Result<Void>> build(int code, String message) {
        return ResponseEntity.status(code).body(Result.fail(code, message));
    }
}
