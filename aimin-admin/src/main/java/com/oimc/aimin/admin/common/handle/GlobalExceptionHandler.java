package com.oimc.aimin.admin.common.handle;


import com.oimc.aimin.base.exception.BusinessException;
import com.oimc.aimin.base.response.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("请求地址：'{}',错误信息'{}'", requestURI,e.getMessage());
        return Result.error(e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleValidException(BindException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        String message = e.getAllErrors().getFirst().getDefaultMessage();
        log.error("请求地址：'{}',错误信息'{}'", requestURI,message);
        return Result.error(message);
    }


}
