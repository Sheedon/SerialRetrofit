package org.sheedon.serial.retrofit;

import androidx.annotation.GuardedBy;
import androidx.annotation.Nullable;

import org.sheedon.serial.Request;
import org.sheedon.serial.ResponseBody;

import java.io.IOException;

import static org.sheedon.serial.retrofit.Utils.throwIfFatal;

/**
 * 串口Call，用于触发串口调度
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2020/2/23 22:27
 */
final class SerialCall<T> implements Call<T> {
    private final ServiceMethod<T, ?> serviceMethod;
    private final @Nullable
    Object[] args;

    private volatile boolean canceled;

    @GuardedBy("this")
    private @Nullable
    org.sheedon.serial.Call rawCall;
    @GuardedBy("this") // Either a RuntimeException, non-fatal Error, or IOException.
    private @Nullable
    Throwable creationFailure;
    @GuardedBy("this")
    private boolean executed;

    SerialCall(ServiceMethod<T, ?> serviceMethod, @Nullable Object[] args) {
        this.serviceMethod = serviceMethod;
        this.args = args;
    }

    @Override
    public Call<T> clone() {
        return new SerialCall<>(serviceMethod, args);
    }

    @Override
    public synchronized Request request() {
        org.sheedon.serial.Call call = rawCall;
        if (call != null) {
            return call.request();
        }
        if (creationFailure != null) {
            if (creationFailure instanceof IOException) {
                throw new RuntimeException("Unable to create request.", creationFailure);
            } else if (creationFailure instanceof RuntimeException) {
                throw (RuntimeException) creationFailure;
            } else {
                throw (Error) creationFailure;
            }
        }
        try {
            return (rawCall = createRawCall()).request();
        } catch (RuntimeException | Error e) {
            throwIfFatal(e); // Do not assign a fatal error to creationFailure.
            creationFailure = e;
            throw e;
        } catch (IOException e) {
            creationFailure = e;
            throw new RuntimeException("Unable to create request.", e);
        }
    }

    @Override
    public void publishNotCallback() {
        enqueue(null);
    }

    @Override
    public void addBindCallback(final Callback<T> callback) {
        org.sheedon.serial.Call call;
        Throwable failure;

        synchronized (this) {

            call = rawCall;
            failure = creationFailure;
            if (call == null && failure == null) {
                try {
                    call = rawCall = createRawCall();
                } catch (Throwable t) {
                    failure = creationFailure = t;
                }
            }
        }

        if (failure != null) {
            dealWithCallback(callback, SerialCall.this, null, failure, false);
            return;
        }

        if (canceled) {
            call.cancel();
        }

        if (callback == null)
            return;

        call.addBindCallback(new org.sheedon.serial.Callback<org.sheedon.serial.Response>() {
            @Override
            public void onFailure(Throwable e) {
                try {
                    dealWithCallback(callback, SerialCall.this, null, e, false);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }

            @Override
            public void onResponse(org.sheedon.serial.Response rawResponse) {
                Response<T> response;
                try {
                    response = parseResponse(rawResponse);
                } catch (Throwable e) {
                    callFailure(e);
                    return;
                }
                callSuccess(response);
            }

            private void callFailure(Throwable e) {
                try {
                    dealWithCallback(callback, SerialCall.this, null, e, false);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }

            private void callSuccess(Response<T> response) {
                try {
                    dealWithCallback(callback, SerialCall.this, response, null, true);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        });
    }

    @Override
    public void unBindCallback() {
        org.sheedon.serial.Call call;

        synchronized (this) {
            call = rawCall;
        }

        if (call == null)
            return;

        call.removeBindCallback();
    }

    @Override
    public void enqueue(final Callback callback) {

        org.sheedon.serial.Call call;
        Throwable failure;

        synchronized (this) {
            if (executed) throw new IllegalStateException("Already executed.");
            executed = true;

            call = rawCall;
            failure = creationFailure;
            if (call == null && failure == null) {
                try {
                    call = rawCall = createRawCall();
                } catch (Throwable t) {
                    failure = creationFailure = t;
                }
            }
        }

        if (failure != null) {
            dealWithCallback(callback, SerialCall.this, null, failure, false);
            return;
        }

        if (canceled) {
            call.cancel();
        }

        if (callback == null)
            call.publishNotCallback();
        else
            call.enqueue(new org.sheedon.serial.Callback<org.sheedon.serial.Response>() {
                @Override
                public void onFailure(Throwable e) {
                    try {
                        dealWithCallback(callback, SerialCall.this, null, e, false);
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }

                @Override
                public void onResponse(org.sheedon.serial.Response rawResponse) {
                    Response<T> response;
                    try {
                        response = parseResponse(rawResponse);
                    } catch (Throwable e) {
                        callFailure(e);
                        return;
                    }
                    callSuccess(response);
                }

                private void callFailure(Throwable e) {
                    try {
                        dealWithCallback(callback, SerialCall.this, null, e, false);
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }

                private void callSuccess(Response<T> response) {
                    try {
                        dealWithCallback(callback, SerialCall.this, response, null, true);
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
            });
    }

    /**
     * 处理反馈
     *
     * @param callback  反馈监听
     * @param call      Call
     * @param response  反馈内容
     * @param t         错误
     * @param isSuccess 是否成功
     */
    private void dealWithCallback(Callback callback, Call<T> call, Response<T> response, Throwable t, boolean isSuccess) {

        if (callback == null)
            return;

        if (isSuccess) {
            callback.onResponse(call, response);
        } else {
            callback.onFailure(call, t);
        }

    }

    private org.sheedon.serial.Call createRawCall() throws IOException {
        Request request = serviceMethod.toRequest(args);
        org.sheedon.serial.Call call = serviceMethod.callFactory.newCall(request);
        if (call == null) {
            throw new NullPointerException("Call.MqttFactory returned null.");
        }
        return call;
    }

    Response<T> parseResponse(org.sheedon.serial.Response rawResponse) {
        ResponseBody rawBody = rawResponse.body();

        try {
            T body = serviceMethod.toResponse(rawBody);
            return Response.success(body, rawResponse);
        } catch (RuntimeException e) {
            // If the underlying source threw an exception, propagate that rather than indicating it was
            // a runtime exception.
            throw e;
        }
    }

    @Override
    public boolean isExecuted() {
        return executed;
    }

    @Override
    public void cancel() {
        canceled = true;

        org.sheedon.serial.Call call;
        synchronized (this) {
            call = rawCall;
        }
        if (call != null) {
            call.cancel();
        }
    }

    @Override
    public boolean isCanceled() {
        if (canceled) {
            return true;
        }
        synchronized (this) {
            return rawCall != null && rawCall.isCanceled();
        }
    }
}
