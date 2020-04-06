package org.sheedon.serial.retrofit;

import org.sheedon.serial.ResponseBody;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * 默认内置转换器
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2020/3/21 22:46
 */
public final class BuiltInConverters extends Converter.Factory {
    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations,
                                                            Retrofit retrofit) {
        if (type == ResponseBody.class) {
            return BufferingResponseBodyConverter.INSTANCE;
        }
        if (type == Void.class) {
            return VoidResponseBodyConverter.INSTANCE;
        }

//        final Type responseType = Utils.getCallResponseType(type);
        if (type != null) {
            return new ResponseBodyRulesConverter<>(type);
        }

        return null;
    }


    static final class VoidResponseBodyConverter implements Converter<ResponseBody, Void> {
        static final VoidResponseBodyConverter INSTANCE = new VoidResponseBodyConverter();

        @Override
        public Void convert(ResponseBody value) {
            value.close();
            return null;
        }
    }

    static final class BufferingResponseBodyConverter
            implements Converter<ResponseBody, ResponseBody> {
        static final BufferingResponseBodyConverter INSTANCE = new BufferingResponseBodyConverter();

        @Override
        public ResponseBody convert(ResponseBody value) {
            try {
                // Buffer the entire body to avoid future I/O.
                return value;
            } finally {
                value.close();
            }
        }
    }


    public static final class ToStringConverter implements Converter<Object, String> {
        static final ToStringConverter INSTANCE = new ToStringConverter();

        @Override
        public String convert(Object value) {
            return value.toString();
        }
    }


}
