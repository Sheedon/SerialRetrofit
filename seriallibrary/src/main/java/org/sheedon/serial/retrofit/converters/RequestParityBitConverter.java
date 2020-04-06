package org.sheedon.serial.retrofit.converters;


import org.sheedon.serial.retrofit.Converter;
import org.sheedon.serial.retrofit.SerialMessage;
import org.sheedon.serial.utils.CRC16M;
import org.sheedon.serial.utils.CRC_16;

/**
 * 请求校验码转化器
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2020/2/24 22:41
 */
public final class RequestParityBitConverter implements Converter<SerialMessage, String> {


    @Override
    public String convert(SerialMessage value) {
        if (value == null)
            return "";
        String contentStr = value.getStartBit() + value.getMessageBit();
        StringBuilder checkResult = new StringBuilder(CRC16M.getBufHexStr(CRC_16.getSendBuf(contentStr)));
        checkResult = checkResult.delete(0, contentStr.length());
        for (int index = checkResult.length(); index < 4; index++) {
            checkResult.insert(0, "0");
        }
        return checkResult.toString();
    }
}
