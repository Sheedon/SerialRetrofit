package org.sheedon.serial.retrofit;

import androidx.annotation.Nullable;

import org.sheedon.serial.ResponseBody;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * 数据转化器
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2020/3/21 22:41
 */
public interface Converter<F, T> {
    T convert(F value);

    /**
     * Creates {@link Converter} instances based on a type and target usage.
     */
    abstract class Factory {

        /**
         * 返回内容拼接处理器
         */
        public @Nullable
        Converter<ResponseBody, ?> responseBodyConverter(Type type,
                                                         Annotation[] annotations, Retrofit retrofit) {
            return null;
        }


        /**
         * 返回校验器生成的校验位数据结果
         */
        public @Nullable
        Converter<SerialMessage, String> requestParityBitConverter(Retrofit retrofit) {
            return null;
        }

        /**
         * 返回请求数据拼接组合结果
         */
        public @Nullable
        Converter<SerialMessage, String> requestBodyConverter(Retrofit retrofit) {
            return null;
        }


        /**
         * 数据组合暂不适用
         */
        public @Nullable
        Converter<?, String> stringConverter(Type type, Annotation[] annotations,
                                             Retrofit retrofit) {
            return null;
        }


        protected static Type getParameterUpperBound(int index, ParameterizedType type) {
            return Utils.getParameterUpperBound(index, type);
        }


        protected static Class<?> getRawType(Type type) {
            return Utils.getRawType(type);
        }
    }
}
