package org.sheedon.serial.retrofit;


import org.sheedon.serial.Request;

/**
 * 对Retrofit方法的调用，该方法将请求发送到串口并返回响应。
 * 每个调用都会产生自己的请求和响应对。使用{@link #clone}对相同的Web服务器进行具有相同参数的多个调用；
 * 这可用于实现轮询或重试失败的呼叫。(暂未使用)
 * 调用可以与{@link #publishNotCallback}无反馈执行，
 * 也可以与{@link * #enqueue}异步有反馈执行。
 * 无论哪种情况，都可以使用{@link #cancel}随时取消通话。
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2020/2/22 17:26
 */
public interface Call<T> extends Cloneable {
    /**
     * Returns the original request that initiated this call.
     */
    Request request();

    /**
     * 无反馈请求
     */
    void publishNotCallback();

    /**
     * 异步反馈请求
     */
    void enqueue(Callback<T> responseCallback);

    /**
     * Cancels the request, if possible. Requests that are already complete cannot be canceled.
     */
    void cancel();

    /**
     * Returns true if this call has been either {@linkplain #publishNotCallback() executed} or {@linkplain
     * #enqueue(Callback) enqueued}. It is an error to execute or enqueue a call more than once.
     */
    boolean isExecuted();

    /**
     * True if {@link #cancel()} was called.
     */
    boolean isCanceled();

    /**
     * Create a new, identical call to this one which can be enqueued or executed even if this call
     * has already been.
     */
    Call<T> clone();
}
