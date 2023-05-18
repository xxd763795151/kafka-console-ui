package com.xuxd.kafka.console.aspect.annotation;

import java.lang.annotation.*;

/**
 * 该注解用到controller层的方法上
 * @author 晓东哥哥
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ControllerLog {

    String value() default "";
}
