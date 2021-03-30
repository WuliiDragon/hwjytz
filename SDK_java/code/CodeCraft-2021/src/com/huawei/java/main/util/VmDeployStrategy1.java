package com.huawei.java.main.util;

import com.huawei.java.main.Main;
import com.huawei.java.main.decision.DivideIntervalzyl;
import com.huawei.java.main.entity.Server;
import com.huawei.java.main.entity.ServerHavePurchase;
import com.huawei.java.main.entity.ServerNode;
import com.huawei.java.main.entity.VMHaveRequest;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author mkm_xjtu
 * @Date 2021/3/16
 */
public class VmDeployStrategy1 {
    public static void vmDeployStrategy1(List<List<VMHaveRequest>> vmHaveRequestListCurrent,
                                         Map<Integer, VMHaveRequest> vmIdMap,
                                         Map<String, List<List<ServerHavePurchase>>> serverHaveBuySinAndDouList,
                                         Map<Integer, ServerHavePurchase> serverIdMap,
                                         List<Server> serverTypeList,
                                         Map<Integer, List<Integer>> vmMigrationMap) {
//        boolean isFirstDay = true;


        Map<String, List<List<ServerHavePurchase>>> serverListByEnergyCost = sortServerListByEnergyCost(serverHaveBuySinAndDouList);

        List<List<ServerHavePurchase>> sinServerList = serverListByEnergyCost.get("single");
        List<List<ServerHavePurchase>> douServerList = serverListByEnergyCost.get("double");


        int nowDay = 0;
        for (List<VMHaveRequest> vmHaveRequests : vmHaveRequestListCurrent) {
            nowDay += 1;
            handleADay(vmHaveRequests, nowDay, vmMigrationMap, serverTypeList, serverListByEnergyCost, serverIdMap, sinServerList, douServerList, vmIdMap);
        }

        int x = Initialize.K;
        for (; x < Initialize.T - 1; x++) {
            nowDay += 1;
            handleADay(Initialize.readDay(), nowDay, vmMigrationMap, serverTypeList, serverListByEnergyCost, serverIdMap, sinServerList, douServerList, vmIdMap);
        }

        if (!Main.isMatch) {
            Cost.AddCostAllDay(serverHaveBuySinAndDouList);
        }
    }


    private static void handleADay(List<VMHaveRequest> vmHaveRequestListEveryday,
                                   int nowDay,
                                   Map<Integer, List<Integer>> vmMigrationMap,
                                   List<Server> serverTypeList,
                                   Map<String, List<List<ServerHavePurchase>>> serverHaveBuySinAndDouList,
                                   Map<Integer, ServerHavePurchase> serverIdMap,
                                   List<List<ServerHavePurchase>> sinServerList,
                                   List<List<ServerHavePurchase>> douServerList,
                                   Map<Integer, VMHaveRequest> vmIdMap) {


        if (nowDay > 1) {
            vmMigrationMap = VmMigrationStrategy.startVmMigrationPlan3_2_thread(serverHaveBuySinAndDouList, serverIdMap);
            sinServerList = DivideIntervalzyl.divideServerByRemainMemCoreEightRange1(serverHaveBuySinAndDouList.get("single"), 1, Main.rangeVM);
            douServerList = DivideIntervalzyl.divideServerByRemainMemCoreEightRange1(serverHaveBuySinAndDouList.get("double"), 0, Main.rangeVM);
        }


        List<List<ServerHavePurchase>> douServerListBuyToday = new ArrayList<>();
        List<List<ServerHavePurchase>> sinServerListBuyToday = new ArrayList<>();
        for (int i = 0; i < Main.rangeVM.length; ++i) {
            douServerListBuyToday.add(new ArrayList<ServerHavePurchase>());
            sinServerListBuyToday.add(new ArrayList<ServerHavePurchase>());
        }

        for (VMHaveRequest vm : vmHaveRequestListEveryday) {
            int addOrDel = vm.getAddOrDel();
            //增加虚拟机
            if (addOrDel == 1) {
                Main.vmNum += 1;
                int vmDeployType = vm.getDeployType();  // 0是单节点，1是双节点
                if (vmDeployType == 1) {
                    boolean isSuccessLoad = vmDouNodeDeploy(vm, douServerList); //是否找到可以分配的服务器
                    if (!isSuccessLoad) {
                        douNodeServerBuyAndLoadVm(vm, serverTypeList, douServerList, serverIdMap, douServerListBuyToday, nowDay);
                    }
                } else {
                    boolean isSuccessLoad = vmSinNodeDeploy(vm, sinServerList); //是否找到可以分配的服务器
                    if (!isSuccessLoad) {
                        sinNodeServerBuyAndLoadVm(vm, serverTypeList, sinServerList, serverIdMap, sinServerListBuyToday, nowDay);
                    }
                }
            } else { //删除虚拟机
                Main.vmNum -= 1;
                vmSinAndDouDelete(vmIdMap, vm, serverIdMap);
            }
        }

        Map<String, List<List<ServerHavePurchase>>> serverMapBuyToday = new HashMap<>();
        serverMapBuyToday.put("single", sinServerListBuyToday);
        serverMapBuyToday.put("double", douServerListBuyToday);
        Output.everydayOutput(serverHaveBuySinAndDouList, serverMapBuyToday, vmMigrationMap, vmHaveRequestListEveryday, serverIdMap);
        System.out.println("++++++++++++++++++");
        System.out.println(nowDay);
        if (!Main.isMatch) {
            Cost.AddCostADay(serverHaveBuySinAndDouList);
        }
    }


    private static List<List<ServerHavePurchase>> sinNodeServerBuyAndLoadVm(VMHaveRequest vm, List<Server> serverTypeList,
                                                                            List<List<ServerHavePurchase>> sinServerList,
                                                                            Map<Integer, ServerHavePurchase> serverIdMap,
                                                                            List<List<ServerHavePurchase>> sinServerListBuyToday, int nowDay) {
        float vmMemory = vm.getMemory();
        float vmCore = vm.getCore();
        float mcRate = vmMemory / vmCore;
        int rangeIndex = DivideIntervalzyl.calRangeIndex(mcRate, Main.rangeT);
//        List<List<Server>> serverDivideByMemCoreList = DivideInterval.divideServerByMemCoreNineRange(serverTypeList);
        List<List<Server>> serverDivideByMemCoreList = DivideIntervalzyl.divideServerByMemCoreEightRange(serverTypeList, Main.rangeVM);

        while (true) {
            List<Server> serverList = serverDivideByMemCoreList.get(rangeIndex);

            for (Server server : serverTypeList) {
                server.computeValueForMoney(vmMemory, vmCore, Main.requestTotalDayNum, nowDay);
            }

            Collections.sort(serverList, Comparator.comparing(Server::getValueForMoney));
            for (Server server : serverList) {
                float serverMemory = server.getMemory();
                float serverCore = server.getCore();
                if (serverMemory / 2 >= vmMemory && serverCore / 2 >= vmCore) {
//                    int id = serverIdMap.size();
                    int id = -1;
                    ServerNode serverNodeA = new ServerNode(0, id, (int) (serverCore / 2), (int) (serverMemory / 2));
                    ServerNode serverNodeB = new ServerNode(1, id, (int) (serverCore / 2), (int) (serverMemory / 2));
                    ServerHavePurchase serverHavePurchase = new ServerHavePurchase(server, id, serverNodeA, serverNodeB);
                    serverHavePurchase.setValueForMoney(server.getValueForMoney());
                    serverHavePurchase.addVM(vm, 0);
                    sinServerList.get(rangeIndex).add(serverHavePurchase);
                    sinServerListBuyToday.get(rangeIndex).add(serverHavePurchase);
                    updateVmCoreAndMemMap(vm, Main.vmSinMemoryMap, Main.vmSinCoreMap, true);
                    return sinServerListBuyToday;
                }
            }
            if (rangeIndex == Main.rangeVM.length) {
                rangeIndex = 0;
            } else {
                rangeIndex += 1;
            }
        }
    }

    private static List<List<ServerHavePurchase>> douNodeServerBuyAndLoadVm(VMHaveRequest vm, List<Server> serverTypeList,
                                                                            List<List<ServerHavePurchase>> douServerList,
                                                                            Map<Integer, ServerHavePurchase> serverIdMap,
                                                                            List<List<ServerHavePurchase>> douServerListBuyToday, int nowDay) {


        float vmMemory = vm.getMemory();
        float vmCore = vm.getCore();
        float mcRate = vmMemory / vmCore;
        int rangeIndex = DivideIntervalzyl.calRangeIndex(mcRate, Main.rangeT);

        List<List<Server>> serverDivideByMemCoreList = DivideIntervalzyl.divideServerByMemCoreEightRange(serverTypeList, Main.rangeVM);
        // 如果在当前区间找不到满足条件的server，会从其他区间找
        while (true) {
            List<Server> serverList = serverDivideByMemCoreList.get(rangeIndex);

            for (Server server : serverTypeList) {
                server.computeValueForMoney(vmMemory, vmCore, Main.requestTotalDayNum, nowDay);
            }
            Collections.sort(serverList, Comparator.comparing(Server::getValueForMoney));

            for (Server server : serverList) {
                int serverMemory = server.getMemory();
                int serverCore = server.getCore();
                if (serverMemory >= vmMemory && serverCore >= vmCore) {
//                    int id = serverIdMap.size();
                    int id = -1;
                    int remainCore = serverCore / 2;
                    int remainMemory = serverMemory / 2;
                    ServerNode serverNodeA = new ServerNode(0, id, remainCore, remainMemory);
                    ServerNode serverNodeB = new ServerNode(1, id, remainCore, remainMemory);
                    ServerHavePurchase serverHavePurchase = new ServerHavePurchase(server, id, serverNodeA, serverNodeB);
                    serverHavePurchase.setValueForMoney(server.getValueForMoney());
                    serverHavePurchase.addVM(vm, 2);
                    douServerList.get(rangeIndex).add(serverHavePurchase);
                    douServerListBuyToday.get(rangeIndex).add(serverHavePurchase);
                    updateVmCoreAndMemMap(vm, Main.vmDouMemoryMap, Main.vmDouCoreMap, true);
                    return douServerListBuyToday;
                }
            }
            if (rangeIndex == Main.rangeVM.length) {
                rangeIndex = 0;
            } else {
                rangeIndex += 1;
            }

        }
    }


    private static void vmSinAndDouDelete(Map<Integer, VMHaveRequest> vmIdMap, VMHaveRequest vmRequest, Map<Integer, ServerHavePurchase> serverIdMap) {
        int vmId = vmRequest.getId();
        VMHaveRequest vmHaveRequest = vmIdMap.get(vmId);
        int serverId = vmHaveRequest.getServerId();
        ServerHavePurchase server = serverIdMap.get(serverId);
        server.delVM(server, vmHaveRequest);
        if (vmRequest.getDeployType() == 1) {
            updateVmCoreAndMemMap(vmHaveRequest, Main.vmDouMemoryMap, Main.vmDouCoreMap, false);
        } else {
            updateVmCoreAndMemMap(vmHaveRequest, Main.vmSinMemoryMap, Main.vmSinCoreMap, false);
        }
    }

    private static boolean vmSinNodeDeploy(VMHaveRequest vm, List<List<ServerHavePurchase>> sinServerList) {
        float vmCore = vm.getCore();
        float vmMemory = vm.getMemory();
        float mcRate = vmMemory / vmCore;
        int rangeIndex = calRangeIndexEight1(mcRate, Main.rangeVM);
        boolean vmIsLoadOnFlag = false;
        int tmpRangeIndex = rangeIndex;
        /**
         * 将List按着虚拟机由多到少排序
         */
        /***********************************************************/
        for (List<ServerHavePurchase> everyRangeServerList : sinServerList) {
            for (ServerHavePurchase serverHavePurchase : everyRangeServerList) {
                List<VMHaveRequest> holdVM = serverHavePurchase.getHoldVM();
                serverHavePurchase.setHoldVmNum(holdVM.size());
                serverHavePurchase.setMcRatioSum();
            }
            Collections.sort(everyRangeServerList, Comparator.comparing(ServerHavePurchase::getMcRatioSum));
//            Collections.sort(everyRangeServerList, Comparator.comparing(ServerHavePurchase::getHoldVmNum).reversed());
        }
        /****************************************************/

        int index = rangeIndex;
        for (int i = 0; i < sinServerList.size(); i++) {
            List<ServerHavePurchase> serverListByRange = sinServerList.get(index);
            for (ServerHavePurchase server : serverListByRange) {
                int remainCoreA = server.getServerNodeA().getRemainCore();
                int remainMemA = server.getServerNodeA().getRemainMemory();
                int remainCoreB = server.getServerNodeB().getRemainCore();
                int remainMemB = server.getServerNodeB().getRemainMemory();
                if (remainCoreA >= vmCore && remainMemA >= vmMemory) {
                    server.addVM(vm, 0);
                    updateVmCoreAndMemMap(vm, Main.vmSinMemoryMap, Main.vmSinCoreMap, true);
                    return true;
                } else if (remainCoreB >= vmCore && remainMemB >= vmMemory) {
                    server.addVM(vm, 1);
                    updateVmCoreAndMemMap(vm, Main.vmSinMemoryMap, Main.vmSinCoreMap, true);
                    return true;
                }
            }
            index++;
            if (index == sinServerList.size()) {
                index = 0;
            }
        }
        return false;
    }

    private static boolean vmDouNodeDeploy(VMHaveRequest vm, List<List<ServerHavePurchase>> douServerList) {
        float vmCore = vm.getCore();
        float vmMemory = vm.getMemory();
        float mcRate = vmMemory / vmCore;
        int rangeIndex = calRangeIndexEight1(mcRate, Main.rangeVM);

        /**
         * 将List按着虚拟机由多到少排序
         */
        /***********************************************************/
        for (List<ServerHavePurchase> everyRangeServerList : douServerList) {
            for (ServerHavePurchase serverHavePurchase : everyRangeServerList) {
                List<VMHaveRequest> holdVM = serverHavePurchase.getHoldVM();
                serverHavePurchase.setHoldVmNum(holdVM.size());
                serverHavePurchase.setMcRatioSum();
            }
            Collections.sort(everyRangeServerList, Comparator.comparing(ServerHavePurchase::getMcRatioSum));
//            Collections.sort(everyRangeServerList, Comparator.comparing(ServerHavePurchase::getHoldVmNum).reversed());
        }


        /****************************************************/

        int index = rangeIndex;
        for (int i = 0; i < douServerList.size(); i++) {
            List<ServerHavePurchase> serverListByRange = douServerList.get(index);
            for (ServerHavePurchase server : serverListByRange) {
                int remainCore = server.getServerNodeA().getRemainCore();
                int remainMem = server.getServerNodeA().getRemainMemory();
                if (remainCore >= vmCore / 2 && remainMem >= vmMemory / 2) {
                    server.addVM(vm, 2);
                    updateVmCoreAndMemMap(vm, Main.vmDouMemoryMap, Main.vmDouCoreMap, true);
                    return true;
                }
            }
            index++;
            if (index == douServerList.size()) {
                index = 0;
            }
        }
        return false;
    }

    private static Map<String, List<List<ServerHavePurchase>>> sortServerListByEnergyCost(Map<String, List<List<ServerHavePurchase>>> serverHaveBuySinAndDouMap) {
        List<List<ServerHavePurchase>> douNodeServerFourRange = serverHaveBuySinAndDouMap.get("double");
        List<List<ServerHavePurchase>> sinNodeServerFourRange = serverHaveBuySinAndDouMap.get("single");

        List<List<ServerHavePurchase>> douServerRangeList = new ArrayList<>();
        List<List<ServerHavePurchase>> sinServerRangeList = new ArrayList<>();


        for (List<ServerHavePurchase> serverListEveryRange : douNodeServerFourRange) {

            serverListEveryRange = serverListEveryRange.stream().sorted(
                    Comparator.comparing(ServerHavePurchase::getEnergyConsumptionPerDay)
                            .thenComparing(ServerHavePurchase::getTypeName)
            ).collect(Collectors.toList());
            douServerRangeList.add(serverListEveryRange);
        }
        for (List<ServerHavePurchase> serverListEveryRange : sinNodeServerFourRange) {

            serverListEveryRange = serverListEveryRange.stream().sorted(
                    Comparator.comparing(ServerHavePurchase::getEnergyConsumptionPerDay)
                            .thenComparing(ServerHavePurchase::getTypeName)
            ).collect(Collectors.toList());
            sinServerRangeList.add(serverListEveryRange);
        }

        Map<String, List<List<ServerHavePurchase>>> serverAfterSorted = new HashMap<>();
        serverAfterSorted.put("single", sinServerRangeList);
        serverAfterSorted.put("double", douServerRangeList);
        return serverAfterSorted;
    }

    public static int calRangeIndex(float mcRate) {
        int rangeIndex;
        if (mcRate <= 1 || mcRate > 9) {
            rangeIndex = 0;
        } else if (mcRate > 1 && mcRate <= 3.5) {
            rangeIndex = 1;
        } else if (mcRate > 3.5 && mcRate <= 6) {
            rangeIndex = 2;
        } else {
            rangeIndex = 3;
        }
        return rangeIndex;
    }

    public static int calRangeIndexNine(float mcRate) {
        int rangeIndex;
        if (mcRate <= 0.7 || mcRate > 7) {
            rangeIndex = 0;
        } else if (mcRate <= 1) {
            rangeIndex = 1;
        } else if (mcRate <= 2) {
            rangeIndex = 2;
        } else if (mcRate <= 3) {
            rangeIndex = 3;
        } else if (mcRate <= 4) {
            rangeIndex = 4;
        } else if (mcRate <= 5) {
            rangeIndex = 5;
        } else if (mcRate <= 6) {
            rangeIndex = 6;
        } else {
            rangeIndex = 7;
        }
        return rangeIndex;
    }


    public static int calRangeIndexEight1(float mcRate, float range[]) {

        for (int i = 0; i < range.length - 1; i++) {
            if (i == 0 && mcRate <= range[i]) {
                return i;
            }
            if (mcRate > range[i] && mcRate <= range[i + 1]) {
                return i + 1;
            }
        }
        return 0;
    }

    private static void updateVmCoreAndMemMap(VMHaveRequest vm, Map<Integer, List<VMHaveRequest>> vmMemoryMap, Map<Integer, List<VMHaveRequest>> vmCoreMap, boolean isAdd) {
        int core = vm.getCore();
        int memory = vm.getMemory();
        if (isAdd) {
            if (vmMemoryMap.containsKey(memory)) {
                List<VMHaveRequest> vmHaveRequests = vmMemoryMap.get(memory);
                vmHaveRequests.add(vm);
                vmMemoryMap.put(memory, vmHaveRequests);
            } else {
                List<VMHaveRequest> vmHaveRequests = new ArrayList<>();
                vmHaveRequests.add(vm);
                vmMemoryMap.put(memory, vmHaveRequests);
            }

            if (vmCoreMap.containsKey(core)) {
                List<VMHaveRequest> vmHaveRequests = vmCoreMap.get(core);
                vmHaveRequests.add(vm);
                vmCoreMap.put(core, vmHaveRequests);
            } else {
                List<VMHaveRequest> vmHaveRequests = new ArrayList<>();
                vmHaveRequests.add(vm);
                vmCoreMap.put(core, vmHaveRequests);
            }
        } else {
            List<VMHaveRequest> vmMemoryList = vmMemoryMap.get(memory);
            vmMemoryList.remove(vm);
            vmMemoryMap.put(memory, vmMemoryList);

            List<VMHaveRequest> vmCoreList = vmCoreMap.get(core);
            vmCoreList.remove(vm);
            vmCoreMap.put(core, vmCoreList);
        }

    }
}