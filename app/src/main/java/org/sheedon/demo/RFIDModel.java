package org.sheedon.demo;

import org.sheedon.serial.internal.CharsUtils;
import org.sheedon.serial.retrofit.serialport.RULES;

import java.util.Arrays;

/**
 * RFIDModel
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2020/4/8 23:45
 */
public class RFIDModel {

    @RULES(end = 1)
    private byte[] commandType;// 命令类型
    @RULES(begin = 1, end = 2)
    private byte[] command;// 命令
    @RULES(begin = 2, end = 4)
    private byte[] length;// 长度
    @RULES(begin = 4, end = 5)
    private byte[] signalStrength;// 信号强度
    @RULES(begin = 5, end = 7)
    private byte[] pc;// PC值
    @RULES(begin = 7, end = 19)
    private byte[] labelNumber;// 标签编号

    public byte[] getCommandType() {
        return commandType;
    }

    public byte[] getCommand() {
        return command;
    }

    public byte[] getLength() {
        return length;
    }

    public byte[] getSignalStrength() {
        return signalStrength;
    }

    public byte[] getPc() {
        return pc;
    }

    public byte[] getLabelNumber() {
        return labelNumber;
    }

    /**
     * 获取标签编号
     */
    public String getDecodeLabelNumber() {
        return CharsUtils.byte2HexStrNotEmpty(labelNumber);
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
        return signalStrength.length > 0 ? (signalStrength[0] & 0xff) : 0;
    }

    @Override
    public String toString() {
        return "RFIDModel{" +
                "commandType=" + CharsUtils.byte2HexStrNotEmpty(commandType) +
                ", command=" + CharsUtils.byte2HexStrNotEmpty(command) +
                ", length=" + CharsUtils.byte2HexStrNotEmpty(length) +
                ", signalStrength=" + CharsUtils.byte2HexStrNotEmpty(signalStrength) +
                ", pc=" + CharsUtils.byte2HexStrNotEmpty(pc) +
                ", labelNumber=" + CharsUtils.byte2HexStrNotEmpty(labelNumber) +
                '}';
    }
}
