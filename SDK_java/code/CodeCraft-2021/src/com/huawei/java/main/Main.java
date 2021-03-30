package com.huawei.java.main;

import com.huawei.java.main.entity.Server;
import com.huawei.java.main.entity.ServerHavePurchase;
import com.huawei.java.main.entity.VM;
import com.huawei.java.main.entity.VMHaveRequest;
import com.huawei.java.main.util.Initialize;
import com.huawei.java.main.util.VmDeployStrategy1;

import java.util.*;

public class Main {
    public static boolean isMatch = true;
    public static String path = "../../data/training-1.txt";
    public static int requestTotalDayNum;
    public static int topSelectToBuy = 5;
    public static int totalCost = 0;
    public static int rangesNum = 7;
    //    public static float totalCost=0;
    public static  List<Map<Integer, List<Integer>>> vmMigrationMapEveryDay2=new ArrayList<>();
    public static Map<Integer, List<VMHaveRequest>> vmDouCoreMap ;
    public static Map<Integer, List<VMHaveRequest>> vmDouMemoryMap;

    public static Map<Integer, List<VMHaveRequest>> vmSinCoreMap;
    public static Map<Integer, List<VMHaveRequest>> vmSinMemoryMap =new HashMap<>();
    public static int maxGateValue =20;
    public static int minGateValue=4;

    public static int gateValue=maxGateValue;

    public static float rangeVM[] = {1, 2,3, 4,5,6,7,8};


//    public static float rangeServer[] = {0.4f, 0.8f, 1, 2, 3, 4, 5, 6, 7};
//    public static float rangeVM[] = {0.4f, 0.8f, 1, 2, 3, 4, 5, 6, 7};
//    public static float rangeT[] = {1f,2f,3f,5,7};
    public static float rangeT[] = {1f,3.4f,6,9};
    public static int vmNum=0;
//    public static float rangeT[] = {1f,2.5f,4f,6,9};

    public static void main(String[] args) throws CloneNotSupportedException {
        //表示服务器的总c和m是虚拟机的倍数（多出的部分是阈度）

        Map<Integer, List<Integer>> vmMigrationMap = new LinkedHashMap<>();
        Map<Integer, ServerHavePurchase> serverIdMap = new HashMap<>();
        List<Server> serverTypeList = Initialize.initServer();
        List<VM> vmTypeList = Initialize.initVM();
        List<List<VMHaveRequest>> vmHaveRequestListAllDay = Initialize.initVMRequest();
        vmDouCoreMap =new HashMap<>();
        vmDouMemoryMap =new HashMap<>();
        vmSinCoreMap =new HashMap<>();
        vmSinMemoryMap =new HashMap<>();
        /**
         * 初始化服务器类型列表
         * 虚拟机类型列表
         * 用户请求列表，用户请求用
         */

        Initialize.initialize(serverTypeList, vmTypeList, vmHaveRequestListAllDay);
        vmNum=0;
        requestTotalDayNum = vmHaveRequestListAllDay.size();

        Map<String, List<List<ServerHavePurchase>>> sinAndDouServerList = new HashMap<>();
        List<List<ServerHavePurchase>> singleServerHavePurchaseList = new ArrayList<>();
        List<List<ServerHavePurchase>> doubleServerHavePurchaseList = new ArrayList<>();
        for (int i = 0; i < Main.rangeVM.length; ++i) {
            singleServerHavePurchaseList.add(new ArrayList<>());
            doubleServerHavePurchaseList.add(new ArrayList<>());
        }

        sinAndDouServerList.put("single", singleServerHavePurchaseList);
        sinAndDouServerList.put("double", doubleServerHavePurchaseList);


        VmDeployStrategy1.vmDeployStrategy1(vmHaveRequestListAllDay, Initialize.vmIdMap, sinAndDouServerList, serverIdMap, serverTypeList, vmMigrationMap);


        return;
    }
}