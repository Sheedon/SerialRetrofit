package org.sheedon.demo.converters;

import androidx.annotation.Nullable;

import org.sheedon.serial.retrofit.Converter;
import org.sheedon.serial.retrofit.Retrofit;
import org.sheedon.serial.retrofit.SerialMessage;
import org.sheedon.serial.retrofit.converters.RequestBodyConverter;

/**
 * java类作用描述
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2020/4/20 17:11
 */
public class SerialConverterFactory extends Converter.Factory {

    // Guarding public API nullability.
    public static SerialConverterFactory create() {
        return new SerialConverterFactory();
    }

    private SerialConverterFactory() {

    }


    @Nullable
    @Override
    public Converter<SerialMessage, String> requestBodyConverter(Retrofit retrofit) {
        return new RequestBodyConverter();
    }

    @Nullable
    @Override
    public Converter<SerialMessage, String> requestParityBitConverter(Retrofit retrofit) {
        return new RequestParityBitConverter();
    }
}