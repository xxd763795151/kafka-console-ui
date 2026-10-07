package com.xuxd.kafka.console.exception;

import com.xuxd.kafka.console.beans.ResponseData;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class GlobalExceptionHandlerTest {

    @Test
    void databaseDetailsAreNotReturnedToTheClient() {
        String sqlError = "PRIMARY KEY ON T_SYS_ROLE(ID); INSERT INTO t_sys_role";
        ResponseData<?> response = (ResponseData<?>) new GlobalExceptionHandler()
                .exceptionHandler(null, new DuplicateKeyException(sqlError));

        assertEquals(ResponseData.FAILED_CODE, response.getCode());
        assertEquals("操作失败，请稍后重试或联系管理员", response.getMsg());
        assertFalse(response.getMsg().contains(sqlError));
    }

    @Test
    void authorizationDetailsAreNotReturnedToTheClient() {
        ResponseData<?> response = (ResponseData<?>) new GlobalExceptionHandler()
                .unAuthorizedExceptionHandler(null, new UnAuthorizedException("admin:secret-permission"));

        assertEquals("无权限执行此操作", response.getMsg());
    }
}
