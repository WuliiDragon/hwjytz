package com.huawei.java.test;

import com.huawei.java.main.Main;
import com.huawei.java.main.entity.Server;
import com.huawei.java.main.util.Utils;

import java.util.ArrayList;
import java.util.List;

import static com.huawei.java.main.util.Cost.calculateDayCost;

/**
 * @author Lofit .LaeL
 * @email GreatLandmark@outlook.com
 * @date 2021/3/13
 */

// 用于测试工程
public class Test {
    public static List<String> timeCost = new ArrayList<>();

    public static void main(String[] args) {


        for (int i = 0; i < 2; i++) {

            StringBuilder sb = new StringBuilder(Utils.buildPath);
            sb.append(i + 1 + ".txt");
            Main.path = String.valueOf(sb);

            long start = System.currentTimeMillis();
            try {
                Main.isMatch = false;
                Main.main(null);
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
            long end = System.currentTimeMillis();
//        System.out.println("*** Time *** Total : "+(end-start));
            timeCost.add("Total : " + (end - start));
//        readDataTime();
//        runningTime();
            printTimeCost();
            System.out.println("*** Total Cost : " + Main.totalCost);
        }

        System.out.flush();
    }

    public static void costTest() {
        //Test function: calculateDayCost;
        Server s = new Server("a", 1, 1, 11, 1);
        List<Server> sl1 = new ArrayList<Server>();
        List<Server> sl2 = new ArrayList<Server>();
        sl1.add(s);
        sl1.add(s);
        sl1.add(s);
        sl1.add(s);
        sl1.add(s);
        sl1.add(s);
        sl1.add(s);
        sl1.add(s);
        sl1.add(s);
        sl1.add(s);
        sl2.add(s);
        sl2.add(s);
        sl2.add(s);
        sl2.add(s);
        System.out.println(calculateDayCost(sl1, sl2));
    }

    //计算整个程序运行时间
    private static void runningTime() {
        // 计算运行总共的时间
        long start = System.currentTimeMillis();
        try {
            Main.main(null);
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        long end = System.currentTimeMillis();
        System.out.println("*** Total Time : " + (end - start));

    }

    //计算数据读取运行时间
    private static void readDataTime() {
        // 计算运行总共的时间
        long start = System.currentTimeMillis();
        ReadDataTest.readlines();
        long end = System.currentTimeMillis();
        System.out.println("*** Read Data Time : " + (end - start));
    }

    //输出 timeCost
    private static void printTimeCost() {
        System.out.println("*********** Time Cost (ms) **************");
        timeCost.forEach(str -> {
            System.out.println(str);
        });
        System.out.println("*********** End of Time Cost ************");
    }


}
