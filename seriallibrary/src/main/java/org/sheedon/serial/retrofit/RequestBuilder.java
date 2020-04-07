package org.sheedon.serial.retrofit;


import org.sheedon.serial.Request;

/**
 * 请求构建者
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2020/3/21 23:42
 */
public class RequestBuilder {

    private final org.sheedon.serial.RequestBuilder requestBuilder;

    private ServiceMethod serviceMethod;
    // 串口消息
    private SerialMessage serialMessage;
    // 绑定数据
    private BindCallback bindCallback;

    public RequestBuilder(ServiceMethod serviceMethod, SerialMessage serialMessage,
                          BindCallback bindCallback) {
        this.serviceMethod = serviceMethod;
        this.serialMessage = serialMessage;
        this.bindCallback = bindCallback;
        this.requestBuilder = new org.sheedon.serial.RequestBuilder();
    }


    /**
     * 添加路径参数
     */
    public void addPathParam(String name, String value, boolean encoded) {
        if (serialMessage == null || serialMessage.getMessageBit() == null) {
            throw new AssertionError();
        }
        String message = serialMessage.getMessageBit();
        serialMessage.setMessageBit(message.replace("{" + name + "}", value));
    }

    /**
     * 添加反馈信息路径参数
     */
    public void addBackPathParam(String name, String value, boolean encoded) {
        if (bindCallback == null || bindCallback.getBackName() == null) {
            throw new AssertionError();
        }
        String message = bindCallback.getBackName();
        bindCallback.setBackName(message.replace("{" + name + "}", value));
    }

    Request build() {
        if (serialMessage == null) {
            throw new IllegalArgumentException(
                    "serialMessage is Null");
        }

        serialMessage.setParityBit(serviceMethod.toParityBit(serialMessage));

        String data = serviceMethod.toRequestBody(serialMessage);

        if (data == null) {
            throw new IllegalArgumentException(
                    "data is Null in serialMessage");
        }

        return requestBuilder
                .data(data)
                .delayMilliSecond(bindCallback.getDelayMilliSecond())
                .backName(bindCallback.getBackName())
                .build();
    }
}
