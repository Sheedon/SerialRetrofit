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

    // 结束字节 若结果内容是-100代表到结尾 依次减少代表从当前末尾为减少n个字节
    // 例如 byte[]  {0x11,0x22,0x33,0x44}
    // -100 代表都需要
    // -101 代表只需要3个
    int end() default 0;

    // 没有数据默认填充数据
    byte[] value() default 0;

    // 是否需要解码
    boolean decode() default false;
}
