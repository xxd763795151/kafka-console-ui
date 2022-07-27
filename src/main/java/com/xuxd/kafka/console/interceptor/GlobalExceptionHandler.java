package com.xuxd.kafka.console.interceptor;

import com.xuxd.kafka.console.beans.ResponseData;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2021-10-19 14:32:18
 **/
@Slf4j
@ControllerAdvice(basePackages = "com.xuxd.kafka.console.controller")
public class GlobalExceptionHandler {

    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public Object exceptionHandler(HttpServletRequest req, Exception ex) throws Exception {

        log.error("exception handle: ", ex);
        return ResponseData.create().failed(ex.getMessage());
    }
}
