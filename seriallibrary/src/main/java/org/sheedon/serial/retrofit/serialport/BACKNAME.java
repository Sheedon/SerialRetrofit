package org.sheedon.serial.retrofit.serialport;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 反馈名
 * 例如：
 * @BACKNAME("xxx")
 * Call<> getManagerList();
 *
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2020/3/22 10:38
 */
@Documented
@Target(METHOD)
@Retention(RUNTIME)
public @interface BACKNAME {
    String value();
}
