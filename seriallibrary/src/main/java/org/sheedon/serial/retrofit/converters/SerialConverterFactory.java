package org.sheedon.serial.retrofit.converters;


import androidx.annotation.Nullable;

import org.sheedon.serial.retrofit.Converter;
import org.sheedon.serial.retrofit.Retrofit;
import org.sheedon.serial.retrofit.SerialMessage;

/**
 * 串口转化工厂
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
