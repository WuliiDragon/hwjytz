package com.huawei.java.test;

import java.io.*;
import java.util.ArrayList;
import java.util.List;


/**
 * @author Lofit .LaeL
 * @email GreatLandmark@outlook.com
 * @date 2021/3/13
 */
public class ReadDataTest {
    public static BufferedReader bufferedReader;
    public static String path = "../../data/training-1.txt";

    public static String readline() {
        File file = new File(path);
        if (file.isFile() && file.exists()) {
            try {
                FileInputStream fileInputStream = new FileInputStream(file);
                InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
                bufferedReader = new BufferedReader(inputStreamReader);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        String context = null;
//        if (Main.isMatch) {
//            Scanner scan = new Scanner(System.in);
//            context = scan.nextLine();
//        } else {
        try {
            context = bufferedReader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
//        }
        return context;
    }

    public static List<String> readlines() {
        File file = new File(path);
        if (file.isFile() && file.exists()) {
            try {
                FileInputStream fileInputStream = new FileInputStream(file);
                InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
                bufferedReader = new BufferedReader(inputStreamReader);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        List<String> lines = new ArrayList<String>();
        String contentLine;
//        if (Main.isMatch) {
//            Scanner scan = new Scanner(System.in);
//            while (scan.hasNext()) {
//                lines.add(scan.nextLine());
//            }
//        } else {
        try {
            while ((contentLine = bufferedReader.readLine()) != null) {
                lines.add(contentLine);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
//        }
        return lines;
    }
}
