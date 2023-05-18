package com.xuxd.kafka.console.aspect.annotation;

import java.lang.annotation.*;

/**
 * 权限注解，开启认证的时候拥有该权限的用户才能访问对应接口.
 *
 * @author: xuxd
 * @date: 2023/5/17 22:30
 **/
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Permission {

    String[] value() default {};
}
