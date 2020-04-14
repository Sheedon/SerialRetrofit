package org.sheedon.serial.retrofit;


import androidx.annotation.Nullable;

import org.sheedon.serial.ResponseBody;
import org.sheedon.serial.SerialClient;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

import static java.util.Collections.unmodifiableList;
import static org.sheedon.serial.retrofit.Utils.checkNotNull;

/**
 * 数据重构组合
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2020/3/9 22:40
 */
public class Retrofit {

    private final Map<Method, ServiceMethod<?, ?>> serviceMethodCache = new ConcurrentHashMap<>();

    final org.sheedon.serial.SerialFactory serialFactory;
    final String baseStartBit;
    final String baseEndBit;
    final List<Converter.Factory> converterFactories;
    final List<CallAdapter.Factory> callAdapterFactories;
    final @Nullable
    Executor callbackExecutor;
    final boolean validateEagerly;

    Retrofit(org.sheedon.serial.SerialFactory serialFactory,
             String baseStartBit, String baseEndBit,
             List<Converter.Factory> converterFactories,
             List<CallAdapter.Factory> adapterFactories,
             @Nullable Executor callbackExecutor, boolean validateEagerly) {
        this.serialFactory = serialFactory;
        this.baseStartBit = baseStartBit;
        this.baseEndBit = baseEndBit;
        this.converterFactories = unmodifiableList(converterFactories);
        this.callAdapterFactories = unmodifiableList(adapterFactories); // Defensive copy at call site.
        this.callbackExecutor = callbackExecutor;
        this.validateEagerly = validateEagerly;

    }

    /**
     * 创建由{@code service}接口定义的API端点的实现。
     */
    public <T> T create(final Class<T> service) {
        Utils.validateServiceInterface(service);

        return (T) Proxy.newProxyInstance(service.getClassLoader(), new Class<?>[]{service},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args)
                            throws Throwable {

                        // If the method is a method from Object then defer to normal invocation.
                        if (method.getDeclaringClass() == Object.class) {
                            return method.invoke(this, args);
                        }

                        ServiceMethod<Object, Object> serviceMethod =
                                (ServiceMethod<Object, Object>) loadServiceMethod(method);
                        return adapt(serviceMethod, args);
                    }
                });

    }

    public org.sheedon.serial.SerialFactory serialFactory() {
        return serialFactory;
    }

    private void eagerlyValidateMethods(Class<?> service) {
        Platform platform = Platform.get();
        for (Method method : service.getDeclaredMethods()) {
            if (!platform.isDefaultMethod(method)) {
                loadServiceMethod(method);
            }
        }
    }

    /**
     * 加载动态代理方法
     *
     * @param method 方法
     * @return ServiceMethod
     */
    private ServiceMethod<?, ?> loadServiceMethod(Method method) {
        ServiceMethod<?, ?> result = serviceMethodCache.get(method);
        if (result != null) return result;

        synchronized (serviceMethodCache) {
            result = serviceMethodCache.get(method);
            if (result == null) {
                result = new ServiceMethod.Builder<>(this, method).build();
                serviceMethodCache.put(method, result);
            }
        }
        return result;
    }

    private <T> T adapt(ServiceMethod<Object, Object> serviceMethod, @Nullable Object[] args){
        if (serviceMethod.callAdapter.rawType() == Call.class) {
            return (T) serviceMethod.adapt(new SerialCall<>(serviceMethod, args));
        }else if(serviceMethod.callAdapter.rawType() == Observable.class){
            return (T) serviceMethod.adapt(new SerialObservable<>(serviceMethod, args));
        }
        return null;
    }

    /**
     * 反馈适配器
     *
     * @param returnType  反馈类型
     * @param annotations 注解
     * @return CallAdapter
     */
    CallAdapter<?, ?> callAdapter(Type returnType, Annotation[] annotations) {
        return nextCallAdapter(null, returnType, annotations);
    }

    /**
     * 通过反馈适配器工厂拿到可用的适配器
     * 例如默认的Call<Model> 为了提取 Model 数据内容
     *
     * @param skipPast    坐标
     * @param returnType  反馈类型
     * @param annotations 注解
     * @return CallAdapter
     */
    private CallAdapter<?, ?> nextCallAdapter(@Nullable CallAdapter.Factory skipPast, Type returnType,
                                              Annotation[] annotations) {
        checkNotNull(returnType, "returnType == null");
        checkNotNull(annotations, "annotations == null");

        int start = callAdapterFactories.indexOf(skipPast) + 1;
        for (int i = start, count = callAdapterFactories.size(); i < count; i++) {
            CallAdapter<?, ?> adapter = callAdapterFactories.get(i).get(returnType, annotations, this);
            if (adapter != null) {
                return adapter;
            }
        }

        StringBuilder builder = new StringBuilder("Could not locate call adapter for ")
                .append(returnType)
                .append(".\n");
        if (skipPast != null) {
            builder.append("  Skipped:");
            for (int i = 0; i < start; i++) {
                builder.append("\n   * ").append(callAdapterFactories.get(i).getClass().getName());
            }
            builder.append('\n');
        }
        builder.append("  Tried:");
        for (int i = start, count = callAdapterFactories.size(); i < count; i++) {
            builder.append("\n   * ").append(callAdapterFactories.get(i).getClass().getName());
        }
        throw new IllegalArgumentException(builder.toString());
    }

    /**
     * 反馈数据包的转化处理
     *
     * @param type        类型
     * @param annotations 注解
     * @param <T>         反馈格式
     */
    <T> Converter<ResponseBody, T> responseBodyConverter(Type type, Annotation[] annotations) {
        return nextResponseBodyConverter(null, type, annotations);
    }

    /**
     * 轮询查找可执行的结果包处理
     *
     * @param skipPast    坐标
     * @param type        类型
     * @param annotations 注解
     * @param <T>         反馈类型格式
     */
    private <T> Converter<ResponseBody, T> nextResponseBodyConverter(
            @Nullable Converter.Factory skipPast, Type type, Annotation[] annotations) {
        checkNotNull(type, "type == null");
        checkNotNull(annotations, "annotations == null");

        int start = converterFactories.indexOf(skipPast) + 1;
        for (int i = start, count = converterFactories.size(); i < count; i++) {
            Converter<ResponseBody, ?> converter =
                    converterFactories.get(i).responseBodyConverter(type, annotations, this);
            if (converter != null) {
                //noinspection unchecked
                return (Converter<ResponseBody, T>) converter;
            }
        }

        StringBuilder builder = new StringBuilder("Could not locate ResponseBody converter for ")
                .append(type)
                .append(".\n");
        if (skipPast != null) {
            builder.append("  Skipped:");
            for (int i = 0; i < start; i++) {
                builder.append("\n   * ").append(converterFactories.get(i).getClass().getName());
            }
            builder.append('\n');
        }
        builder.append("  Tried:");
        for (int i = start, count = converterFactories.size(); i < count; i++) {
            builder.append("\n   * ").append(converterFactories.get(i).getClass().getName());
        }
        throw new IllegalArgumentException(builder.toString());
    }

    /**
     * 获取转化工厂集合
     */
    private List<Converter.Factory> converterFactories() {
        return converterFactories;
    }

    /**
     * Returns a {@link Converter} for {@code type} to {@link String} from the available
     * {@linkplain #converterFactories() factories}.
     */
    <T> Converter<T, String> stringConverter(Type type, Annotation[] annotations) {
        checkNotNull(type, "type == null");
        checkNotNull(annotations, "annotations == null");

        for (int i = 0, count = converterFactories.size(); i < count; i++) {
            Converter<?, String> converter =
                    converterFactories.get(i).stringConverter(type, annotations, this);
            if (converter != null) {
                //noinspection unchecked
                return (Converter<T, String>) converter;
            }
        }

        // Nothing matched. Resort to default converter which just calls toString().
        //noinspection unchecked
        return (Converter<T, String>) BuiltInConverters.ToStringConverter.INSTANCE;
    }

    /**
     * 反馈请求校验码转化适配器
     */
    Converter<SerialMessage, String> requestParityBitConverter() {
        return nextParityBitConverter(null);
    }

    /**
     * 轮询查找获取校验码处理适配器
     *
     * @param skipPast 坐标
     */
    private Converter<SerialMessage, String> nextParityBitConverter(
            @Nullable Converter.Factory skipPast) {

        int start = converterFactories.indexOf(skipPast) + 1;
        for (int i = start, count = converterFactories.size(); i < count; i++) {
            Converter.Factory factory = converterFactories.get(i);
            Converter<SerialMessage, String> converter =
                    factory.requestParityBitConverter(this);
            if (converter != null) {
                //noinspection unchecked
                return converter;
            }
        }

        StringBuilder builder = new StringBuilder("Could not locate ParityBit converter for ")
                .append(".\n");
        if (skipPast != null) {
            builder.append("  Skipped:");
            for (int i = 0; i < start; i++) {
                builder.append("\n   * ").append(converterFactories.get(i).getClass().getName());
            }
            builder.append('\n');
        }
        builder.append("  Tried:");
        for (int i = start, count = converterFactories.size(); i < count; i++) {
            builder.append("\n   * ").append(converterFactories.get(i).getClass().getName());
        }
        throw new IllegalArgumentException(builder.toString());
    }

    /**
     * 请求包数据拼接处理器
     */
    Converter<SerialMessage, String> requestBodyConverter() {
        return nextRequestBodyConverter(null);
    }

    /**
     * 轮询查找请求内容凭借处理适配器
     *
     * @param skipPast 坐标
     */
    private Converter<SerialMessage, String> nextRequestBodyConverter(
            @Nullable Converter.Factory skipPast) {

        int start = converterFactories.indexOf(skipPast) + 1;
        for (int i = start, count = converterFactories.size(); i < count; i++) {
            Converter.Factory factory = converterFactories.get(i);
            Converter<SerialMessage, String> converter =
                    factory.requestBodyConverter(this);
            if (converter != null) {
                //noinspection unchecked
                return converter;
            }
        }

        StringBuilder builder = new StringBuilder("Could not locate RequestBody converter for ")
                .append(".\n");
        if (skipPast != null) {
            builder.append("  Skipped:");
            for (int i = 0; i < start; i++) {
                builder.append("\n   * ").append(converterFactories.get(i).getClass().getName());
            }
            builder.append('\n');
        }
        builder.append("  Tried:");
        for (int i = start, count = converterFactories.size(); i < count; i++) {
            builder.append("\n   * ").append(converterFactories.get(i).getClass().getName());
        }
        throw new IllegalArgumentException(builder.toString());
    }

    public static final class Builder {
        private final Platform platform;
        private @Nullable
        org.sheedon.serial.SerialFactory serialFactory;
        private final List<Converter.Factory> converterFactories = new ArrayList<>();
        private final List<CallAdapter.Factory> callAdapterFactories = new ArrayList<>();
        private @Nullable
        Executor callbackExecutor;
        private boolean validateEagerly;

        // 默认起始位
        private String baseStartBit;
        // 默认结束位
        private String baseEndBit;

        Builder(Platform platform) {
            this.platform = platform;
        }

        public Builder() {
            this(Platform.get());
        }

        Builder(Retrofit retrofit) {
            platform = Platform.get();
            serialFactory = retrofit.serialFactory;
            baseStartBit = retrofit.baseStartBit;
            baseEndBit = retrofit.baseEndBit;

            converterFactories.addAll(retrofit.converterFactories);
            // Remove the default BuiltInConverters instance added by build().
            converterFactories.remove(0);

            callAdapterFactories.addAll(retrofit.callAdapterFactories);
            // Remove the default, platform-aware call adapter added by build().
            callAdapterFactories.remove(callAdapterFactories.size() - 1);

            callbackExecutor = retrofit.callbackExecutor;
            validateEagerly = retrofit.validateEagerly;
        }

        /**
         * The Serial client used for requests.
         * <p>
         * This is a convenience method for calling {@link #serialFactory}.
         */
        public Builder client(SerialClient client) {
            return serialFactory(checkNotNull(client, "client == null"));
        }

        /**
         * Specify a custom call factory for creating {@link Call} instances.
         * <p>
         * Note: Calling {@link #client} automatically sets this value.
         */
        public Builder serialFactory(org.sheedon.serial.SerialFactory factory) {
            this.serialFactory = checkNotNull(factory, "factory == null");
            return this;
        }

        /**
         * Set the API base StartBit.
         */
        public Builder baseStartBit(String baseStartBit) {
            if (baseStartBit == null)
                baseStartBit = "";
            this.baseStartBit = baseStartBit;
            return this;
        }

        /**
         * Set the API base EndBit.
         */
        public Builder baseEndBit(String baseEndBit) {
            if (baseEndBit == null)
                baseEndBit = "";
            this.baseEndBit = baseEndBit;
            return this;
        }


        /**
         * Add converter factory for serialization and deserialization of objects.
         */
        public Builder addConverterFactory(Converter.Factory factory) {
            converterFactories.add(checkNotNull(factory, "factory == null"));
            return this;
        }

        /**
         * Add a call adapter factory for supporting service method return types other than {@link
         * Call}.
         */
        public Builder addCallAdapterFactory(CallAdapter.Factory factory) {
            callAdapterFactories.add(checkNotNull(factory, "factory == null"));
            return this;
        }

        /**
         * The executor on which {@link Callback} methods are invoked when returning {@link Call} from
         * your service method.
         * <p>
         * Note: {@code executor} is not used for {@linkplain #addCallAdapterFactory custom method
         * return types}.
         */
        public Builder callbackExecutor(Executor executor) {
            this.callbackExecutor = checkNotNull(executor, "executor == null");
            return this;
        }

        /**
         * Returns a modifiable list of call adapter factories.
         */
        public List<CallAdapter.Factory> callAdapterFactories() {
            return this.callAdapterFactories;
        }

        /**
         * Returns a modifiable list of converter factories.
         */
        public List<Converter.Factory> converterFactories() {
            return this.converterFactories;
        }

        /**
         * When calling {@link #create} on the resulting {@link Retrofit} instance, eagerly validate
         * the configuration of all methods in the supplied interface.
         */
        public Builder validateEagerly(boolean validateEagerly) {
            this.validateEagerly = validateEagerly;
            return this;
        }


        public Retrofit build() {

            if (baseStartBit == null)
                baseStartBit = "";

            if (baseEndBit == null)
                baseEndBit = "";

            org.sheedon.serial.SerialFactory serialFactory = this.serialFactory;
            if (serialFactory == null) {
                //
                throw new IllegalStateException("serialFactory is null.");
            }

            Executor callbackExecutor = this.callbackExecutor;
            if (callbackExecutor == null) {
                callbackExecutor = platform.defaultCallbackExecutor();
            }

            // Make a defensive copy of the adapters and add the default Call adapter.
            List<CallAdapter.Factory> callAdapterFactories = new ArrayList<>(this.callAdapterFactories);
            callAdapterFactories.add(platform.defaultCallAdapterFactory(callbackExecutor));

            // Make a defensive copy of the converters.
            List<Converter.Factory> converterFactories =
                    new ArrayList<>(1 + this.converterFactories.size());

            // Add the built-in converter factory first. This prevents overriding its behavior but also
            // ensures correct behavior when using converters that consume all types.
            converterFactories.add(new BuiltInConverters());
            converterFactories.addAll(this.converterFactories);

            return new Retrofit(serialFactory, baseStartBit, baseEndBit,
                    unmodifiableList(converterFactories),
                    unmodifiableList(callAdapterFactories), callbackExecutor, validateEagerly);
        }
    }

}
