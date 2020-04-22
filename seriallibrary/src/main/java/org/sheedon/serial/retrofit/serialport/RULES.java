package org.sheedon.serial.retrofit.serialport;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 字符长度
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2020/3/24 12:58
 */
@Documented
@Retention(RUNTIME)
@Target(FIELD)
public @interface RULES {
    // 开始字节
    int begin() default 0;

    // 结束字节
    int end() default 0;

    // 没有数据默认填充数据
    String value() default "";
}
