package org.sheedon.serial.retrofit;

import org.sheedon.serial.ResponseBody;
import org.sheedon.serial.retrofit.serialport.RULES;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Type;

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

        return parseTypeAndAssignment(value.getMessageBit(), type, null);
    }

    /**
     * 解析类型并且赋值
     */
    private static <T> T parseTypeAndAssignment(String message, Type type, Object parentObj) {
        if (!(type instanceof Class))
            return null;

        Class cls = (Class) type;

        Object object = Utils.newInstance(cls,parentObj);

        if (object == null)
            return null;

        int position = 0;

        Field[] fields = cls.getDeclaredFields();
        for (Field field : fields) {
            if (field == null)
                continue;

            int cutLength = parseFieldParameter(message.substring(position), object, field);
            position += cutLength;
        }

        return (T) object;
    }


    /**
     * 解析字段参数
     *
     * @param object 实例类
     * @param field  字段
     */
    private static int parseFieldParameter(String message, Object object, Field field) {
        Annotation[] annotations = field.getAnnotations();

        RULES rules = getFieldAnnotationRules(annotations);

        if (rules == null)
            return 0;

        int length = rules.length();
        String value = rules.value();

        if (length <= 0 || message.length() < length) {
            assignObject(field, object, value);
            return message.length();
        }

        assignObject(field, object, message.substring(0, length));
        return length;
    }

    /**
     * 赋值操作
     *
     * @param field   字段
     * @param object  实体类
     * @param message 需要赋值的内容
     */
    private static void assignObject(Field field, Object object, String message) {
        field.setAccessible(true);

        Class<?> fieldType = field.getType();
        try {
            if (isPrimitive(fieldType)) {
                field.set(object, message);
            } else {
                field.set(object, parseTypeAndAssignment(message, fieldType, object));
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
                || boolean.class == type || Boolean.class == type;
    }
}
