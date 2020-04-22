package org.sheedon.demo.converters;

import android.annotation.SuppressLint;

import org.sheedon.demo.CharsUtils;
import org.sheedon.serial.DataCheckBean;
import org.sheedon.serial.DataConverter;
import org.sheedon.serial.ResponseBody;

/**
 * 数据校验转化器
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2020/3/11 0:45
 */
public class CheckDataConverter implements DataConverter<StringBuffer, DataCheckBean> {

    private static final String STARTBIT = "BB";
    private static final String ENDBIT = "7E";

    CheckDataConverter() {

    }

    // 数据格式
    // 协议头    命令类型      命令      数据长度位     其他内容    CRC16校验   停止位
    // BB        04            22         0005         00001032    B07A         7E
    @Override
    public DataCheckBean convert(StringBuffer value) {
        if (value == null || value.length() == 0) {
            return DataCheckBean.build(null, 0);
        }

        int index = value.indexOf(STARTBIT);
        if (index == -1) {
            return DataCheckBean.build(null, 0);
        }

        if (index + 10 >= value.length()) {
            return DataCheckBean.build(null, index);
        }

        // 一个内容到总长度
        String lengthStr = value.substring(index + 6, index + 10);
        int length = calcLength(lengthStr) * 2;
        if (length < 0 || index + length + 14 > value.length()) {
            return DataCheckBean.build(null, index);
        }

        String content = value.substring(index + 2, index + length + 12);
        boolean check = checkContent(content);
        if (check) {
            ResponseBody body = ResponseBody.build(STARTBIT,
                    content.substring(0, content.length() - 2),
                    content.substring(content.length() - 2),
                    ENDBIT, STARTBIT + content + ENDBIT);

            return DataCheckBean.build(body, index + length + 14);
        } else {
            return DataCheckBean.build(null, index + length + 14);
        }
    }

    /**
     * 获取总长度
     *
     * @param str 字符
     */
    private int calcLength(String str) {
        String highPosition = str.substring(0, 2);
        String lowPosition = str.substring(2, 4);

        int low = Integer.parseInt(lowPosition, 16);
        int high = Integer.parseInt(highPosition, 16);

        return high * 16 * 16 + low;

    }


    /**
     * 核实内容校验码
     * 拿到校验码 后两位
     * 拿到内容 除了后两位外的数据
     *
     * @param content 内容
     * @return 校验是否一致
     */
    private boolean checkContent(String content) {
        if (content.length() <= 2)
            return false;

        String checkStr = content.substring(content.length() - 2);
        String contentStr = content.substring(0, content.length() - 2);
        StringBuilder checkResult = new StringBuilder(getCheckResult(contentStr));
        for (int index = checkResult.length(); index < 2; index++) {
            checkResult.insert(0, "0");
        }

        return checkResult.toString().equalsIgnoreCase(checkStr);
    }

    @SuppressLint("DefaultLocale")
    private String getCheckResult(String contentStr) {
        if (contentStr == null)
            return "";

        if (contentStr.length() % 2 == 1) {
            contentStr += "0";
        }

        byte[] bytes = CharsUtils.hexStringToBytes(contentStr);

        return CharsUtils.sumCheckToHexStr(bytes);
    }
}
