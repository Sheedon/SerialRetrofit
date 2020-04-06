package org.sheedon.demo;

import android.app.Application;

/**
 * @Description: java类作用描述
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2020/3/9 22:42
 */
public class App extends Application {
    private static Application instance;

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;
    }

    public Application getInstance() {
        return instance;
    }
}
