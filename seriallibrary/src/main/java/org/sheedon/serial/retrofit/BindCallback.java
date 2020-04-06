package org.sheedon.serial.retrofit;

/**
 * 反馈请求绑定数据
 * 用于记录延迟时间和反馈名
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2020/3/22 10:25
 */
class BindCallback {

    private int delayMilliSecond = -1;
    private String backName;

    int getDelayMilliSecond() {
        return delayMilliSecond;
    }

    void setDelayMilliSecond(int delayMilliSecond) {
        this.delayMilliSecond = delayMilliSecond;
    }

    String getBackName() {
        return backName;
    }

    void setBackName(String backName) {
        this.backName = backName;
    }
}
