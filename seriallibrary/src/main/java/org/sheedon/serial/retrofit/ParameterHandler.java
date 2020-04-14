package org.sheedon.serial.retrofit;


import androidx.annotation.Nullable;

import java.io.IOException;
import java.lang.reflect.Array;

import static org.sheedon.serial.retrofit.Utils.checkNotNull;


/**
 * 参数处理
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2020/3/21 23:07
 */
abstract class ParameterHandler<T> {
    abstract void apply(RequestBuilder builder, @Nullable T value) throws IOException;

    final ParameterHandler<Iterable<T>> iterable() {
        return new ParameterHandler<Iterable<T>>() {
            @Override
            void apply(RequestBuilder builder, @Nullable Iterable<T> values)
                    throws IOException {
                if (values == null) return; // Skip null values.

                for (T value : values) {
                    ParameterHandler.this.apply(builder, value);
                }
            }
        };
    }

    final ParameterHandler<Object> array() {
        return new ParameterHandler<Object>() {
            @Override
            void apply(RequestBuilder builder, @Nullable Object values) throws IOException {
                if (values == null) return; // Skip null values.

                for (int i = 0, size = Array.getLength(values); i < size; i++) {
                    //noinspection unchecked
                    ParameterHandler.this.apply(builder, (T) Array.get(values, i));
                }
            }
        };
    }

    /**
     * 参数地址拼接到消息体内
     */
    static final class Path<T> extends ParameterHandler<T> {
        private final String name;
        private final Converter<T, String> valueConverter;
        private final boolean encoded;

        Path(String name, Converter<T, String> valueConverter, boolean encoded) {
            this.name = checkNotNull(name, "name == null");
            this.valueConverter = valueConverter;
            this.encoded = encoded;
        }

        @Override
        void apply(RequestBuilder builder, @Nullable T value) {
            if (value == null) {
                throw new IllegalArgumentException(
                        "Path parameter \"" + name + "\" value must not be null.");
            }
            builder.addPathParam(name, valueConverter.convert(value), encoded);
        }
    }

    /**
     * 反馈地址拼接到消息体内
     */
    static final class BackPath<T> extends ParameterHandler<T> {
        private final String name;
        private final Converter<T, String> valueConverter;
        private final boolean encoded;

        BackPath(String name, Converter<T, String> valueConverter, boolean encoded) {
            this.name = checkNotNull(name, "name == null");
            this.valueConverter = valueConverter;
            this.encoded = encoded;
        }

        @Override
        void apply(RequestBuilder builder, @Nullable T value) {
            if (value == null) {
                throw new IllegalArgumentException(
                        "Path parameter \"" + name + "\" value must not be null.");
            }
            builder.addPathParam(name, valueConverter.convert(value), encoded);
        }
    }
}