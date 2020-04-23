package org.sheedon.serial.retrofit;

import org.sheedon.serial.ResponseBody;
import org.sheedon.serial.internal.CharsUtils;
import org.sheedon.serial.retrofit.serialport.RULES;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * 内容规则解析
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2020/3/25 20:48
 */
public class ResponseBodyRulesConverter<T> implements Converter<ResponseBody, T> {

    private Type type;

    public ResponseBodyRulesConverter(Type type) {
        this.type = type;
    }

    @Override
    public T convert(ResponseBody value) {
        if (type == null || value == null || value.getMessageBit() == null)
            return null;

        return parseTypeAndAssignment(value.getMessageBit(), type, null, false);
    }

    /**
     * 解析类型并且赋值
     */
    private static <T> T parseTypeAndAssignment(byte[] message, Type type, Object parentObj, boolean decode) {
        if (!(type instanceof Class))
            return null;

        Class cls = (Class) type;

        Object object = Utils.newInstance(cls, parentObj);

        if (object == null)
            return null;

        Field[] fields = cls.getDeclaredFields();
        for (Field field : fields) {
            if (field == null)
                continue;

            parseFieldParameter(message, object, field);
        }

        return (T) object;
    }


    /**
     * 解析字段参数
     *
     * @param object 实例类
     * @param field  字段
     */
    private static void parseFieldParameter(byte[] message, Object object, Field field) {
        Annotation[] annotations = field.getAnnotations();

        RULES rules = getFieldAnnotationRules(annotations);

        if (rules == null)
            return;

        int begin = rules.begin();
        int end = rules.end();
        byte[] value = rules.value();
        boolean decode = rules.decode();

        int length = end - begin;

        if (length <= 0 || message.length < end) {
            assignObject(field, object, value, decode);
            return;
        }
        assignObject(field, object, Arrays.copyOfRange(message, begin, end), decode);
    }

    /**
     * 赋值操作
     *
     * @param field   字段
     * @param object  实体类
     * @param message 需要赋值的内容
     * @param decode  是否需要解码
     */
    private static void assignObject(Field field, Object object, byte[] message, boolean decode) {
        field.setAccessible(true);

        Class<?> fieldType = field.getType();
        try {
            if (isPrimitive(fieldType)) {
                if (decode || (byte[].class != fieldType && Byte[].class != fieldType)) {
                    decode(field, fieldType, object, message);
                } else {
                    field.set(object, message);
                }
            } else {
                field.set(object, parseTypeAndAssignment(message, fieldType, object, decode));
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }


    }

    /**
     * 解码操作
     */
    private static void decode(Field field, Class<?> type, Object object, byte[] message) {
        try {
            if (String.class.equals(type)) {
                field.set(object, CharsUtils.byte2HexStrNotEmpty(message));
            } else if (short.class.equals(type) || Short.class == type) {
                field.set(object, ByteBuffer.wrap(message).order(ByteOrder.LITTLE_ENDIAN).getShort());
            } else if (long.class == type || Long.class == type) {
                field.set(object, ByteBuffer.wrap(message).order(ByteOrder.LITTLE_ENDIAN).getLong());
            } else if (int.class == type || Integer.class == type) {
                field.set(object, ByteBuffer.wrap(message).order(ByteOrder.LITTLE_ENDIAN).getInt());
            } else if (float.class == type || Float.class == type) {
                field.set(object, ByteBuffer.wrap(message).order(ByteOrder.LITTLE_ENDIAN).getFloat());
            } else if (double.class == type || Double.class == type) {
                field.set(object, ByteBuffer.wrap(message).order(ByteOrder.LITTLE_ENDIAN).getDouble());
            } else if (char.class == type || Character.class == type) {
                field.set(object, ByteBuffer.wrap(message).order(ByteOrder.LITTLE_ENDIAN).getChar());
            } else if (byte.class == type || Byte.class == type) {
                field.set(object, ByteBuffer.wrap(message).order(ByteOrder.LITTLE_ENDIAN).get());
            } else {
                field.set(object, message);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取字段注解规则
     *
     * @param annotations 注解集合
     * @return RULES
     */
    private static RULES getFieldAnnotationRules(Annotation[] annotations) {
        RULES rules = null;
        for (Annotation annotation : annotations) {
            RULES parseRules = parseFieldRulesAnnotation(annotation);
            if (parseRules == null) {
                continue;
            }

            if (rules != null) {
                throw new IllegalArgumentException("RULES annotations found, only one allowed.");
            }

            rules = parseRules;
        }
        return rules;
    }

    //解析方法注解
    private static RULES parseFieldRulesAnnotation(Annotation annotation) {
        if (annotation instanceof RULES) {
            return (RULES) annotation;
        }
        return null;
    }

    private static boolean isPrimitive(Class<?> type) {
        return String.class == type
                || short.class == type || Short.class == type
                || long.class == type || Long.class == type
                || int.class == type || Integer.class == type
                || float.class == type || Float.class == type
                || double.class == type || Double.class == type
                || char.class == type || Character.class == type
                || byte.class == type || Byte.class == type
                || boolean.class == type || Boolean.class == type
                || byte[].class == type || Byte[].class == type;
    }
}
