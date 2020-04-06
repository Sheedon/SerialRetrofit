package org.sheedon.serial.retrofit.serialport;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 起始位
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2020/3/9 23:06
 */
@Documented
@Target(METHOD)
@Retention(RUNTIME)
public @interface STARTBIT {
    String value() default "";
}
