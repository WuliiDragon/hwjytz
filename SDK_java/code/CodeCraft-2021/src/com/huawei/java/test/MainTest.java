package com.huawei.java.test;

import com.huawei.java.main.Main;

public class MainTest {

    public static void mainTest() throws CloneNotSupportedException {
        Main.isMatch = false;//读取本地数据文件
        Main.main(null);
    }
}