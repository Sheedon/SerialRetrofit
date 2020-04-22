package org.sheedon.demo.converters;

import android.annotation.SuppressLint;

import org.sheedon.demo.CharsUtils;
import org.sheedon.serial.retrofit.Converter;
import org.sheedon.serial.retrofit.SerialMessage;

/**
 * 请求校验码转化器
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2020/4/20 17:12
 */
public final class RequestParityBitConverter implements Converter<SerialMessage, String> {


    @SuppressLint("DefaultLocale")
    @Override
    public String convert(SerialMessage value) {
        if (value == null)
            return "";

        if (value.getParityBit() != null && !value.getParityBit().equals("")) {
            return value.getParityBit().toUpperCase();
        }

        if (value.getMessageBit() == null || value.getMessageBit().isEmpty()) {
            return "";
        }

        String contentStr = value.getMessageBit();
        if (contentStr.length() % 2 == 1) {
            contentStr += "0";
        }

        byte[] bytes = CharsUtils.hexStringToBytes(contentStr);

        return CharsUtils.sumCheckToHexStr(bytes);
    }

}
