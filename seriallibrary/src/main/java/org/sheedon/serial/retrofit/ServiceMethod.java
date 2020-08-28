package org.sheedon.serial.retrofit;

import androidx.annotation.Nullable;

import org.sheedon.serial.Request;
import org.sheedon.serial.Response;
import org.sheedon.serial.ResponseBody;
import org.sheedon.serial.retrofit.serialport.BACKNAME;
import org.sheedon.serial.retrofit.serialport.BackPath;
import org.sheedon.serial.retrofit.serialport.DELAYMILLISECOND;
import org.sheedon.serial.retrofit.serialport.ENDBIT;
import org.sheedon.serial.retrofit.serialport.MESSAGEBIT;
import org.sheedon.serial.retrofit.serialport.PARITYBIT;
import org.sheedon.serial.retrofit.serialport.Path;
import org.sheedon.serial.retrofit.serialport.STARTBIT;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * 动态代理方法数据获取
 * 获取内容
 * 1. 方法体注解
 * 2. 方法体反馈格式
 * 3. 方法体参数
 * 3.1 方法体参数注解
 * 3.2 方法体参数类型
 * <p>
 * 例如：
 *
 * @STARTBIT("7A")
 * @MESSAGEBIT("{length}{address}{type}{message}")
 * @BACKNAME("0101") Call<String> getManagerList(@Path("length") String length,
 * @Path("address") String address,
 * @Path("type") String type,
 * @Path("message") String message);
 * <p>
 * 方法注解
 * @STARTBIT("7A")
 * @MESSAGEBIT("{length}{address}{type}{message}")
 * @BACKNAME("0101") ...
 * <p>
 * 方法体反馈格式
 * Call<String> 中的 String
 * <p>
 * 方法体参数注解
 * @Path("length")
 * @Path("address")
 * @Path("type")
 * @Path("message") 方法体参数类型
 * String length
 * String address
 * String type
 * String message
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2020/3/22 21:44
 */
final class ServiceMethod<R, T> {

    final org.sheedon.serial.SerialFactory serialFactory;
    final CallAdapter<R, T> callAdapter;

    private final Converter<ResponseBody, R> responseConverter;
    private final Converter<SerialMessage, String> parityBitConverter;
    private final Converter<SerialMessage, String> requestBodyConverter;
    private final ParameterHandler<?>[] parameterHandlers;
    private final BindCallback bindCallback;
    private final SerialMessage serialMessage;

    ServiceMethod(Builder<R, T> builder) {
        this.serialFactory = builder.retrofit.serialFactory();
        this.callAdapter = builder.callAdapter;
        this.responseConverter = builder.responseConverter;
        this.parityBitConverter = builder.parityBitConverter;
        this.requestBodyConverter = builder.requestBodyConverter;
        this.parameterHandlers = builder.parameterHandlers;
        this.bindCallback = builder.bindCallback;
        this.serialMessage = builder.serialMessage;
    }

    Request toRequest(@Nullable Object... args) throws IOException {

        RequestBuilder requestBuilder = new RequestBuilder(this, serialMessage.clone(),
                bindCallback);

        @SuppressWarnings("unchecked") // It is an error to invoke a method with the wrong arg types.
                ParameterHandler<Object>[] handlers = (ParameterHandler<Object>[]) parameterHandlers;

        int argumentCount = args != null ? args.length : 0;
        if (argumentCount != handlers.length) {
            throw new IllegalArgumentException("Argument count (" + argumentCount
                    + ") doesn't match expected count (" + handlers.length + ")");
        }

        for (int p = 0; p < argumentCount; p++) {
            handlers[p].apply(requestBuilder, args[p]);
        }

        return requestBuilder.build();
    }

    T adapt(Call<R> call) {
        return callAdapter.adapt(call);
    }

    T adapt(Observable<R> observable) {
        return callAdapter.adapt(observable);
    }

    /**
     * Builds a method return value from an Serial response body.
     */
    R toResponse(ResponseBody body) {
        return responseConverter.convert(body);
    }

    /**
     * Builds a method return value from an Serial request parity.
     */
    String toParityBit(SerialMessage serialMessage) {
        return parityBitConverter.convert(serialMessage);
    }

    String toRequestBody(SerialMessage serialMessage) {
        return requestBodyConverter.convert(serialMessage);
    }

    static final class Builder<T, R> {
        final Retrofit retrofit;
        final Method method;
        final Annotation[] methodAnnotations;
        final Annotation[][] parameterAnnotationsArray;
        final Type[] parameterTypes;

        // 反馈绑定信息，用于填充延迟时间和反馈名
        final BindCallback bindCallback;
        // 串口消息体，包含内容 【起始位】、【消息位】、【校验位】、【停止位】
        final SerialMessage serialMessage;


        Type responseType;
        ParameterHandler<?>[] parameterHandlers;
        // 回调转化器
        Converter<ResponseBody, T> responseConverter;
        // 校验位转化器
        Converter<SerialMessage, String> parityBitConverter;
        // 请求数据合成转化器
        Converter<SerialMessage, String> requestBodyConverter;
        // 反馈适配器
        CallAdapter<T, R> callAdapter;


        /**
         * 构造器，生成初始数据
         *
         * @param retrofit 传入的Retrofit
         * @param method   方法
         */
        Builder(Retrofit retrofit, Method method) {
            this.retrofit = retrofit;
            this.method = method;
            this.methodAnnotations = method.getAnnotations();
            this.parameterTypes = method.getGenericParameterTypes();
            this.parameterAnnotationsArray = method.getParameterAnnotations();

            this.serialMessage = SerialMessage.build(retrofit.baseStartBit, retrofit.baseEndBit);
            this.bindCallback = new BindCallback();
        }


        /**
         * 构建动态代理服务方法
         */
        ServiceMethod build() {
            callAdapter = createCallAdapter();
            responseType = callAdapter.responseType();
            if (responseType == Response.class) {
                throw methodError("'"
                        + Utils.getRawType(responseType).getName()
                        + "' is not a valid response body type. Did you mean ResponseBody?");
            }

            responseConverter = createResponseConverter();
            parityBitConverter = createParityBitConverter();
            requestBodyConverter = createRequestBodyConverter();

            for (Annotation annotation : methodAnnotations) {
                parseMethodAnnotation(annotation);
            }

            int parameterCount = parameterAnnotationsArray.length;
            parameterHandlers = new ParameterHandler[parameterCount];
            for (int p = 0; p < parameterCount; p++) {
                Type parameterType = parameterTypes[p];
                if (Utils.hasUnresolvableType(parameterType)) {
                    throw parameterError(p, "Parameter type must not include a type variable or wildcard: %s",
                            parameterType);
                }

                Annotation[] parameterAnnotations = parameterAnnotationsArray[p];
                if (parameterAnnotations == null) {
                    throw parameterError(p, "No Retrofit annotation found.");
                }

                parameterHandlers[p] = parseParameter(p, parameterType, parameterAnnotations);
            }

            return new ServiceMethod<>(this);
        }

        /**
         * 创建反馈适配器
         * 一般采用默认配置，目的是获取Call<T>内的泛型类
         */
        private CallAdapter<T, R> createCallAdapter() {
            Type returnType = method.getGenericReturnType();
            if (Utils.hasUnresolvableType(returnType)) {
                throw methodError(
                        "Method return type must not include a type variable or wildcard: %s", returnType);
            }
            if (returnType == void.class) {
                throw methodError("Service methods cannot return void.");
            }
            Annotation[] annotations = method.getAnnotations();
            try {
                //noinspection unchecked
                return (CallAdapter<T, R>) retrofit.callAdapter(returnType, annotations);
            } catch (RuntimeException e) { // Wide exception range because factories are user code.
                throw methodError(e, "Unable to create call adapter for %s", returnType);
            }
        }


        /**
         * 创建反馈转化器
         * 用于将默认的数据转成我们自己配置的实体类内容
         */
        private Converter<ResponseBody, T> createResponseConverter() {
            Annotation[] annotations = method.getAnnotations();
            try {
                return retrofit.responseBodyConverter(responseType, annotations);
            } catch (RuntimeException e) { // Wide exception range because factories are user code.
                throw methodError(e, "Unable to create converter for %s", responseType);
            }
        }

        /**
         * 创建校验位转化器，
         * 需要开发者自己配置校验生成逻辑
         */
        private Converter<SerialMessage, String> createParityBitConverter() {
            try {
                return retrofit.requestParityBitConverter();
            } catch (RuntimeException e) { // Wide exception range because factories are user code.
                throw methodError(e, "Unable to create converter for %s", responseType);
            }
        }

        /**
         * 创建请求内容生成器
         * 由开发者自己规定组合模式
         */
        private Converter<SerialMessage, String> createRequestBodyConverter() {
            try {
                return retrofit.requestBodyConverter();
            } catch (RuntimeException e) { // Wide exception range because factories are user code.
                throw methodError(e, "Unable to create converter for %s", responseType);
            }
        }

        //解析方法注解
        private void parseMethodAnnotation(Annotation annotation) {
            if (annotation instanceof STARTBIT) {
                serialMessage.setStartBit(((STARTBIT) annotation).value());
            } else if (annotation instanceof MESSAGEBIT) {
                serialMessage.setMessageBit(((MESSAGEBIT) annotation).value());
            } else if (annotation instanceof PARITYBIT) {
                serialMessage.setParityBit(((PARITYBIT) annotation).value());
            } else if (annotation instanceof ENDBIT) {
                serialMessage.setEndBit(((ENDBIT) annotation).value());
            } else if (annotation instanceof DELAYMILLISECOND) {
                bindCallback.setDelayMilliSecond(((DELAYMILLISECOND) annotation).value());
            } else if (annotation instanceof BACKNAME) {
                bindCallback.setBackName(((BACKNAME) annotation).value());
            }
        }


        /**
         * 解析方法体内的参数
         * 1个参数只能有一个注解
         *
         * @param p             下标
         * @param parameterType 参数类型
         * @param annotations   注解
         * @return ParameterHandler
         */
        private ParameterHandler<?> parseParameter(
                int p, Type parameterType, Annotation[] annotations) {
            ParameterHandler<?> result = null;
            for (Annotation annotation : annotations) {
                ParameterHandler<?> annotationAction = parseParameterAnnotation(
                        p, parameterType, annotations, annotation);

                if (annotationAction == null) {
                    continue;
                }

                if (result != null) {
                    throw parameterError(p, "Multiple Retrofit annotations found, only one allowed.");
                }

                result = annotationAction;
            }

            if (result == null) {
                throw parameterError(p, "No Retrofit annotation found.");
            }

            return result;
        }

        /**
         * 解析参数注解
         *
         * @param p           下标
         * @param type        类型
         * @param annotations 注解
         * @param annotation  注解项
         * @return ParameterHandler
         */
        private ParameterHandler<?> parseParameterAnnotation(
                int p, Type type, Annotation[] annotations, Annotation annotation) {
            if (annotation instanceof Path) {
                Path path = (Path) annotation;
                String name = path.value();
//                validatePathName(p, name);

                Converter<?, String> converter = retrofit.stringConverter(type, annotations);
                return new ParameterHandler.Path<>(name, converter, path.encoded());
            } else if (annotation instanceof BackPath) {
                BackPath path = (BackPath) annotation;
                String name = path.value();
                Converter<?, String> converter = retrofit.stringConverter(type, annotations);
                return new ParameterHandler.Path<>(name, converter, path.encoded());
            }
            return null; // Not a Retrofit annotation.
        }


        private RuntimeException methodError(String message, Object... args) {
            return methodError(null, message, args);
        }

        private RuntimeException methodError(Throwable cause, String message, Object... args) {
            message = String.format(message, args);
            return new IllegalArgumentException(message
                    + "\n    for method "
                    + method.getDeclaringClass().getSimpleName()
                    + "."
                    + method.getName(), cause);
        }

        private RuntimeException parameterError(
                Throwable cause, int p, String message, Object... args) {
            return methodError(cause, message + " (parameter #" + (p + 1) + ")", args);
        }

        private RuntimeException parameterError(int p, String message, Object... args) {
            return methodError(message + " (parameter #" + (p + 1) + ")", args);
        }
    }

    static Class<?> boxIfPrimitive(Class<?> type) {
        if (boolean.class == type) return Boolean.class;
        if (byte.class == type) return Byte.class;
        if (char.class == type) return Character.class;
        if (double.class == type) return Double.class;
        if (float.class == type) return Float.class;
        if (int.class == type) return Integer.class;
        if (long.class == type) return Long.class;
        if (short.class == type) return Short.class;
        return type;
    }
}
