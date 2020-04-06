package org.sheedon.serial.retrofit;

/**
 * 串口反馈Callback
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2020/3/21 22:49
 */
public interface Callback<T> {

    void onResponse(Call<T> call, Response<T> response);

    /**
     * Invoked when a network exception occurred talking to the server or when an unexpected
     * exception occurred creating the request or processing the response.
     */
    void onFailure(Call<T> call, Throwable t);
}
