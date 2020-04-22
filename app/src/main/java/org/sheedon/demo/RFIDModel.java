package org.sheedon.demo;

import org.sheedon.serial.retrofit.serialport.RULES;

/**
 * RFIDModel
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2020/4/8 23:45
 */
public class RFIDModel {

    @RULES(end = 2)
    private String commandType;// 命令类型
    @RULES(begin = 2, end = 4)
    private String command;// 命令
    @RULES(begin = 4, end = 8)
    private String length;// 长度
    @RULES(begin = 8, end = 10)
    private String signalStrength;// 信号强度
    @RULES(begin = 10, end = 14)
    private String pc;// PC值
    @RULES(begin = 14, end = 38)
    private String labelNumber;// 标签编号

    public String getCommandType() {
        return commandType;
    }

    public String getCommand() {
        return command;
    }

    public String getLength() {
        return length;
    }

    public String getSignalStrength() {
        return signalStrength;
    }

    public String getPc() {
        return pc;
    }

    /**
     * 获取标签编号
     */
    public String getDecodeLabelNumber() {
        return labelNumber;
    }

    /**
     * 获取信号强度
     */
    public long getDecodeSignalStrength() {
        return convertNum();
    }


    /**
     * 16进制转10进制
     */
    private long convertNum() {
        try {
            return Integer.parseInt(signalStrength, 16);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    @Override
    public String toString() {
        return "RFIDModel{" +
                "commandType='" + commandType + '\'' +
                ", command='" + command + '\'' +
                ", length='" + length + '\'' +
                ", signalStrength='" + signalStrength + '\'' +
                ", pc='" + pc + '\'' +
                ", labelNumber='" + labelNumber + '\'' +
                '}';
    }
}
