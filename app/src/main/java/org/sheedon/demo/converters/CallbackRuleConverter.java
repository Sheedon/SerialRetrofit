package org.sheedon.demo.converters;


import org.sheedon.serial.DataConverter;

/**
 * 反馈规则转换器
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2020/3/11 0:45
 */
public class CallbackRuleConverter implements DataConverter<String, String> {

    CallbackRuleConverter() {

    }

    // 数据格式
    // 协议头    命令类型      命令      数据长度位     其他内容    CRC16校验   停止位
    // BB        04            22         0005         00001032    B07A         7E
    @Override
    public String convert(String value) {
        if (value == null || value.isEmpty() || value.length() < 6)
            return "";

        return value.substring(2, 6).toUpperCase();
    }
}
