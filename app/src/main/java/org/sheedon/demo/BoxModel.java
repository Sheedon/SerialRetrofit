package org.sheedon.demo;

import org.sheedon.serial.retrofit.serialport.RULES;

/**
 * @Description: java类作用描述
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2020/3/24 20:10
 */
public class BoxModel {

    @RULES(length = 2)
    private String name;
    @RULES(length = 2)
    private String age;
    @RULES(length = 6)
    private String other;

    @RULES(length = 2)
    private Body body;

    public String getName() {
        return name;
    }

    public String getAge() {
        return age;
    }

    public String getOther() {
        return other;
    }

    public Body getBody() {
        return body;
    }

    @Override
    public String toString() {
        return "BoxModel{" +
                "name='" + name + '\'' +
                ", age='" + age + '\'' +
                ", other='" + other + '\'' +
                ", body=" + body +
                '}';
    }

    private class Body{
        @RULES(length = 2)
        private String id;

        public String getId() {
            return id;
        }

        @Override
        public String toString() {
            return "Body{" +
                    "id='" + id + '\'' +
                    '}';
        }
    }
}
