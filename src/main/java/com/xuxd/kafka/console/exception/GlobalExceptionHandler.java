package com.xuxd.kafka.console.exception;

import com.xuxd.kafka.console.beans.ResponseData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2021-10-19 14:32:18
 **/
@Slf4j
@ControllerAdvice(basePackages = "com.xuxd.kafka.console.controller")
public class GlobalExceptionHandler {

    @ResponseStatus(code = HttpStatus.FORBIDDEN)
    @ExceptionHandler(value = UnAuthorizedException.class)
    @ResponseBody
    public Object unAuthorizedExceptionHandler(HttpServletRequest req, Exception ex) throws Exception {
        log.error("unAuthorized: {}", ex.getMessage());
        return ResponseData.create().failed("UnAuthorized: " + ex.getMessage());
    }

    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public Object exceptionHandler(HttpServletRequest req, Exception ex) throws Exception {

        log.error("exception handle: ", ex);
        return ResponseData.create().failed(ex.getMessage());
    }
}
