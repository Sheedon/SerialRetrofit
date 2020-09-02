package org.sheedon.serial.retrofit;

/**
 * 串口消息
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2020/3/22 10:39
 */
public class SerialMessage {
    private String startBit;
    private String messageBit;
    private String parityBit;
    private String endBit;

    private SerialMessage() {

    }

    public static SerialMessage build(String startBit, String endBit) {
        return build(startBit, "", "", endBit);
    }

    public static SerialMessage build(String startBit, String messageBit, String parityBit, String endBit) {
        SerialMessage message = new SerialMessage();
        message.startBit = startBit;
        message.messageBit = messageBit;
        message.parityBit = parityBit;
        message.endBit = endBit;
        return message;
    }

    public String getStartBit() {
        return startBit;
    }

    public void setStartBit(String startBit) {
        this.startBit = startBit;
    }

    public String getMessageBit() {
        return messageBit;
    }

    public void setMessageBit(String messageBit) {
        this.messageBit = messageBit;
    }

    public String getParityBit() {
        return parityBit;
    }

    public void setParityBit(String parityBit) {
        this.parityBit = parityBit;
    }

    public String getEndBit() {
        return endBit;
    }

    public void setEndBit(String endBit) {
        this.endBit = endBit;
    }


    @Override
    public SerialMessage clone() {
        try {
            return (SerialMessage) super.clone();
        } catch (CloneNotSupportedException e) {
            SerialMessage message = new SerialMessage();
            message.startBit = startBit;
            message.messageBit = messageBit;
            message.parityBit = parityBit;
            message.endBit = endBit;
            return message;
        }
    }
}
