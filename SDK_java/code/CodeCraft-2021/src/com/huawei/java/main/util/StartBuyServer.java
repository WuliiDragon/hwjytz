//package com.huawei.java.main.util;
//
//import com.huawei.java.main.decision.DivideRange;
//import com.huawei.java.main.entity.Server;
//import com.huawei.java.main.entity.ServerHavePurchase;
//import com.huawei.java.main.entity.ServerNode;
//import com.huawei.java.main.entity.VMHaveRequest;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
///**
// * @Author mkm_xjtu
// * @Date 2021/3/15
// */
//public class StartBuyServer {
//
//    /**
//     *
//     * @param serverTypeList 服务器类型列表
//     * @param vmHaveRequestListAllDay
//     * @param extendRate
//     * @return
//     */
//    public static Map<String, List<Map<String, Integer>>> startBuyServerByVmRequest(List<Server> serverTypeList, List<List<VMHaveRequest>> vmHaveRequestListAllDay, float extendRate) {
////        List<List<Server>> serverDivideByMemCoreList = DivideInterval.divideServerByMemCore(serverTypeList);
////        List<List<List<VMHaveRequest>>> vmDivideSinAndDou = DivideInterval.divideVmByMemCore(vmHaveRequestListAllDay);
//
//        List<List<Server>> serverDivideByMemCoreList = DivideInterval.divideServerByMemCoreEightRange(serverTypeList);
//        List<List<List<VMHaveRequest>>> vmDivideSinAndDou = DivideInterval.divideVmByMemCoreEightRange(vmHaveRequestListAllDay);
//
//        List<List<VMHaveRequest>> singleVm = vmDivideSinAndDou.get(0);
//        List<List<VMHaveRequest>> doubleVm = vmDivideSinAndDou.get(1);
//
//        List<Map<String, Integer>> serverSingleHaveBuyNameAndNum = new ArrayList<>();
//        List<Map<String, Integer>> serverDoubleHaveBuyNameAndNum = new ArrayList<>();
//
//        Map<String, List<Map<String, Integer>>> result = new HashMap<>();
//
//        for (int i = 0; i < singleVm.size(); i++) {
//            List<VMHaveRequest> vmHaveRequestOneScope = singleVm.get(i);
//
//            List<Integer> vmCoreAndMemSumByOneList = PurchaseServerMethod.getVmCoreAndMemSumByOneList(vmHaveRequestOneScope);
//            int coreSum = vmCoreAndMemSumByOneList.get(0);
//            int memSum = vmCoreAndMemSumByOneList.get(1);
//            serverSingleHaveBuyNameAndNum.add(PurchaseServerMethod.purchaseServerPlan1(serverDivideByMemCoreList.get(i), (float) coreSum,(float)  memSum, extendRate));
//        }
//
//        for (int i = 0; i < doubleVm.size(); i++) {
//            List<VMHaveRequest> vmHaveRequestOneScope = doubleVm.get(i);
//
//            List<Integer> vmCoreAndMemSumByOneList = PurchaseServerMethod.getVmCoreAndMemSumByOneList(vmHaveRequestOneScope);
//            int coreSum = vmCoreAndMemSumByOneList.get(0);
//            int memSum = vmCoreAndMemSumByOneList.get(1);
//            serverDoubleHaveBuyNameAndNum.add(PurchaseServerMethod.purchaseServerPlan1(serverDivideByMemCoreList.get(i), coreSum, memSum, extendRate));
//        }
//        result.put("double", serverDoubleHaveBuyNameAndNum);
//        result.put("single", serverSingleHaveBuyNameAndNum);
//        return result;
//    }
//
//    //重载方法，动态rate
//    public static Map<String, List<Map<String, Integer>>> startBuyServerByVmRequest(List<Server> serverTypeList, List<List<VMHaveRequest>> vmHaveRequestListAllDay) {
////        List<List<Server>> serverDivideByMemCoreList = DivideInterval.divideServerByMemCore(serverTypeList);
////        List<List<List<VMHaveRequest>>> vmDivideSinAndDou = DivideInterval.divideVmByMemCore(vmHaveRequestListAllDay);
//
////        List<List<Server>> serverDivideByMemCoreList = DivideInterval.divideServerByMemCoreEightRange(serverTypeList);
////        List<List<List<VMHaveRequest>>> vmDivideSinAndDou = DivideInterval.divideVmByMemCoreEightRange(vmHaveRequestListAllDay);
//        //！！！先划分VM，因为要通过VM 列表确定阈值
//        List<List<List<VMHaveRequest>>> vmDivideSinAndDou = DivideRange.divideVmByMemCore(vmHaveRequestListAllDay);
//        List<List<List<Server>>> serverDivideSinAndDou = DivideRange.divideServerByMemCore(serverTypeList);
//
//        List<List<VMHaveRequest>> singleVm = vmDivideSinAndDou.get(0);
//        List<List<VMHaveRequest>> doubleVm = vmDivideSinAndDou.get(1);
//        List<List<Server>> singelServerDivide = serverDivideSinAndDou.get(0);
//        List<List<Server>> doubleServerDivide = serverDivideSinAndDou.get(1);
//
//        List<Map<String, Integer>> serverSingleHaveBuyNameAndNum = new ArrayList<>();
//        List<Map<String, Integer>> serverDoubleHaveBuyNameAndNum = new ArrayList<>();
//
//        Map<String, List<Map<String, Integer>>> result = new HashMap<>();
//
//        for (int i = 0; i < singleVm.size(); i++) {
//            List<VMHaveRequest> vmHaveRequestOneScope = singleVm.get(i);
//
//            List<Integer> vmCoreAndMemSumByOneList = PurchaseServerMethod.getVmCoreAndMemSumByOneList(vmHaveRequestOneScope);
//            int coreSum = vmCoreAndMemSumByOneList.get(0);
//            int memSum = vmCoreAndMemSumByOneList.get(1);
//            float extendRate = DivideRange.eachRangeRate[0][i];
//            extendRate = 0.01f;
//            serverSingleHaveBuyNameAndNum.add(PurchaseServerMethod.purchaseServerPlan1(singelServerDivide.get(i), (float) coreSum, (float) memSum, extendRate));
//        }
//
//        for (int i = 0; i < doubleVm.size(); i++) {
//            List<VMHaveRequest> vmHaveRequestOneScope = doubleVm.get(i);
//
//            List<Integer> vmCoreAndMemSumByOneList = PurchaseServerMethod.getVmCoreAndMemSumByOneList(vmHaveRequestOneScope);
//            int coreSum = vmCoreAndMemSumByOneList.get(0);
//            int memSum = vmCoreAndMemSumByOneList.get(1);
//            float extendRate = DivideRange.eachRangeRate[1][i];
//            extendRate = 0.01f;
//            serverDoubleHaveBuyNameAndNum.add(PurchaseServerMethod.purchaseServerPlan1(doubleServerDivide.get(i), coreSum, memSum, extendRate));
//        }
//        result.put("double", serverDoubleHaveBuyNameAndNum);
//        result.put("single", serverSingleHaveBuyNameAndNum);
//        return result;
//    }
//
//    public static Map<String, List<List<ServerHavePurchase>>> buyPlanServer(Map<String, List<Map<String, Integer>>> plan) {
//        Map<String, List<List<ServerHavePurchase>>> result = new HashMap<>();
//
//        List<Map<String, Integer>> doublePlan = plan.get("double");
//        List<Map<String, Integer>> singlePlan = plan.get("single");
//
//        List<List<ServerHavePurchase>> serverSigleBuy = new ArrayList<>();
//        for (Map<String, Integer> t : singlePlan) {
//            List<ServerHavePurchase> ServerHavePurchaseBuyByPlan_t = new ArrayList<ServerHavePurchase>();
//
//            for (String serverName : t.keySet()) {
//
//                int buyNum = t.get(serverName);
//                for (int i = 0; i < buyNum; i++) {
//                    Server server = Initialize.serverTypeMap.get(serverName);
//                    ServerNode serverNodeA = new ServerNode(0,  server.getCore() / 2, server.getMemory() / 2);
//                    ServerNode serverNodeB = new ServerNode(1,  server.getCore() / 2, server.getMemory() / 2);
//                    ServerHavePurchase serverHavePurchase = new ServerHavePurchase(server, serverNodeA, serverNodeB);
//                    ServerHavePurchaseBuyByPlan_t.add(serverHavePurchase);
//
//                }
//            }
//            serverSigleBuy.add(ServerHavePurchaseBuyByPlan_t);
//
//        }
//
//        List<List<ServerHavePurchase>> serverDoubleBuy = new ArrayList<>();
//        for (Map<String, Integer> t : doublePlan) {
//            List<ServerHavePurchase> ServerHavePurchaseBuyByPlan_t = new ArrayList<ServerHavePurchase>();
//
//            for (String serverName : t.keySet()) {
//
//                int buyNum = t.get(serverName);
//                for (int i = 0; i < buyNum; i++) {
//                    Server server = Initialize.serverTypeMap.get(serverName);
//                    ServerNode serverNodeA = new ServerNode(0, server.getCore() / 2, server.getMemory() / 2);
//                    ServerNode serverNodeB = new ServerNode(1, server.getCore() / 2, server.getMemory() / 2);
//                    ServerHavePurchase serverHavePurchase = new ServerHavePurchase(server, serverNodeA, serverNodeB);
//                    ServerHavePurchaseBuyByPlan_t.add(new ServerHavePurchase(server,  serverNodeA, serverNodeB));
//                }
//            }
//            serverDoubleBuy.add(ServerHavePurchaseBuyByPlan_t);
//        }
//        result.put("single", serverSigleBuy);
//        result.put("double", serverDoubleBuy);
//        return result;
//    }
//    public static void t() {
//
//    }
//
//
//}
