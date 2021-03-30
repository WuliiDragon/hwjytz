package com.huawei.java.main.decision;

import com.huawei.java.main.Main;
import com.huawei.java.main.entity.Server;
import com.huawei.java.main.entity.VMHaveRequest;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 动态划分区间
 * 注意：请先执行 VM划分，在执行server划分！！！
 *
 * @author Lofit .LaeL
 * @email GreatLandmark@outlook.com
 * @date 2021/3/17
 */
public class DivideRange {
    public static final int rangeNum = Main.rangesNum;// 6个区间
    public static float[] sThreshold = new float[rangeNum - 1];// 单节点，区间的threshold阈值
    public static float[] dThreshold = new float[rangeNum - 1];// 双节点，区间的threshold阈值
    public static int singleAddVmTotal = 0, doubleAddVmTotal = 0;
    public static List<List<VMHaveRequest>> singleAddList = new ArrayList<>();//单节点，add的按天分的VM
    public static List<List<VMHaveRequest>> doubleAddList = new ArrayList<>();//双节点，add的按天分的VM
    public static List<List<Server>> singleDivideServerType = new ArrayList<>();
    public static List<List<Server>> doubleDivideServerType = new ArrayList<>();
    /**
     * 统计各个区间中add、del比，计算方式 rate=1-del/add;
     * float[][]，第一维 0单1双，第二维为区间
     */
    public static float[][] eachRangeRate = new float[2][rangeNum];
    static List<Float> sMcList = new ArrayList<>();
    static List<Float> dMcList = new ArrayList<>();


    /**
     * 计算阈值、划分VM、计算各区间的rate
     *
     * @param vmList 请求的VM的List
     * @return 三维List, 第一维0单 1双，第二维：区间，第三维 VM.
     */
    public static List<List<List<VMHaveRequest>>> divideVmByMemCore(List<List<VMHaveRequest>> vmList) {
        //预处理操作
        classifyVm(vmList);//去除del VM等操作
        calculateThreshold();//计算阈值
        //下面进行按阈值划分VM
        List<List<List<VMHaveRequest>>> allVmHaveDivideByMcList = new ArrayList<>();
        //单节点的
        List<List<VMHaveRequest>> singleVmHaveDivideByMcList = new ArrayList<>();
//        双节点的
        List<List<VMHaveRequest>> doubleVmHaveDivideByMcList = new ArrayList<>();
        for (int i = 0; i < rangeNum; i++) {
            List<VMHaveRequest> newList = new ArrayList<>();
            singleVmHaveDivideByMcList.add(newList);
        }
        for (int i = 0; i < rangeNum; i++) {
            List<VMHaveRequest> newList = new ArrayList<>();
            doubleVmHaveDivideByMcList.add(newList);
        }
//  计算rate, 四维分别为：单双、区间、增加删除、core|memroy
        int[][][][] matrix = new int[2][rangeNum][2][2];

        singleAddList.forEach(dayVm -> {
            dayVm.forEach(item -> {
                float rate = (float) item.getMemory() / item.getCore();
                boolean flag = true;
                for (int i = 0; i < rangeNum - 1; i++) {
                    if (rate <= sThreshold[i]) {
                        singleVmHaveDivideByMcList.get(i).add(item);
                        //  add
                        if (1 == item.getAddOrDel()) {
                            matrix[0][i][0][0] += item.getCore();
                            matrix[0][i][0][1] += item.getMemory();
                        } else {
                            //  del
                            matrix[0][i][1][0] += item.getCore();
                            matrix[0][i][1][1] += item.getMemory();
                        }
                        flag = false;
                        break;
                    }
                    ;
                }
                if (flag) {
                    singleVmHaveDivideByMcList.get(rangeNum - 1).add(item);
                    //  add
                    if (1 == item.getAddOrDel()) {
                        matrix[0][rangeNum - 1][0][0] += item.getCore();
                        matrix[0][rangeNum - 1][0][1] += item.getMemory();
                    } else {
                        //  del
                        matrix[0][rangeNum - 1][1][0] += item.getCore();
                        matrix[0][rangeNum - 1][1][1] += item.getMemory();
                    }
                }
            });
        });
        doubleAddList.forEach(dayVm -> {
            dayVm.forEach(item -> {
                float rate = (float) item.getMemory() / item.getCore();
                boolean flag = true;
                for (int i = 0; i < rangeNum - 1; i++) {
                    if (rate <= dThreshold[i]) {
                        doubleVmHaveDivideByMcList.get(i).add(item);
                        //add
                        if (1 == item.getAddOrDel()) {
                            matrix[1][i][0][0] += item.getCore();
                            matrix[1][i][0][1] += item.getMemory();
                        } else {
                            // del
                            matrix[1][i][1][0] += item.getCore();
                            matrix[1][i][1][1] += item.getMemory();
                        }
                        flag = false;
                        break;
                    }
                }
                if (flag) {
                    doubleVmHaveDivideByMcList.get(rangeNum - 1).add(item);
                    //add
                    if (1 == item.getAddOrDel()) {
                        matrix[1][rangeNum - 1][0][0] += item.getCore();
                        matrix[1][rangeNum - 1][0][1] += item.getMemory();
                    } else {
                        // del
                        matrix[1][rangeNum - 1][1][0] += item.getCore();
                        matrix[1][rangeNum - 1][1][1] += item.getMemory();
                    }
                }
            });
        });
        allVmHaveDivideByMcList.add(singleVmHaveDivideByMcList);
        allVmHaveDivideByMcList.add(doubleVmHaveDivideByMcList);

        for (int i = 0; i < 2; i++) {//单双
            for (int j = 0; j < rangeNum; j++) {//区间
                float c = 1 - (float) matrix[i][j][1][0] / matrix[i][j][0][0];//core
                float m = 1 - (float) matrix[i][j][1][1] / matrix[i][j][0][1];//memory
                // core memory取小作为rate
                if (c > m) {
                    eachRangeRate[i][j] = m;
                } else {
                    eachRangeRate[i][j] = c;
                }
            }
        }

        return allVmHaveDivideByMcList;
    }

    /**
     * 请先执行 divideVmByMemCore(...) ，因为需要先确定阈值。
     *
     * @param serverList
     * @return 三维List 第一维0单 1双，第二维：区间，第三维 server
     */
    public static List<List<List<Server>>> divideServerByMemCore(List<Server> serverList) {

        List<List<List<Server>>> allServerList = new ArrayList<>();
        for (int i = 0; i < rangeNum; i++) {
            List<Server> newList = new ArrayList<>();
            singleDivideServerType.add(newList);
        }
        for (int i = 0; i < rangeNum; i++) {
            List<Server> newList = new ArrayList<>();
            doubleDivideServerType.add(newList);
        }

        serverList.forEach(
                item -> {
                    float rate = (float) item.getMemory() / item.getCore();
                    boolean flag = true;
                    for (int i = 0; i < rangeNum - 1; i++) {
                        if (rate <= sThreshold[i]) {
                            singleDivideServerType.get(i).add(item);
                            flag = false;
                            break;
                        }
                    }
                    if (flag) {
                        singleDivideServerType.get(rangeNum - 1).add(item);
                    }

                    flag = true;
                    for (int i = 0; i < rangeNum - 1; i++) {
                        if (rate <= dThreshold[i]) {
                            doubleDivideServerType.get(i).add(item);
                            flag = false;
                            break;
                        }
                    }
                    if (flag) {
                        doubleDivideServerType.get(rangeNum - 1).add(item);
                    }
                });
        allServerList.add(singleDivideServerType);
        allServerList.add(doubleDivideServerType);
        return allServerList;
    }

    public static int calSingleRangeIndexDynamic(float mcRate) {
        int rangeIndex = -1;
        for (int i = 0; i < rangeNum - 1; i++) {
            if (mcRate <= sThreshold[i]) {
                return i;
            }
        }
        return rangeNum - 1;
    }

    public static int calDoubleRangeIndexDynamic(float mcRate) {
        int rangeIndex = -1;
        for (int i = 0; i < rangeNum - 1; i++) {
            if (mcRate <= dThreshold[i]) {
                return i;
            }
        }
        return rangeNum - 1;
    }

    private static void calculateThreshold() {
        sMcList.sort(Comparator.naturalOrder());
        int sIndex = (int) singleAddVmTotal / rangeNum;
        dMcList.sort(Comparator.naturalOrder());
        int dIndex = (int) doubleAddVmTotal / rangeNum;

        for (int i = 0; i < rangeNum - 1; ) {
            sThreshold[i] = sMcList.get(sIndex * (i + 1));
            dThreshold[i] = dMcList.get(dIndex * (++i));
        }
    }

    private static void classifyVm(List<List<VMHaveRequest>> vmList) {
        vmList.forEach(dayVmList -> {
            List<VMHaveRequest> sAddDayList = new ArrayList<>();
            List<VMHaveRequest> dAddDayList = new ArrayList<>();
            dayVmList.forEach(vm -> {
                if (1 == vm.getAddOrDel()) {
                    if (0 == vm.isDeployType()) {
                        //single
                        sAddDayList.add(vm);
                        singleAddVmTotal++;
                        sMcList.add((float) vm.getMemory() / vm.getCore());
                    } else {
                        //double
                        dAddDayList.add(vm);
                        doubleAddVmTotal++;
                        dMcList.add((float) vm.getMemory() / vm.getCore());
                    }
                }
            });
            singleAddList.add(sAddDayList);
            doubleAddList.add(dAddDayList);
        });
    }

}
