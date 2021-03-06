package org.sheedon.serial.retrofit;

import org.sheedon.serial.Request;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.concurrent.Executor;

/**
 * 反馈执行适配器工厂
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2020/2/22 17:28
 */
final class ExecutorCallAdapterFactory extends CallAdapter.Factory {
    final Executor callbackExecutor;

    ExecutorCallAdapterFactory(Executor callbackExecutor) {
        this.callbackExecutor = callbackExecutor;
    }

    @Override
    public CallAdapter<?, ?> get(Type returnType, Annotation[] annotations, Retrofit retrofit) {
        final Class<?> rawType = getRawType(returnType);
        if (rawType != Call.class && rawType != Observable.class) {
            return null;
        }

        final Type responseType = Utils.getCallResponseType(returnType);
        return new CallAdapter<Object, Object>() {
            @Override
            public Type rawType() {
                return rawType;
            }

            @Override
            public Type responseType() {
                return responseType;
            }

            @Override
            public Call<Object> adapt(Call<Object> call) {
                return new ExecutorCallbackCall<>(callbackExecutor, call);
            }

            @Override
            public Observable<Object> adapt(Observable<Object> observable) {
                return new ExecutorCallbackObservable<>(callbackExecutor, observable);
            }
        };
    }

    static final class ExecutorCallbackCall<T> implements Call<T> {
        final Executor callbackExecutor;
        final Call<T> delegate;

        ExecutorCallbackCall(Executor callbackExecutor, Call<T> delegate) {
            this.callbackExecutor = callbackExecutor;
            this.delegate = delegate;
        }

        @Override
        public boolean isExecuted() {
            return delegate.isExecuted();
        }

        @Override
        public void cancel() {
            delegate.cancel();
        }

        @Override
        public boolean isCanceled() {
            return delegate.isCanceled();
        }

        @SuppressWarnings("CloneDoesntCallSuperClone") // Performing deep clone.
        @Override
        public Call<T> clone() {
            return new ExecutorCallbackCall<>(callbackExecutor, delegate.clone());
        }

        @Override
        public Request request() {
            return delegate.request();
        }

        @Override
        public void publishNotCallback() {
            enqueue(null);
        }

        @Override
        public void enqueue(final Callback.Call callback) {
//            checkNotNull(callback, "callback == null");

            if (callback == null) {
                delegate.publishNotCallback();
                return;
            }

            delegate.enqueue(new Callback.Call<T>() {
                @Override
                public void onResponse(Call<T> call, final Response<T> response) {
                    callbackExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            if (delegate.isCanceled()) {
                                // Emulate OkHttp's behavior of throwing/delivering an IOException on cancellation.
                                callback.onFailure(ExecutorCallbackCall.this, new IOException("Canceled"));
                            } else {
                                callback.onResponse(ExecutorCallbackCall.this, response);
                            }
                        }
                    });
                }

                @Override
                public void onFailure(Call<T> call, final Throwable t) {
                    callbackExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            callback.onFailure(ExecutorCallbackCall.this, t);
                        }
                    });
                }
            });
        }
    }

    static final class ExecutorCallbackObservable<T> implements Observable<T> {
        final Executor callbackExecutor;
        final Observable<T> delegate;

        ExecutorCallbackObservable(Executor callbackExecutor, Observable<T> delegate) {
            this.callbackExecutor = callbackExecutor;
            this.delegate = delegate;
        }

        @Override
        public boolean isExecuted() {
            return delegate.isExecuted();
        }

        @Override
        public void cancel() {
            delegate.cancel();
        }

        @Override
        public boolean isCanceled() {
            return delegate.isCanceled();
        }

        @SuppressWarnings("CloneDoesntCallSuperClone") // Performing deep clone.
        @Override
        public Observable<T> clone() {
            return new ExecutorCallbackObservable<>(callbackExecutor, delegate.clone());
        }

        @Override
        public Request request() {
            return delegate.request();
        }

        @Override
        public void subscribe(final Callback.Observable<T> callback) {
            if (callback == null) {
                return;
            }

            delegate.subscribe(new Callback.Observable<T>() {

                @Override
                public void onResponse(Observable<T> call, final Response<T> response) {
                    callbackExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            if (delegate.isCanceled()) {
                                // Emulate OkHttp's behavior of throwing/delivering an IOException on cancellation.
                                callback.onFailure(ExecutorCallbackObservable.this, new IOException("Canceled"));
                            } else {
                                callback.onResponse(ExecutorCallbackObservable.this, response);
                            }
                        }
                    });
                }

                @Override
                public void onFailure(Observable<T> call, final Throwable t) {
                    callbackExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            callback.onFailure(ExecutorCallbackObservable.this, t);
                        }
                    });
                }

            });
        }
    }
}
