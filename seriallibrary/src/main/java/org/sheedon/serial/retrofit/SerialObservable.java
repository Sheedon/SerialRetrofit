package org.sheedon.serial.retrofit;


import androidx.annotation.GuardedBy;
import androidx.annotation.Nullable;

import org.sheedon.serial.Request;
import org.sheedon.serial.ResponseBody;

import java.io.IOException;

import static org.sheedon.serial.retrofit.Utils.throwIfFatal;


/**
 * 串口观察者
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2020/4/14 14:20
 */
final class SerialObservable<T> implements Observable<T> {
    private final ServiceMethod<T, ?> serviceMethod;
    private final @Nullable
    Object[] args;

    private volatile boolean canceled;

    @GuardedBy("this")
    private @Nullable
    org.sheedon.serial.Observable rawObservable;
    @GuardedBy("this") // Either a RuntimeException, non-fatal Error, or IOException.
    private @Nullable
    Throwable creationFailure;
    @GuardedBy("this")
    private boolean executed;

    SerialObservable(ServiceMethod<T, ?> serviceMethod, @Nullable Object[] args) {
        this.serviceMethod = serviceMethod;
        this.args = args;
    }

    @Override
    public Observable<T> clone() {
        return new SerialObservable<>(serviceMethod, args);
    }

    @Override
    public synchronized Request request() {
        org.sheedon.serial.Observable observable = rawObservable;
        if (observable != null) {
            return observable.request();
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
            return (rawObservable = createRawObservable()).request();
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
    public void subscribe(final Callback.Observable<T> callback) {
        org.sheedon.serial.Observable observable;
        Throwable failure;

        synchronized (this) {

            observable = rawObservable;
            failure = creationFailure;
            if (observable == null && failure == null) {
                try {
                    observable = rawObservable = createRawObservable();
                } catch (Throwable t) {
                    failure = creationFailure = t;
                }
            }
        }

        if (failure != null) {
            dealWithCallback(callback, SerialObservable.this, null, failure, false);
            return;
        }

        if (canceled) {
            observable.cancel();
        }

        if (callback == null)
            return;

        observable.subscribe(new org.sheedon.serial.Callback<org.sheedon.serial.Response>() {
            @Override
            public void onFailure(Throwable e) {
                try {
                    dealWithCallback(callback, SerialObservable.this, null, e, false);
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
                    dealWithCallback(callback, SerialObservable.this, null, e, false);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }

            private void callSuccess(Response<T> response) {
                try {
                    dealWithCallback(callback, SerialObservable.this, response, null, true);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        });
    }

    /**
     * 处理反馈
     *
     * @param callback   反馈监听
     * @param observable Observable
     * @param response   反馈内容
     * @param t          错误
     * @param isSuccess  是否成功
     */
    private void dealWithCallback(Callback.Observable callback, Observable<T> observable, Response<T> response, Throwable t, boolean isSuccess) {

        if (callback == null)
            return;

        if (isSuccess) {
            callback.onResponse(observable, response);
        } else {
            callback.onFailure(observable, t);
        }

    }

    private org.sheedon.serial.Observable createRawObservable() throws IOException {
        Request request = serviceMethod.toRequest(args);
        org.sheedon.serial.Observable observable = serviceMethod.serialFactory.newObservable(request);
        if (observable == null) {
            throw new NullPointerException("Observable.SerialFactory returned null.");
        }
        return observable;
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

        org.sheedon.serial.Observable observable;
        synchronized (this) {
            observable = rawObservable;
        }
        if (observable != null) {
            observable.cancel();
        }
    }

    @Override
    public boolean isCanceled() {
        if (canceled) {
            return true;
        }
        synchronized (this) {
            return rawObservable != null && rawObservable.isCanceled();
        }
    }
}
