package org.sheedon.serial.retrofit.converters;


import org.sheedon.serial.retrofit.Converter;
import org.sheedon.serial.retrofit.SerialMessage;

/**
 * 请求内容转化类
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2020/2/24 22:41
 */
public final class RequestBodyConverter implements Converter<SerialMessage, String> {

    @Override
    public String convert(SerialMessage value) {
        if (value == null)
            return "";
        return value.getStartBit() + value.getMessageBit() + value.getParityBit() + value.getEndBit();
    }
}
