package com.huawei.java.main.util;

import com.huawei.java.main.Main;
import com.huawei.java.main.decision.DivideIntervalzyl;
import com.huawei.java.main.entity.ServerHavePurchase;
import com.huawei.java.main.entity.VMHaveRequest;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

/**
 * @Author mkm_xjtu
 * @Date 2021/3/18
 */
public class VmMigrationStrategy {


    // 迁移方案1：在同一range内，将serverList根据装载的虚拟机数由小到大排序，将数量少的服务器中的虚拟机放到数量多的服务器里面，节约服务器
    public static Map<Integer, List<Integer>> startVmMigrationPlan1(Map<String, List<List<ServerHavePurchase>>> serverHaveBuySinAndDouList) {

        Map<Integer, List<Integer>> vmMigrationMapEveryDay = new HashMap<>();
        List<List<ServerHavePurchase>> singleServerList = serverHaveBuySinAndDouList.get("single");
        List<List<ServerHavePurchase>> doubleServerList = serverHaveBuySinAndDouList.get("double");

        float douVmTotalNum = 0;
        float sinVmTotalNum = 0;

        for (List<ServerHavePurchase> serverHavePurchaseList : doubleServerList) {
            for (ServerHavePurchase serverHavePurchase : serverHavePurchaseList) {
                List<VMHaveRequest> holdVM = serverHavePurchase.getHoldVM();
                douVmTotalNum += holdVM.size();
            }
        }
        for (List<ServerHavePurchase> serverHavePurchaseList : singleServerList) {
            for (ServerHavePurchase serverHavePurchase : serverHavePurchaseList) {
                List<VMHaveRequest> holdVM = serverHavePurchase.getHoldVM();
                sinVmTotalNum += holdVM.size();
            }
        }


        int douVmMigrationMaxNum = (int) douVmTotalNum / 1000 * 5;
        int sinVmMigrationMaxNum = (int) sinVmTotalNum / 1000 * 5;

        if (douVmMigrationMaxNum > 1) {

            // 对双节点服务器中拥有的vm数进行排序
            for (List<ServerHavePurchase> serverEveryRange : doubleServerList) {
                for (ServerHavePurchase serverHavePurchase : serverEveryRange) {
                    serverHavePurchase.setHoldVmNum(serverHavePurchase.getHoldVM().size());
                }
                Collections.sort(serverEveryRange, Comparator.comparing(ServerHavePurchase::getHoldVmNum));
            }

            // 对双节点虚拟机进行迁移
            for (List<ServerHavePurchase> serverEveryRange : doubleServerList) {
                for (int i = 0; i < serverEveryRange.size(); i++) {
                    ServerHavePurchase serverHavePurchase = serverEveryRange.get(i);
                    if (serverHavePurchase.getHoldVM() == null && serverHavePurchase.getHoldVM().size() == 0) {
                        continue;
                    } else {
                        List<VMHaveRequest> holdVmList = serverHavePurchase.getHoldVM();
                        for (int k = 0; k < holdVmList.size(); k++) {
                            VMHaveRequest vm = holdVmList.get(k);
                            int vmCore = vm.getCore() / 2;
                            int vmMemory = vm.getMemory() / 2;
                            for (int j = serverEveryRange.size() - 1; j > i; j--) {
                                ServerHavePurchase targetServer = serverEveryRange.get(j);
                                int serverRemainCore = targetServer.getServerNodeA().getRemainCore();
                                int serverRemainMem = targetServer.getServerNodeA().getRemainMemory();
                                if (vmCore <= serverRemainCore && vmMemory <= serverRemainMem) {
                                    vmMigration(vm, serverHavePurchase, targetServer, 2);
                                    List<Integer> serverAndNodeId = new ArrayList<>();
                                    serverAndNodeId.add(targetServer.getId());
                                    vmMigrationMapEveryDay.put(vm.getId(), serverAndNodeId);
                                    if (vmMigrationMapEveryDay.size() >= douVmMigrationMaxNum) {
                                        return vmMigrationMapEveryDay;
                                    }
                                    k--;  // holdVM中的虚拟机被删掉了，要保证遍历时的id正确
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }

        if (sinVmMigrationMaxNum > 1) {
            // 对单节点服务器中拥有的vm数进行排序
            for (List<ServerHavePurchase> serverEveryRange : singleServerList) {
                for (ServerHavePurchase serverHavePurchase : serverEveryRange) {
                    serverHavePurchase.setHoldVmNum(serverHavePurchase.getHoldVM().size());
                }
                Collections.sort(serverEveryRange, Comparator.comparing(ServerHavePurchase::getHoldVmNum));
            }

            // 对单节点虚拟机进行迁移
            for (List<ServerHavePurchase> serverEveryRange : singleServerList) {
                for (int i = 0; i < serverEveryRange.size(); i++) {
                    ServerHavePurchase serverHavePurchase = serverEveryRange.get(i);
                    if (serverHavePurchase.getHoldVM() == null && serverHavePurchase.getHoldVM().size() == 0) {
                        continue;
                    } else {
                        List<VMHaveRequest> holdVmList = serverHavePurchase.getHoldVM();
                        for (int k = 0; k < holdVmList.size(); k++) {
                            VMHaveRequest vm = holdVmList.get(k);
                            int vmCore = vm.getCore();
                            int vmMemory = vm.getMemory();
                            for (int j = serverEveryRange.size() - 1; j > i; j--) {
                                ServerHavePurchase targetServer = serverEveryRange.get(j);
                                int serverRemainCoreA = targetServer.getServerNodeA().getRemainCore();
                                int serverRemainMemA = targetServer.getServerNodeA().getRemainMemory();
                                int serverRemainCoreB = targetServer.getServerNodeB().getRemainCore();
                                int serverRemainMemB = targetServer.getServerNodeB().getRemainMemory();
                                if (vmCore <= serverRemainCoreA && vmMemory <= serverRemainMemA) {
                                    vmMigration(vm, serverHavePurchase, targetServer, 0);
                                    List<Integer> serverAndNodeId = new ArrayList<>();
                                    serverAndNodeId.add(targetServer.getId());
                                    serverAndNodeId.add(0);
                                    vmMigrationMapEveryDay.put(vm.getId(), serverAndNodeId);
                                    if (vmMigrationMapEveryDay.size() >= sinVmMigrationMaxNum + douVmMigrationMaxNum) {
                                        return vmMigrationMapEveryDay;
                                    }
                                    k--;  // holdVM中的虚拟机被删掉了，要保证遍历时的id正确
                                    break;
                                } else if (vmCore <= serverRemainCoreB && vmMemory <= serverRemainMemB) {
                                    vmMigration(vm, serverHavePurchase, targetServer, 1);
                                    List<Integer> serverAndNodeId = new ArrayList<>();
                                    serverAndNodeId.add(targetServer.getId());
                                    serverAndNodeId.add(1);
                                    vmMigrationMapEveryDay.put(vm.getId(), serverAndNodeId);
                                    if (vmMigrationMapEveryDay.size() >= sinVmMigrationMaxNum + douVmMigrationMaxNum) {
                                        return vmMigrationMapEveryDay;
                                    }
                                    k--;  // holdVM中的虚拟机被删掉了，要保证遍历时的id正确
                                    break;
                                }
                            }
                        }

                    }

                }
            }

        }

        return vmMigrationMapEveryDay;
    }

    // 迁移方案3：跨range迁移，将serverList根据装载的虚拟机数由小到大排序，将数量少的服务器中的虚拟机放到数量多的服务器里面，节约服务器
    public static Map<Integer, List<Integer>> startVmMigrationPlan3(Map<String, List<List<ServerHavePurchase>>> serverHaveBuySinAndDouList) {

        Map<Integer, List<Integer>> vmMigrationMapEveryDay = new HashMap<>();
        List<List<ServerHavePurchase>> singleServerList = serverHaveBuySinAndDouList.get("single");
        List<List<ServerHavePurchase>> doubleServerList = serverHaveBuySinAndDouList.get("double");

        float douVmTotalNum = 0;
        float sinVmTotalNum = 0;
        List<ServerHavePurchase> doubleServerListAllRanges = new ArrayList<>();
        List<ServerHavePurchase> singleServerListAllRanges = new ArrayList<>();

        for (List<ServerHavePurchase> serverHavePurchaseList : doubleServerList) {
            for (ServerHavePurchase serverHavePurchase : serverHavePurchaseList) {
                doubleServerListAllRanges.add(serverHavePurchase);
                List<VMHaveRequest> holdVM = serverHavePurchase.getHoldVM();
                douVmTotalNum += holdVM.size();
            }
        }
        for (List<ServerHavePurchase> serverHavePurchaseList : singleServerList) {
            for (ServerHavePurchase serverHavePurchase : serverHavePurchaseList) {
                singleServerListAllRanges.add(serverHavePurchase);
                List<VMHaveRequest> holdVM = serverHavePurchase.getHoldVM();
                sinVmTotalNum += holdVM.size();
            }
        }
        int douVmMigrationMaxNum = (int) douVmTotalNum / 1000 * 5;
        int sinVmMigrationMaxNum = (int) sinVmTotalNum / 1000 * 5;

        if (douVmMigrationMaxNum > 1) {

            // 对双节点服务器中拥有的vm数进行排序

            for (ServerHavePurchase serverHavePurchase : doubleServerListAllRanges) {
                serverHavePurchase.setHoldVmNum(serverHavePurchase.getHoldVM().size());
            }
            Collections.sort(doubleServerListAllRanges, Comparator.comparing(ServerHavePurchase::getHoldVmNum));


            // 对双节点虚拟机进行迁移
            for (int i = 0; i < doubleServerListAllRanges.size(); i++) {
                ServerHavePurchase serverHavePurchase = doubleServerListAllRanges.get(i);
                if (serverHavePurchase.getHoldVM() == null && serverHavePurchase.getHoldVM().size() == 0) {
                    continue;
                } else {
                    List<VMHaveRequest> holdVmList = serverHavePurchase.getHoldVM();
                    for (int k = 0; k < holdVmList.size(); k++) {
                        VMHaveRequest vm = holdVmList.get(k);
                        int vmCore = vm.getCore() / 2;
                        int vmMemory = vm.getMemory() / 2;
                        for (int j = doubleServerListAllRanges.size() - 1; j > i; j--) {
                            ServerHavePurchase targetServer = doubleServerListAllRanges.get(j);
                            int serverRemainCore = targetServer.getServerNodeA().getRemainCore();
                            int serverRemainMem = targetServer.getServerNodeA().getRemainMemory();
                            if (vmCore <= serverRemainCore && vmMemory <= serverRemainMem) {
                                vmMigration(vm, serverHavePurchase, targetServer, 2);
                                List<Integer> serverAndNodeId = new ArrayList<>();
                                serverAndNodeId.add(targetServer.getId());
                                vmMigrationMapEveryDay.put(vm.getId(), serverAndNodeId);
                                if (vmMigrationMapEveryDay.size() >= douVmMigrationMaxNum) {
                                    return vmMigrationMapEveryDay;
                                }
                                k--;  // holdVM中的虚拟机被删掉了，要保证遍历时的id正确
                                break;
                            }
                        }
                    }
                }
            }
        }


        if (sinVmMigrationMaxNum > 1) {
            // 对单节点服务器中拥有的vm数进行排序

            for (ServerHavePurchase serverHavePurchase : singleServerListAllRanges) {
                serverHavePurchase.setHoldVmNum(serverHavePurchase.getHoldVM().size());
            }
            Collections.sort(singleServerListAllRanges, Comparator.comparing(ServerHavePurchase::getHoldVmNum));


            // 对单节点虚拟机进行迁移
            for (int i = 0; i < singleServerListAllRanges.size(); i++) {
                ServerHavePurchase serverHavePurchase = singleServerListAllRanges.get(i);
                if (serverHavePurchase.getHoldVM() == null && serverHavePurchase.getHoldVM().size() == 0) {
                    continue;
                } else {
                    List<VMHaveRequest> holdVmList = serverHavePurchase.getHoldVM();
                    for (int k = 0; k < holdVmList.size(); k++) {
                        VMHaveRequest vm = holdVmList.get(k);
                        int vmCore = vm.getCore();
                        int vmMemory = vm.getMemory();
                        for (int j = singleServerListAllRanges.size() - 1; j > i; j--) {
                            ServerHavePurchase targetServer = singleServerListAllRanges.get(j);
                            int serverRemainCoreA = targetServer.getServerNodeA().getRemainCore();
                            int serverRemainMemA = targetServer.getServerNodeA().getRemainMemory();
                            int serverRemainCoreB = targetServer.getServerNodeB().getRemainCore();
                            int serverRemainMemB = targetServer.getServerNodeB().getRemainMemory();
                            if (vmCore <= serverRemainCoreA && vmMemory <= serverRemainMemA) {
                                vmMigration(vm, serverHavePurchase, targetServer, 0);
                                List<Integer> serverAndNodeId = new ArrayList<>();
                                serverAndNodeId.add(targetServer.getId());
                                serverAndNodeId.add(0);
                                vmMigrationMapEveryDay.put(vm.getId(), serverAndNodeId);
                                if (vmMigrationMapEveryDay.size() >= sinVmMigrationMaxNum + douVmMigrationMaxNum) {
                                    return vmMigrationMapEveryDay;
                                }
                                k--;  // holdVM中的虚拟机被删掉了，要保证遍历时的id正确
                                break;
                            } else if (vmCore <= serverRemainCoreB && vmMemory <= serverRemainMemB) {
                                vmMigration(vm, serverHavePurchase, targetServer, 1);
                                List<Integer> serverAndNodeId = new ArrayList<>();
                                serverAndNodeId.add(targetServer.getId());
                                serverAndNodeId.add(1);
                                vmMigrationMapEveryDay.put(vm.getId(), serverAndNodeId);
                                if (vmMigrationMapEveryDay.size() >= sinVmMigrationMaxNum + douVmMigrationMaxNum) {
                                    return vmMigrationMapEveryDay;
                                }
                                k--;  // holdVM中的虚拟机被删掉了，要保证遍历时的id正确
                                break;
                            }
                        }
                    }

                }

            }
        }

        return vmMigrationMapEveryDay;
    }


    // 迁移方案3_1：跨range迁移，将serverList根据剩余的m+c由大到小排序，将数量少的服务器中的虚拟机放到数量多的服务器里面，节约服务器
    public static Map<Integer, List<Integer>> startVmMigrationPlan3_1(Map<String, List<List<ServerHavePurchase>>> serverHaveBuySinAndDouList) {

        Map<Integer, List<Integer>> vmMigrationMapEveryDay = new HashMap<>();
        List<List<ServerHavePurchase>> singleServerList = serverHaveBuySinAndDouList.get("single");
        List<List<ServerHavePurchase>> doubleServerList = serverHaveBuySinAndDouList.get("double");

        float douVmTotalNum = 0;
        float sinVmTotalNum = 0;
        List<ServerHavePurchase> doubleServerListAllRanges = new ArrayList<>();
        List<ServerHavePurchase> singleServerListAllRanges = new ArrayList<>();

        for (List<ServerHavePurchase> serverHavePurchaseList : doubleServerList) {
            for (ServerHavePurchase serverHavePurchase : serverHavePurchaseList) {
                doubleServerListAllRanges.add(serverHavePurchase);
                serverHavePurchase.setMcRatioSum();
                List<VMHaveRequest> holdVM = serverHavePurchase.getHoldVM();
                douVmTotalNum += holdVM.size();
            }
        }
        for (List<ServerHavePurchase> serverHavePurchaseList : singleServerList) {
            for (ServerHavePurchase serverHavePurchase : serverHavePurchaseList) {
                singleServerListAllRanges.add(serverHavePurchase);
                List<VMHaveRequest> holdVM = serverHavePurchase.getHoldVM();
                sinVmTotalNum += holdVM.size();
                serverHavePurchase.setMcRatioSum();
            }
        }
        int douVmMigrationMaxNum = (int) douVmTotalNum / 1000 * 5;
        int sinVmMigrationMaxNum = (int) sinVmTotalNum / 1000 * 5;


        if (douVmMigrationMaxNum > 1) {
            // 对双节点服务器中拥有的vm数进行排序

            Collections.sort(doubleServerListAllRanges, Comparator.comparing(ServerHavePurchase::getMcRatioSum).reversed());

            // 对双节点虚拟机进行迁移
            for (int i = 0; i < doubleServerListAllRanges.size(); i++) {
                ServerHavePurchase serverHavePurchase = doubleServerListAllRanges.get(i);
                if (serverHavePurchase.getHoldVM() == null && serverHavePurchase.getHoldVM().size() == 0) {
                    continue;
                } else {
                    List<VMHaveRequest> holdVmList = serverHavePurchase.getHoldVM();
                    for (int k = 0; k < holdVmList.size(); k++) {
                        VMHaveRequest vm = holdVmList.get(k);
                        int vmCore = vm.getCore() / 2;
                        int vmMemory = vm.getMemory() / 2;
                        for (int j = doubleServerListAllRanges.size() - 1; j > i; j--) {
                            ServerHavePurchase targetServer = doubleServerListAllRanges.get(j);
                            int serverRemainCore = targetServer.getServerNodeA().getRemainCore();
                            int serverRemainMem = targetServer.getServerNodeA().getRemainMemory();
                            if (vmCore <= serverRemainCore && vmMemory <= serverRemainMem) {
                                vmMigration(vm, serverHavePurchase, targetServer, 2);
                                List<Integer> serverAndNodeId = new ArrayList<>();
                                serverAndNodeId.add(targetServer.getId());
                                vmMigrationMapEveryDay.put(vm.getId(), serverAndNodeId);
                                if (vmMigrationMapEveryDay.size() >= douVmMigrationMaxNum + sinVmMigrationMaxNum) {
                                    return vmMigrationMapEveryDay;
                                }
                                k--;  // holdVM中的虚拟机被删掉了，要保证遍历时的id正确
                                break;
                            }
                        }
                    }
                }
            }
        }

        if (sinVmMigrationMaxNum > 1) {
            // 对单节点服务器中拥有的vm数进行排序

            Collections.sort(singleServerListAllRanges, Comparator.comparing(ServerHavePurchase::getMcRatioSum).reversed());


            // 对单节点虚拟机进行迁移
            for (int i = 0; i < singleServerListAllRanges.size(); i++) {
                ServerHavePurchase serverHavePurchase = singleServerListAllRanges.get(i);
                if (serverHavePurchase.getHoldVM() == null && serverHavePurchase.getHoldVM().size() == 0) {
                    continue;
                } else {
                    List<VMHaveRequest> holdVmList = serverHavePurchase.getHoldVM();
                    for (int k = 0; k < holdVmList.size(); k++) {
                        VMHaveRequest vm = holdVmList.get(k);
                        int vmCore = vm.getCore();
                        int vmMemory = vm.getMemory();
                        for (int j = singleServerListAllRanges.size() - 1; j > i; j--) {
                            ServerHavePurchase targetServer = singleServerListAllRanges.get(j);
                            int serverRemainCoreA = targetServer.getServerNodeA().getRemainCore();
                            int serverRemainMemA = targetServer.getServerNodeA().getRemainMemory();
                            int serverRemainCoreB = targetServer.getServerNodeB().getRemainCore();
                            int serverRemainMemB = targetServer.getServerNodeB().getRemainMemory();
                            if (vmCore <= serverRemainCoreA && vmMemory <= serverRemainMemA) {
                                vmMigration(vm, serverHavePurchase, targetServer, 0);
                                List<Integer> serverAndNodeId = new ArrayList<>();
                                serverAndNodeId.add(targetServer.getId());
                                serverAndNodeId.add(0);
                                vmMigrationMapEveryDay.put(vm.getId(), serverAndNodeId);
                                if (vmMigrationMapEveryDay.size() >= sinVmMigrationMaxNum + douVmMigrationMaxNum) {
                                    return vmMigrationMapEveryDay;
                                }
                                k--;  // holdVM中的虚拟机被删掉了，要保证遍历时的id正确
                                break;
                            } else if (vmCore <= serverRemainCoreB && vmMemory <= serverRemainMemB) {
                                vmMigration(vm, serverHavePurchase, targetServer, 1);
                                List<Integer> serverAndNodeId = new ArrayList<>();
                                serverAndNodeId.add(targetServer.getId());
                                serverAndNodeId.add(1);
                                vmMigrationMapEveryDay.put(vm.getId(), serverAndNodeId);
                                if (vmMigrationMapEveryDay.size() >= sinVmMigrationMaxNum + douVmMigrationMaxNum) {
                                    return vmMigrationMapEveryDay;
                                }
                                k--;  // holdVM中的虚拟机被删掉了，要保证遍历时的id正确
                                break;
                            }
                        }
                    }

                }

            }
        }


        return vmMigrationMapEveryDay;
    }


    public synchronized static void putInMap(Map<Integer, List<Integer>> map, Integer id, List<Integer> serverAndNodeId) {
        map.put(id, serverAndNodeId);
    }

    // 迁移方案3_1thread：跨range迁移，将serverList根据剩余的m+c由大到小排序，将数量少的服务器中的虚拟机放到数量多的服务器里面，节约服务器
    public static Map<Integer, List<Integer>> startVmMigrationPlan3_1_thread(Map<String, List<List<ServerHavePurchase>>> serverHaveBuySinAndDouList) {

        Map<Integer, List<Integer>> vmMigrationMapEveryDay = new ConcurrentHashMap<>();
        List<List<ServerHavePurchase>> singleServerList = serverHaveBuySinAndDouList.get("single");
        List<List<ServerHavePurchase>> doubleServerList = serverHaveBuySinAndDouList.get("double");

        float douVmTotalNum = 0;
        float sinVmTotalNum = 0;
        List<ServerHavePurchase> doubleServerListAllRanges = new ArrayList<>();
        List<ServerHavePurchase> singleServerListAllRanges = new ArrayList<>();

        for (List<ServerHavePurchase> serverHavePurchaseList : doubleServerList) {
            for (ServerHavePurchase serverHavePurchase : serverHavePurchaseList) {
                doubleServerListAllRanges.add(serverHavePurchase);
                serverHavePurchase.setMcRatioSum();
                List<VMHaveRequest> holdVM = serverHavePurchase.getHoldVM();
                douVmTotalNum += holdVM.size();
            }
        }
        for (List<ServerHavePurchase> serverHavePurchaseList : singleServerList) {
            for (ServerHavePurchase serverHavePurchase : serverHavePurchaseList) {
                singleServerListAllRanges.add(serverHavePurchase);
                List<VMHaveRequest> holdVM = serverHavePurchase.getHoldVM();
                sinVmTotalNum += holdVM.size();
                serverHavePurchase.setMcRatioSum();
            }
        }
        int douVmMigrationMaxNum = (int) douVmTotalNum / 1000 * 5;
        int sinVmMigrationMaxNum = (int) sinVmTotalNum / 1000 * 5;

        final CountDownLatch latch = new CountDownLatch(2);
        Thread doubleThread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (douVmMigrationMaxNum > 1) {
                    // 对双节点服务器中拥有的vm数进行排序
                    Collections.sort(doubleServerListAllRanges, Comparator.comparing(ServerHavePurchase::getMcRatioSum).reversed());
                    // 对双节点虚拟机进行迁移
                    for (int i = 0; i < doubleServerListAllRanges.size(); i++) {
                        ServerHavePurchase serverHavePurchase = doubleServerListAllRanges.get(i);
                        if (serverHavePurchase.getHoldVM() == null && serverHavePurchase.getHoldVM().size() == 0) {
                            continue;
                        } else {
                            List<VMHaveRequest> holdVmList = serverHavePurchase.getHoldVM();
                            for (int k = 0; k < holdVmList.size(); k++) {
                                VMHaveRequest vm = holdVmList.get(k);
                                int vmCore = vm.getCore() / 2;
                                int vmMemory = vm.getMemory() / 2;
                                for (int j = doubleServerListAllRanges.size() - 1; j > i; j--) {
                                    ServerHavePurchase targetServer = doubleServerListAllRanges.get(j);
                                    int serverRemainCore = targetServer.getServerNodeA().getRemainCore();
                                    int serverRemainMem = targetServer.getServerNodeA().getRemainMemory();
                                    if (vmCore <= serverRemainCore && vmMemory <= serverRemainMem) {
                                        if (vmMigrationMapEveryDay.size() >= douVmMigrationMaxNum + sinVmMigrationMaxNum) {
                                            latch.countDown();
                                            return;
                                        }
                                        vmMigration(vm, serverHavePurchase, targetServer, 2);
                                        List<Integer> serverAndNodeId = new ArrayList<>();
                                        serverAndNodeId.add(targetServer.getId());
//                                vmMigrationMapEveryDay.put(vm.getId(), serverAndNodeId);
                                        putInMap(vmMigrationMapEveryDay, vm.getId(), serverAndNodeId);

                                        k--;  // holdVM中的虚拟机被删掉了，要保证遍历时的id正确
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    latch.countDown();
                } else {
                    latch.countDown();
                }
            }
        });


        Thread singleThread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (sinVmMigrationMaxNum > 1) {
                    // 对单节点服务器中拥有的vm数进行排序

                    Collections.sort(singleServerListAllRanges, Comparator.comparing(ServerHavePurchase::getMcRatioSum).reversed());


                    // 对单节点虚拟机进行迁移
                    for (int i = 0; i < singleServerListAllRanges.size(); i++) {
                        ServerHavePurchase serverHavePurchase = singleServerListAllRanges.get(i);
                        if (serverHavePurchase.getHoldVM() == null && serverHavePurchase.getHoldVM().size() == 0) {
                            continue;
                        } else {
                            List<VMHaveRequest> holdVmList = serverHavePurchase.getHoldVM();
                            for (int k = 0; k < holdVmList.size(); k++) {
                                VMHaveRequest vm = holdVmList.get(k);
                                int vmCore = vm.getCore();
                                int vmMemory = vm.getMemory();
                                for (int j = singleServerListAllRanges.size() - 1; j > i; j--) {
                                    ServerHavePurchase targetServer = singleServerListAllRanges.get(j);
                                    int serverRemainCoreA = targetServer.getServerNodeA().getRemainCore();
                                    int serverRemainMemA = targetServer.getServerNodeA().getRemainMemory();
                                    int serverRemainCoreB = targetServer.getServerNodeB().getRemainCore();
                                    int serverRemainMemB = targetServer.getServerNodeB().getRemainMemory();
                                    if (vmCore <= serverRemainCoreA && vmMemory <= serverRemainMemA) {
                                        if (vmMigrationMapEveryDay.size() >= sinVmMigrationMaxNum + douVmMigrationMaxNum) {
                                            latch.countDown();
                                            return;
                                        }
                                        vmMigration(vm, serverHavePurchase, targetServer, 0);
                                        List<Integer> serverAndNodeId = new ArrayList<>();
                                        serverAndNodeId.add(targetServer.getId());
                                        serverAndNodeId.add(0);
//                                vmMigrationMapEveryDay.put(vm.getId(), serverAndNodeId);
                                        putInMap(vmMigrationMapEveryDay, vm.getId(), serverAndNodeId);

                                        k--;  // holdVM中的虚拟机被删掉了，要保证遍历时的id正确
                                        break;
                                    } else if (vmCore <= serverRemainCoreB && vmMemory <= serverRemainMemB) {
                                        if (vmMigrationMapEveryDay.size() >= sinVmMigrationMaxNum + douVmMigrationMaxNum) {
                                            latch.countDown();
                                            return;
                                        }
                                        vmMigration(vm, serverHavePurchase, targetServer, 1);
                                        List<Integer> serverAndNodeId = new ArrayList<>();
                                        serverAndNodeId.add(targetServer.getId());
                                        serverAndNodeId.add(1);
//                                vmMigrationMapEveryDay.put(vm.getId(), serverAndNodeId);
                                        putInMap(vmMigrationMapEveryDay, vm.getId(), serverAndNodeId);

                                        k--;  // holdVM中的虚拟机被删掉了，要保证遍历时的id正确
                                        break;
                                    }
                                }
                            }

                        }

                    }
                    latch.countDown();
                } else {
                    latch.countDown();
                }
            }
        });
        doubleThread.start();
        singleThread.start();

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return vmMigrationMapEveryDay;
    }



    // 迁移方案3_2thread：    跨range迁移，将serverList根据剩余的m+c由大到小排序，将数量少的服务器中的虚拟机放到数量多的服务器里面，节约服务器
    public static Map<Integer, List<Integer>> startVmMigrationPlan3_2_thread(Map<String, List<List<ServerHavePurchase>>> serverHaveBuySinAndDouList,
                                                                             Map<Integer, ServerHavePurchase>serverIdMap) {

        Map<Integer, List<Integer>> vmMigrationMapEveryDay = new HashMap<>();




        int vmMigrationMaxNum = Main.vmNum/1000*5;


        if(vmMigrationMaxNum==0){
            return vmMigrationMapEveryDay;
        }

        final CountDownLatch latch = new CountDownLatch(2);
        Thread doubleThread = new Thread(new Runnable() {
            @Override
            public void run() {
                List<List<ServerHavePurchase>> doubleServerList = serverHaveBuySinAndDouList.get("double");


                List<ServerHavePurchase> doubleServerListAllRanges = new ArrayList<>();
                for (List<ServerHavePurchase> serverHavePurchaseList : doubleServerList) {
                    for (ServerHavePurchase serverHavePurchase : serverHavePurchaseList) {
                        doubleServerListAllRanges.add(serverHavePurchase);
                        serverHavePurchase.setMcRatioSum();

                    }
                }
                if (vmMigrationMaxNum >= 1) {

                    List<ServerHavePurchase> douServerHavePurchaseList=new ArrayList<>();
                    // 对双节点服务器中拥有的vm数进行排序
                    Collections.sort(doubleServerListAllRanges, Comparator.comparing(ServerHavePurchase::getMcRatioSum).reversed());
                    for (int i =doubleServerListAllRanges.size()-1; i >0; i--) {
                        ServerHavePurchase serverHavePurchase = doubleServerListAllRanges.get(i);
                        int totalRemain = serverHavePurchase.getServerNodeA().getRemainCore() +
                                serverHavePurchase.getServerNodeB().getRemainCore() +
                                serverHavePurchase.getServerNodeA().getRemainMemory() +
                                serverHavePurchase.getServerNodeB().getRemainMemory();
                        if(totalRemain>=4){
                            douServerHavePurchaseList = doubleServerListAllRanges.subList(0, i + 1);
                            break;
                        }
                    }

                    // 对双节点虚拟机进行迁移
                    for (int i = 0; i < douServerHavePurchaseList.size(); i++) {
                        ServerHavePurchase serverHavePurchase = douServerHavePurchaseList.get(i);

                        if (serverHavePurchase.getHoldVM() == null || serverHavePurchase.getHoldVM().size() == 0) {
                            continue;
                        } else {
                            List<VMHaveRequest> holdVmList = serverHavePurchase.getHoldVM();
                            for (int k = 0; k < holdVmList.size(); k++) {
                                VMHaveRequest vm = holdVmList.get(k);
                                int vmCore = vm.getCore() / 2;
                                int vmMemory = vm.getMemory() / 2;
                                for (int j = douServerHavePurchaseList.size() - 1; j > i; j--) {
                                    ServerHavePurchase targetServer = douServerHavePurchaseList.get(j);
                                    int serverRemainCore = targetServer.getServerNodeA().getRemainCore();
                                    int serverRemainMem = targetServer.getServerNodeA().getRemainMemory();
                                    if (vmCore <= serverRemainCore && vmMemory <= serverRemainMem) {
                                        if (vmMigrationMapEveryDay.size() >= vmMigrationMaxNum-1) {
                                            latch.countDown();
                                            return;
                                        }
                                        vmMigration(vm, serverHavePurchase, targetServer, 2);
                                        List<Integer> serverAndNodeId = new ArrayList<>();
                                        serverAndNodeId.add(targetServer.getId());
                                        putInMap(vmMigrationMapEveryDay, vm.getId(), serverAndNodeId);
                                        k--;  // holdVM中的虚拟机被删掉了，要保证遍历时的id正确
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    latch.countDown();
                } else {
                    latch.countDown();
                }
            }
        });

        Thread singleThread = new Thread(new Runnable() {
            @Override
            public void run() {
                List<List<ServerHavePurchase>> singleServerList = serverHaveBuySinAndDouList.get("single");

                List<ServerHavePurchase> singleServerListAllRanges = new ArrayList<>();
                for (List<ServerHavePurchase> serverHavePurchaseList : singleServerList) {
                    for (ServerHavePurchase serverHavePurchase : serverHavePurchaseList) {
                        singleServerListAllRanges.add(serverHavePurchase);

                        serverHavePurchase.setMcRatioSum();
                    }
                }
                if (vmMigrationMaxNum >=1) {

                    // 对单节点服务器中拥有的
                    Collections.sort(singleServerListAllRanges, Comparator.comparing(ServerHavePurchase::getMcRatioSum).reversed());

                    List<ServerHavePurchase> sinServerHavePurchaseList=new ArrayList<>();
                    // 对双节点服务器中拥有的vm数进行排序
                    Collections.sort(singleServerListAllRanges, Comparator.comparing(ServerHavePurchase::getMcRatioSum).reversed());
                    for (int i =singleServerListAllRanges.size()-1; i >0; i--) {
                        ServerHavePurchase serverHavePurchase = singleServerListAllRanges.get(i);
                        int totalRemainA = serverHavePurchase.getServerNodeA().getRemainCore() +serverHavePurchase.getServerNodeA().getRemainMemory();
                        int totalRemainB = serverHavePurchase.getServerNodeB().getRemainCore()+serverHavePurchase.getServerNodeB().getRemainMemory();
                        if(totalRemainA>=4 ||totalRemainB>=4 ){
                            sinServerHavePurchaseList = singleServerListAllRanges.subList(0, i + 1);
                            break;
                        }
                    }

                    // 对单节点虚拟机进行迁移
                    for (int i = 0; i < sinServerHavePurchaseList.size(); i++) {
                        ServerHavePurchase serverHavePurchase = sinServerHavePurchaseList.get(i);
                        if (serverHavePurchase.getHoldVM() == null || serverHavePurchase.getHoldVM().size() == 0) {
                            continue;
                        } else {
                            List<VMHaveRequest> holdVmList = serverHavePurchase.getHoldVM();
                            for (int k = 0; k < holdVmList.size(); k++) {
                                VMHaveRequest vm = holdVmList.get(k);
                                int vmCore = vm.getCore();
                                int vmMemory = vm.getMemory();
                                for (int j = sinServerHavePurchaseList.size() - 1; j > i; j--) {
                                    ServerHavePurchase targetServer = sinServerHavePurchaseList.get(j);
                                    int serverRemainCoreA = targetServer.getServerNodeA().getRemainCore();
                                    int serverRemainMemA = targetServer.getServerNodeA().getRemainMemory();
                                    int serverRemainCoreB = targetServer.getServerNodeB().getRemainCore();
                                    int serverRemainMemB = targetServer.getServerNodeB().getRemainMemory();
                                    if (vmCore <= serverRemainCoreA && vmMemory <= serverRemainMemA) {
                                        if (vmMigrationMapEveryDay.size() >= vmMigrationMaxNum-1) {
                                            latch.countDown();
                                            return;
                                        }
                                        vmMigration(vm, serverHavePurchase, targetServer, 0);
                                        List<Integer> serverAndNodeId = new ArrayList<>();
                                        serverAndNodeId.add(targetServer.getId());
                                        serverAndNodeId.add(0);
                                        putInMap(vmMigrationMapEveryDay, vm.getId(), serverAndNodeId);

                                        k--;  // holdVM中的虚拟机被删掉了，要保证遍历时的id正确
                                        break;
                                    } else if (vmCore <= serverRemainCoreB && vmMemory <= serverRemainMemB) {
                                        if (vmMigrationMapEveryDay.size() >= vmMigrationMaxNum) {
                                            latch.countDown();
                                            return;
                                        }
                                        vmMigration(vm, serverHavePurchase, targetServer, 1);
                                        List<Integer> serverAndNodeId = new ArrayList<>();
                                        serverAndNodeId.add(targetServer.getId());
                                        serverAndNodeId.add(1);
//                                vmMigrationMapEveryDay.put(vm.getId(), serverAndNodeId);
                                        putInMap(vmMigrationMapEveryDay, vm.getId(), serverAndNodeId);

                                        k--;  // holdVM中的虚拟机被删掉了，要保证遍历时的id正确
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    latch.countDown();
                } else {
                    latch.countDown();
                }
            }
        });


        doubleThread.start();
        singleThread.start();

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Main.vmMigrationMapEveryDay2=new ArrayList<>();

//        if (vmMigrationMaxNum- vmMigrationMapEveryDay.size()==1) {
//
////            List<ServerHavePurchase> singleServerListAllRangesRemain = new ArrayList<>();
//
//            for (ServerHavePurchase serverHavePurchase : singleServerListAllRanges) {
//                serverHavePurchase.setMcRatioSum();
//            }
//
////            // 对双节点服务器中拥有的vm数进行排序
//            Collections.sort(singleServerListAllRanges, Comparator.comparing(ServerHavePurchase::getMcRatioSum).reversed());
//
//            // 对双节点服务器中拥有的vm数进行排序
//
//            // 对单节点虚拟机进行迁移
//            for (int i = 0; i < singleServerListAllRanges.size(); i++) {
//                ServerHavePurchase serverHavePurchase = singleServerListAllRanges.get(i);
//                if (serverHavePurchase.getHoldVM() == null || serverHavePurchase.getHoldVM().size() == 0) {
//                    continue;
//                }
//                else {
//                    List<VMHaveRequest> holdVmList = serverHavePurchase.getHoldVM();
//                    for (int k = 0; k < holdVmList.size(); k++) {
//                        VMHaveRequest vm = holdVmList.get(k);
//                        int vmCore = vm.getCore();
//                        int vmMemory = vm.getMemory();
//                        for (int j = singleServerListAllRanges.size() /2- 1; j > i; j--) {
//                            ServerHavePurchase targetServer = singleServerListAllRanges.get(j);
//                            int serverRemainCoreA = targetServer.getServerNodeA().getRemainCore();
//                            int serverRemainMemA = targetServer.getServerNodeA().getRemainMemory();
//                            int serverRemainCoreB = targetServer.getServerNodeB().getRemainCore();
//                            int serverRemainMemB = targetServer.getServerNodeB().getRemainMemory();
//                            if (vmCore <= serverRemainCoreA && vmMemory <= serverRemainMemA) {
//                                if (vmMigrationMapEveryDay.size() >= vmMigrationMaxNum) {
//                                    return vmMigrationMapEveryDay;
//                                }
//                                vmMigration(vm, serverHavePurchase, targetServer, 0);
//                                List<Integer> serverAndNodeId = new ArrayList<>();
//                                serverAndNodeId.add(targetServer.getId());
//                                serverAndNodeId.add(0);
//                                Map<Integer, List<Integer>> tmpMap=new HashMap<>();
//                                tmpMap.put(vm.getId(),serverAndNodeId);
//                                Main.vmMigrationMapEveryDay2.add(tmpMap);
//                                return vmMigrationMapEveryDay;
//                            } else if (vmCore <= serverRemainCoreB && vmMemory <= serverRemainMemB) {
//                                if (vmMigrationMapEveryDay.size() >= vmMigrationMaxNum) {
//                                    return vmMigrationMapEveryDay;
//                                }
//                                vmMigration(vm, serverHavePurchase, targetServer, 1);
//                                List<Integer> serverAndNodeId = new ArrayList<>();
//                                serverAndNodeId.add(targetServer.getId());
//                                serverAndNodeId.add(1);
//                                Map<Integer, List<Integer>> tmpMap=new HashMap<>();
//                                tmpMap.put(vm.getId(),serverAndNodeId);
//                                Main.vmMigrationMapEveryDay2.add(tmpMap);
//                                return vmMigrationMapEveryDay;
//
//                            }
//                        }
//                    }
//                }
//            }
//
//        }
//        if (vmMigrationMaxNum- vmMigrationMapEveryDay.size() == 1) {
//
//            for (ServerHavePurchase serverHavePurchase : doubleServerListAllRanges) {
//                serverHavePurchase.setMcRatioSum();
//            }
//            // 对双节点服务器中拥有的vm数进行排序
//            Collections.sort(doubleServerListAllRanges, Comparator.comparing(ServerHavePurchase::getMcRatioSum).reversed());
//
//            for (int i = 0; i < doubleServerListAllRanges.size(); i++) {
//                ServerHavePurchase serverHavePurchase = doubleServerListAllRanges.get(i);
//                if (serverHavePurchase.getHoldVM() == null || serverHavePurchase.getHoldVM().size() == 0) {
//                    continue;
//                } else {
//                    List<VMHaveRequest> holdVmList = serverHavePurchase.getHoldVM();
//                    for (int k = 0; k < holdVmList.size(); k++) {
//                        VMHaveRequest vm = holdVmList.get(k);
//                        int vmCore = vm.getCore() / 2;
//                        int vmMemory = vm.getMemory() / 2;
//                        for (int j = doubleServerListAllRanges.size() - 1; j > i; j--) {
//                            ServerHavePurchase targetServer = doubleServerListAllRanges.get(j);
//                            int serverRemainCore = targetServer.getServerNodeA().getRemainCore();
//                            int serverRemainMem = targetServer.getServerNodeA().getRemainMemory();
//                            if (vmCore <= serverRemainCore && vmMemory <= serverRemainMem) {
//                                if (vmMigrationMapEveryDay.size() >= vmMigrationMaxNum) {
//
//                                    return vmMigrationMapEveryDay;
//                                }
//                                vmMigration(vm, serverHavePurchase, targetServer, 2);
//                                List<Integer> serverAndNodeId = new ArrayList<>();
//                                serverAndNodeId.add(targetServer.getId());
//                                Map<Integer, List<Integer>> tmpMap=new HashMap<>();
//                                tmpMap.put(vm.getId(),serverAndNodeId);
//                                Main.vmMigrationMapEveryDay2.add(tmpMap);
//                                return vmMigrationMapEveryDay;
//                            }
//                        }
//                    }
//                }
//            }
//
//        }


//        int migrationMaxNum = vmMigrationMaxNum-vmMigrationMapEveryDay.size()-Main.vmMigrationMapEveryDay2.size();
//        if(migrationMaxNum>=1) {
//            List<List<ServerHavePurchase>> singleServerList = serverHaveBuySinAndDouList.get("single");
//            List<List<ServerHavePurchase>> doubleServerList = serverHaveBuySinAndDouList.get("double");
//            List<List<ServerHavePurchase>> douServerLists = DivideIntervalzyl.divideServerByRemainMemCoreEightRange1(doubleServerList, 0, Main.rangeVM);
//            List<List<ServerHavePurchase>> sinServerLists = DivideIntervalzyl.divideServerByRemainMemCoreEightRange1(singleServerList, 1, Main.rangeVM);
//            migrationSinVmInRange0(Main.vmSinCoreMap, Main.vmSinMemoryMap, sinServerLists,
//                    Main.maxGateValue, Main.minGateValue,serverIdMap, Main.vmMigrationMapEveryDay2, migrationMaxNum);
//
//            migrationMaxNum = vmMigrationMaxNum-vmMigrationMapEveryDay.size()-Main.vmMigrationMapEveryDay2.size();
//            if(migrationMaxNum>=1) {
//                migrationDouVmInRange0(Main.vmDouCoreMap, Main.vmDouMemoryMap, douServerLists,
//                        Main.maxGateValue,Main.minGateValue, serverIdMap, Main.vmMigrationMapEveryDay2, migrationMaxNum);
//            }

//        }

        return vmMigrationMapEveryDay;
    }



    // 迁移方案7：跨range迁移，将serverList根据装载的虚拟机数由小到大排序，将数量少的服务器中的虚拟机放到数量多的服务器里面，节约服务器
    public static Map<Integer, List<Integer>> startVmMigrationPlan7(Map<String, List<List<ServerHavePurchase>>> serverHaveBuySinAndDouList) {

        Map<Integer, List<Integer>> vmMigrationMapEveryDay = new HashMap<>();
        List<List<ServerHavePurchase>> singleServerList = serverHaveBuySinAndDouList.get("single");
        List<List<ServerHavePurchase>> doubleServerList = serverHaveBuySinAndDouList.get("double");

        float douVmTotalNum = 0;
        float sinVmTotalNum = 0;
        List<ServerHavePurchase> doubleServerListAllRanges = new ArrayList<>();
        List<ServerHavePurchase> singleServerListAllRanges = new ArrayList<>();

        for (List<ServerHavePurchase> serverHavePurchaseList : doubleServerList) {
            for (ServerHavePurchase serverHavePurchase : serverHavePurchaseList) {
                doubleServerListAllRanges.add(serverHavePurchase);
                List<VMHaveRequest> holdVM = serverHavePurchase.getHoldVM();
                douVmTotalNum += holdVM.size();
            }
        }
        for (List<ServerHavePurchase> serverHavePurchaseList : singleServerList) {
            for (ServerHavePurchase serverHavePurchase : serverHavePurchaseList) {
                singleServerListAllRanges.add(serverHavePurchase);
                List<VMHaveRequest> holdVM = serverHavePurchase.getHoldVM();
                sinVmTotalNum += holdVM.size();
            }
        }
        int douVmMigrationMaxNum = (int) douVmTotalNum / 1000 * 5;
        int sinVmMigrationMaxNum = (int) sinVmTotalNum / 1000 * 5;

        if (douVmMigrationMaxNum > 1) {

            // 对双节点服务器中拥有的vm数进行排序

            for (ServerHavePurchase serverHavePurchase : doubleServerListAllRanges) {
                serverHavePurchase.setHoldVmNum(serverHavePurchase.getHoldVM().size());
            }
            Collections.sort(doubleServerListAllRanges, Comparator.comparing(ServerHavePurchase::getHoldVmNum)
                    .thenComparing(ServerHavePurchase::getEnergyConsumptionPerDay));


            // 对双节点虚拟机进行迁移
            for (int i = 0; i < doubleServerListAllRanges.size(); i++) {
                ServerHavePurchase serverHavePurchase = doubleServerListAllRanges.get(i);
                if (serverHavePurchase.getHoldVM() == null && serverHavePurchase.getHoldVM().size() == 0) {
                    continue;
                } else {
                    List<VMHaveRequest> holdVmList = serverHavePurchase.getHoldVM();
                    for (int k = 0; k < holdVmList.size(); k++) {
                        VMHaveRequest vm = holdVmList.get(k);
                        int vmCore = vm.getCore() / 2;
                        int vmMemory = vm.getMemory() / 2;
                        for (int j = doubleServerListAllRanges.size() - 1; j > i; j--) {
                            ServerHavePurchase targetServer = doubleServerListAllRanges.get(j);
                            int serverRemainCore = targetServer.getServerNodeA().getRemainCore();
                            int serverRemainMem = targetServer.getServerNodeA().getRemainMemory();
                            if (vmCore <= serverRemainCore && vmMemory <= serverRemainMem) {
                                vmMigration(vm, serverHavePurchase, targetServer, 2);
                                List<Integer> serverAndNodeId = new ArrayList<>();
                                serverAndNodeId.add(targetServer.getId());
                                vmMigrationMapEveryDay.put(vm.getId(), serverAndNodeId);
                                if (vmMigrationMapEveryDay.size() >= douVmMigrationMaxNum) {
                                    return vmMigrationMapEveryDay;
                                }
                                k--;  // holdVM中的虚拟机被删掉了，要保证遍历时的id正确
                                break;
                            }
                        }
                    }
                }
            }
        }


        if (sinVmMigrationMaxNum > 1) {
            // 对单节点服务器中拥有的vm数进行排序

            for (ServerHavePurchase serverHavePurchase : singleServerListAllRanges) {
                serverHavePurchase.setHoldVmNum(serverHavePurchase.getHoldVM().size());
            }
            Collections.sort(singleServerListAllRanges, Comparator.comparing(ServerHavePurchase::getHoldVmNum)
                    .thenComparing(ServerHavePurchase::getEnergyConsumptionPerDay));


            // 对单节点虚拟机进行迁移
            for (int i = 0; i < singleServerListAllRanges.size(); i++) {
                ServerHavePurchase serverHavePurchase = singleServerListAllRanges.get(i);
                if (serverHavePurchase.getHoldVM() == null && serverHavePurchase.getHoldVM().size() == 0) {
                    continue;
                } else {
                    List<VMHaveRequest> holdVmList = serverHavePurchase.getHoldVM();
                    for (int k = 0; k < holdVmList.size(); k++) {
                        VMHaveRequest vm = holdVmList.get(k);
                        int vmCore = vm.getCore();
                        int vmMemory = vm.getMemory();
                        for (int j = singleServerListAllRanges.size() - 1; j > i; j--) {
                            ServerHavePurchase targetServer = singleServerListAllRanges.get(j);
                            int serverRemainCoreA = targetServer.getServerNodeA().getRemainCore();
                            int serverRemainMemA = targetServer.getServerNodeA().getRemainMemory();
                            int serverRemainCoreB = targetServer.getServerNodeB().getRemainCore();
                            int serverRemainMemB = targetServer.getServerNodeB().getRemainMemory();
                            if (vmCore <= serverRemainCoreA && vmMemory <= serverRemainMemA) {
                                vmMigration(vm, serverHavePurchase, targetServer, 0);
                                List<Integer> serverAndNodeId = new ArrayList<>();
                                serverAndNodeId.add(targetServer.getId());
                                serverAndNodeId.add(0);
                                vmMigrationMapEveryDay.put(vm.getId(), serverAndNodeId);
                                if (vmMigrationMapEveryDay.size() >= sinVmMigrationMaxNum + douVmMigrationMaxNum) {
                                    return vmMigrationMapEveryDay;
                                }
                                k--;  // holdVM中的虚拟机被删掉了，要保证遍历时的id正确
                                break;
                            } else if (vmCore <= serverRemainCoreB && vmMemory <= serverRemainMemB) {
                                vmMigration(vm, serverHavePurchase, targetServer, 1);
                                List<Integer> serverAndNodeId = new ArrayList<>();
                                serverAndNodeId.add(targetServer.getId());
                                serverAndNodeId.add(1);
                                vmMigrationMapEveryDay.put(vm.getId(), serverAndNodeId);
                                if (vmMigrationMapEveryDay.size() >= sinVmMigrationMaxNum + douVmMigrationMaxNum) {
                                    return vmMigrationMapEveryDay;
                                }
                                k--;  // holdVM中的虚拟机被删掉了，要保证遍历时的id正确
                                break;
                            }
                        }
                    }

                }

            }

        }

        return vmMigrationMapEveryDay;
    }


    // 迁移方案4：跨range迁移，迁移两次，将serverList根据装载的虚拟机数由小到大排序，将数量少的服务器中的虚拟机放到数量多的服务器里面，节约服务器
    public static Map<Integer, List<Integer>> startVmMigrationPlan4(Map<String, List<List<ServerHavePurchase>>> serverHaveBuySinAndDouList) {

        Map<Integer, List<Integer>> vmMigrationMapEveryDay = new HashMap<>();
        List<List<ServerHavePurchase>> singleServerList = serverHaveBuySinAndDouList.get("single");
        List<List<ServerHavePurchase>> doubleServerList = serverHaveBuySinAndDouList.get("double");

        float douVmTotalNum = 0;
        float sinVmTotalNum = 0;
        List<ServerHavePurchase> doubleServerListAllRanges = new ArrayList<>();
        List<ServerHavePurchase> singleServerListAllRanges = new ArrayList<>();

        for (List<ServerHavePurchase> serverHavePurchaseList : doubleServerList) {
            for (ServerHavePurchase serverHavePurchase : serverHavePurchaseList) {
                doubleServerListAllRanges.add(serverHavePurchase);
                List<VMHaveRequest> holdVM = serverHavePurchase.getHoldVM();
                douVmTotalNum += holdVM.size();
            }
        }
        for (List<ServerHavePurchase> serverHavePurchaseList : singleServerList) {
            for (ServerHavePurchase serverHavePurchase : serverHavePurchaseList) {
                singleServerListAllRanges.add(serverHavePurchase);
                List<VMHaveRequest> holdVM = serverHavePurchase.getHoldVM();
                sinVmTotalNum += holdVM.size();
            }
        }
        int douVmMigrationMaxNum = (int) douVmTotalNum / 1000 * 5;
        int sinVmMigrationMaxNum = (int) sinVmTotalNum / 1000 * 5;

        if (douVmMigrationMaxNum > 1) {

            // 对双节点服务器中拥有的vm数进行排序

            for (ServerHavePurchase serverHavePurchase : doubleServerListAllRanges) {
                serverHavePurchase.setHoldVmNum(serverHavePurchase.getHoldVM().size());
            }
            Collections.sort(doubleServerListAllRanges, Comparator.comparing(ServerHavePurchase::getHoldVmNum));


            // 对双节点虚拟机进行迁移
            for (int i = 0; i < doubleServerListAllRanges.size(); i++) {
                ServerHavePurchase serverHavePurchase = doubleServerListAllRanges.get(i);
                if (serverHavePurchase.getHoldVM() == null && serverHavePurchase.getHoldVM().size() == 0) {
                    continue;
                } else {
                    List<VMHaveRequest> holdVmList = serverHavePurchase.getHoldVM();
                    for (int k = 0; k < holdVmList.size(); k++) {
                        VMHaveRequest vm = holdVmList.get(k);
                        int vmCore = vm.getCore() / 2;
                        int vmMemory = vm.getMemory() / 2;
                        for (int j = doubleServerListAllRanges.size() - 1; j > i; j--) {
                            ServerHavePurchase targetServer = doubleServerListAllRanges.get(j);
                            int serverRemainCore = targetServer.getServerNodeA().getRemainCore();
                            int serverRemainMem = targetServer.getServerNodeA().getRemainMemory();
                            if (vmCore <= serverRemainCore && vmMemory <= serverRemainMem) {
                                vmMigration(vm, serverHavePurchase, targetServer, 2);
                                List<Integer> serverAndNodeId = new ArrayList<>();
                                serverAndNodeId.add(targetServer.getId());
                                vmMigrationMapEveryDay.put(vm.getId(), serverAndNodeId);
                                if (vmMigrationMapEveryDay.size() >= douVmMigrationMaxNum) {
                                    return vmMigrationMapEveryDay;
                                }
                                k--;  // holdVM中的虚拟机被删掉了，要保证遍历时的id正确
                                break;
                            }
                        }
                    }
                }
            }
            // 对双节点虚拟机进行迁移
            for (int i = 0; i < doubleServerListAllRanges.size(); i++) {
                ServerHavePurchase serverHavePurchase = doubleServerListAllRanges.get(i);
                if (serverHavePurchase.getHoldVM() == null && serverHavePurchase.getHoldVM().size() == 0) {
                    continue;
                } else {
                    List<VMHaveRequest> holdVmList = serverHavePurchase.getHoldVM();
                    for (int k = 0; k < holdVmList.size(); k++) {
                        VMHaveRequest vm = holdVmList.get(k);
                        int vmCore = vm.getCore() / 2;
                        int vmMemory = vm.getMemory() / 2;
                        for (int j = doubleServerListAllRanges.size() - 1; j > i; j--) {
                            ServerHavePurchase targetServer = doubleServerListAllRanges.get(j);
                            int serverRemainCore = targetServer.getServerNodeA().getRemainCore();
                            int serverRemainMem = targetServer.getServerNodeA().getRemainMemory();
                            if (vmCore <= serverRemainCore && vmMemory <= serverRemainMem) {
                                vmMigration(vm, serverHavePurchase, targetServer, 2);
                                List<Integer> serverAndNodeId = new ArrayList<>();
                                serverAndNodeId.add(targetServer.getId());
                                vmMigrationMapEveryDay.put(vm.getId(), serverAndNodeId);
                                if (vmMigrationMapEveryDay.size() >= douVmMigrationMaxNum) {
                                    return vmMigrationMapEveryDay;
                                }
                                k--;  // holdVM中的虚拟机被删掉了，要保证遍历时的id正确
                                break;
                            }
                        }
                    }
                }
            }


        }


        if (sinVmMigrationMaxNum > 1) {
            // 对单节点服务器中拥有的vm数进行排序

            for (ServerHavePurchase serverHavePurchase : singleServerListAllRanges) {
                serverHavePurchase.setHoldVmNum(serverHavePurchase.getHoldVM().size());
            }
            Collections.sort(singleServerListAllRanges, Comparator.comparing(ServerHavePurchase::getHoldVmNum));


            // 对单节点虚拟机进行迁移
            for (int i = 0; i < singleServerListAllRanges.size(); i++) {
                ServerHavePurchase serverHavePurchase = singleServerListAllRanges.get(i);
                if (serverHavePurchase.getHoldVM() == null && serverHavePurchase.getHoldVM().size() == 0) {
                    continue;
                } else {
                    List<VMHaveRequest> holdVmList = serverHavePurchase.getHoldVM();
                    for (int k = 0; k < holdVmList.size(); k++) {
                        VMHaveRequest vm = holdVmList.get(k);
                        int vmCore = vm.getCore();
                        int vmMemory = vm.getMemory();
                        for (int j = singleServerListAllRanges.size() - 1; j > i; j--) {
                            ServerHavePurchase targetServer = singleServerListAllRanges.get(j);
                            int serverRemainCoreA = targetServer.getServerNodeA().getRemainCore();
                            int serverRemainMemA = targetServer.getServerNodeA().getRemainMemory();
                            int serverRemainCoreB = targetServer.getServerNodeB().getRemainCore();
                            int serverRemainMemB = targetServer.getServerNodeB().getRemainMemory();
                            if (vmCore <= serverRemainCoreA && vmMemory <= serverRemainMemA) {
                                vmMigration(vm, serverHavePurchase, targetServer, 0);
                                List<Integer> serverAndNodeId = new ArrayList<>();
                                serverAndNodeId.add(targetServer.getId());
                                serverAndNodeId.add(0);
                                vmMigrationMapEveryDay.put(vm.getId(), serverAndNodeId);
                                if (vmMigrationMapEveryDay.size() >= sinVmMigrationMaxNum + douVmMigrationMaxNum) {
                                    return vmMigrationMapEveryDay;
                                }
                                k--;  // holdVM中的虚拟机被删掉了，要保证遍历时的id正确
                                break;
                            } else if (vmCore <= serverRemainCoreB && vmMemory <= serverRemainMemB) {
                                vmMigration(vm, serverHavePurchase, targetServer, 1);
                                List<Integer> serverAndNodeId = new ArrayList<>();
                                serverAndNodeId.add(targetServer.getId());
                                serverAndNodeId.add(1);
                                vmMigrationMapEveryDay.put(vm.getId(), serverAndNodeId);
                                if (vmMigrationMapEveryDay.size() >= sinVmMigrationMaxNum + douVmMigrationMaxNum) {
                                    return vmMigrationMapEveryDay;
                                }
                                k--;  // holdVM中的虚拟机被删掉了，要保证遍历时的id正确
                                break;
                            }
                        }
                    }

                }

            }
            for (int i = 0; i < singleServerListAllRanges.size(); i++) {
                ServerHavePurchase serverHavePurchase = singleServerListAllRanges.get(i);
                if (serverHavePurchase.getHoldVM() == null && serverHavePurchase.getHoldVM().size() == 0) {
                    continue;
                } else {
                    List<VMHaveRequest> holdVmList = serverHavePurchase.getHoldVM();
                    for (int k = 0; k < holdVmList.size(); k++) {
                        VMHaveRequest vm = holdVmList.get(k);
                        int vmCore = vm.getCore();
                        int vmMemory = vm.getMemory();
                        for (int j = singleServerListAllRanges.size() - 1; j > i; j--) {
                            ServerHavePurchase targetServer = singleServerListAllRanges.get(j);
                            int serverRemainCoreA = targetServer.getServerNodeA().getRemainCore();
                            int serverRemainMemA = targetServer.getServerNodeA().getRemainMemory();
                            int serverRemainCoreB = targetServer.getServerNodeB().getRemainCore();
                            int serverRemainMemB = targetServer.getServerNodeB().getRemainMemory();
                            if (vmCore <= serverRemainCoreA && vmMemory <= serverRemainMemA) {
                                vmMigration(vm, serverHavePurchase, targetServer, 0);
                                List<Integer> serverAndNodeId = new ArrayList<>();
                                serverAndNodeId.add(targetServer.getId());
                                serverAndNodeId.add(0);
                                vmMigrationMapEveryDay.put(vm.getId(), serverAndNodeId);
                                if (vmMigrationMapEveryDay.size() >= sinVmMigrationMaxNum + douVmMigrationMaxNum) {
                                    return vmMigrationMapEveryDay;
                                }
                                k--;  // holdVM中的虚拟机被删掉了，要保证遍历时的id正确
                                break;
                            } else if (vmCore <= serverRemainCoreB && vmMemory <= serverRemainMemB) {
                                vmMigration(vm, serverHavePurchase, targetServer, 1);
                                List<Integer> serverAndNodeId = new ArrayList<>();
                                serverAndNodeId.add(targetServer.getId());
                                serverAndNodeId.add(1);
                                vmMigrationMapEveryDay.put(vm.getId(), serverAndNodeId);
                                if (vmMigrationMapEveryDay.size() >= sinVmMigrationMaxNum + douVmMigrationMaxNum) {
                                    return vmMigrationMapEveryDay;
                                }
                                k--;  // holdVM中的虚拟机被删掉了，要保证遍历时的id正确
                                break;
                            }
                        }
                    }

                }

            }

        }

        return vmMigrationMapEveryDay;
    }

    // 迁移方案5：跨range迁移，将serverList根据m+c排序
    public static Map<Integer, List<Integer>> startVmMigrationPlan5(Map<String, List<List<ServerHavePurchase>>> serverHaveBuySinAndDouList) {

        Map<Integer, List<Integer>> vmMigrationMapEveryDay = new HashMap<>();
        List<List<ServerHavePurchase>> singleServerList = serverHaveBuySinAndDouList.get("single");
        List<List<ServerHavePurchase>> doubleServerList = serverHaveBuySinAndDouList.get("double");

        float douVmTotalNum = 0;
        float sinVmTotalNum = 0;
        List<ServerHavePurchase> doubleServerListAllRanges = new ArrayList<>();
        List<ServerHavePurchase> singleServerListAllRanges = new ArrayList<>();

        for (List<ServerHavePurchase> serverHavePurchaseList : doubleServerList) {
            for (ServerHavePurchase serverHavePurchase : serverHavePurchaseList) {
                doubleServerListAllRanges.add(serverHavePurchase);
                List<VMHaveRequest> holdVM = serverHavePurchase.getHoldVM();
                douVmTotalNum += holdVM.size();
            }
        }
        for (List<ServerHavePurchase> serverHavePurchaseList : singleServerList) {
            for (ServerHavePurchase serverHavePurchase : serverHavePurchaseList) {
                singleServerListAllRanges.add(serverHavePurchase);
                List<VMHaveRequest> holdVM = serverHavePurchase.getHoldVM();
                sinVmTotalNum += holdVM.size();
            }
        }
        int douVmMigrationMaxNum = (int) douVmTotalNum / 1000 * 5;
        int sinVmMigrationMaxNum = (int) sinVmTotalNum / 1000 * 5;

        if (douVmMigrationMaxNum > 1) {

            // 对双节点服务器中拥有的vm数进行排序

            for (ServerHavePurchase serverHavePurchase : doubleServerListAllRanges) {
                serverHavePurchase.setMcRatioSum();
            }
            Collections.sort(doubleServerListAllRanges, Comparator.comparing(ServerHavePurchase::getMcRatioSum));

            // 对双节点虚拟机进行迁移
            for (int i = 0; i < doubleServerListAllRanges.size(); i++) {
                ServerHavePurchase serverHavePurchase = doubleServerListAllRanges.get(i);
                if (serverHavePurchase.getHoldVM() == null && serverHavePurchase.getHoldVM().size() == 0) {
                    continue;
                } else {
                    List<VMHaveRequest> holdVmList = serverHavePurchase.getHoldVM();
                    for (int k = 0; k < holdVmList.size(); k++) {
                        VMHaveRequest vm = holdVmList.get(k);
                        int vmCore = vm.getCore() / 2;
                        int vmMemory = vm.getMemory() / 2;
                        for (int j = doubleServerListAllRanges.size() - 1; j > i; j--) {
                            ServerHavePurchase targetServer = doubleServerListAllRanges.get(j);
                            int serverRemainCore = targetServer.getServerNodeA().getRemainCore();
                            int serverRemainMem = targetServer.getServerNodeA().getRemainMemory();
                            if (vmCore <= serverRemainCore && vmMemory <= serverRemainMem) {
                                vmMigration(vm, serverHavePurchase, targetServer, 2);
                                List<Integer> serverAndNodeId = new ArrayList<>();
                                serverAndNodeId.add(targetServer.getId());
                                vmMigrationMapEveryDay.put(vm.getId(), serverAndNodeId);
                                if (vmMigrationMapEveryDay.size() >= douVmMigrationMaxNum) {
                                    return vmMigrationMapEveryDay;
                                }
                                k--;  // holdVM中的虚拟机被删掉了，要保证遍历时的id正确
                                break;
                            }
                        }
                    }
                }
            }
        }

        if (sinVmMigrationMaxNum > 1) {
            // 对单节点服务器中拥有的vm数进行排序

            for (ServerHavePurchase serverHavePurchase : singleServerListAllRanges) {
                serverHavePurchase.setMcRatioSum();
            }
            Collections.sort(doubleServerListAllRanges, Comparator.comparing(ServerHavePurchase::getMcRatioSum));

            // 对单节点虚拟机进行迁移
            for (int i = 0; i < singleServerListAllRanges.size(); i++) {
                ServerHavePurchase serverHavePurchase = singleServerListAllRanges.get(i);
                if (serverHavePurchase.getHoldVM() == null && serverHavePurchase.getHoldVM().size() == 0) {
                    continue;
                } else {
                    List<VMHaveRequest> holdVmList = serverHavePurchase.getHoldVM();
                    for (int k = 0; k < holdVmList.size(); k++) {
                        VMHaveRequest vm = holdVmList.get(k);
                        int vmCore = vm.getCore();
                        int vmMemory = vm.getMemory();
                        for (int j = singleServerListAllRanges.size() - 1; j > i; j--) {
                            ServerHavePurchase targetServer = singleServerListAllRanges.get(j);
                            int serverRemainCoreA = targetServer.getServerNodeA().getRemainCore();
                            int serverRemainMemA = targetServer.getServerNodeA().getRemainMemory();
                            int serverRemainCoreB = targetServer.getServerNodeB().getRemainCore();
                            int serverRemainMemB = targetServer.getServerNodeB().getRemainMemory();
                            if (vmCore <= serverRemainCoreA && vmMemory <= serverRemainMemA) {
                                vmMigration(vm, serverHavePurchase, targetServer, 0);
                                List<Integer> serverAndNodeId = new ArrayList<>();
                                serverAndNodeId.add(targetServer.getId());
                                serverAndNodeId.add(0);
                                vmMigrationMapEveryDay.put(vm.getId(), serverAndNodeId);
                                if (vmMigrationMapEveryDay.size() >= sinVmMigrationMaxNum + douVmMigrationMaxNum) {
                                    return vmMigrationMapEveryDay;
                                }
                                k--;  // holdVM中的虚拟机被删掉了，要保证遍历时的id正确
                                break;
                            } else if (vmCore <= serverRemainCoreB && vmMemory <= serverRemainMemB) {
                                vmMigration(vm, serverHavePurchase, targetServer, 1);
                                List<Integer> serverAndNodeId = new ArrayList<>();
                                serverAndNodeId.add(targetServer.getId());
                                serverAndNodeId.add(1);
                                vmMigrationMapEveryDay.put(vm.getId(), serverAndNodeId);
                                if (vmMigrationMapEveryDay.size() >= sinVmMigrationMaxNum + douVmMigrationMaxNum) {
                                    return vmMigrationMapEveryDay;
                                }
                                k--;  // holdVM中的虚拟机被删掉了，要保证遍历时的id正确
                                break;
                            }
                        }
                    }

                }

            }

        }

        return vmMigrationMapEveryDay;
    }


    // 迁移方案6：跨range，将serverList根据能耗由大到小排序，尽量让能耗大的服务器空出来
    public static Map<Integer, List<Integer>> startVmMigrationPlan6(Map<String, List<List<ServerHavePurchase>>> serverHaveBuySinAndDouList) {

        Map<Integer, List<Integer>> vmMigrationMapEveryDay = new HashMap<>();
        List<List<ServerHavePurchase>> singleServerList = serverHaveBuySinAndDouList.get("single");
        List<List<ServerHavePurchase>> doubleServerList = serverHaveBuySinAndDouList.get("double");

        float douVmTotalNum = 0;
        float sinVmTotalNum = 0;
        List<ServerHavePurchase> doubleServerListAllRanges = new ArrayList<>();
        List<ServerHavePurchase> singleServerListAllRanges = new ArrayList<>();

        for (List<ServerHavePurchase> serverHavePurchaseList : doubleServerList) {
            for (ServerHavePurchase serverHavePurchase : serverHavePurchaseList) {
                doubleServerListAllRanges.add(serverHavePurchase);
                List<VMHaveRequest> holdVM = serverHavePurchase.getHoldVM();
                douVmTotalNum += holdVM.size();
            }
        }
        for (List<ServerHavePurchase> serverHavePurchaseList : singleServerList) {
            for (ServerHavePurchase serverHavePurchase : serverHavePurchaseList) {
                singleServerListAllRanges.add(serverHavePurchase);
                List<VMHaveRequest> holdVM = serverHavePurchase.getHoldVM();
                sinVmTotalNum += holdVM.size();
            }
        }
        int douVmMigrationMaxNum = (int) douVmTotalNum / 1000 * 5;
        int sinVmMigrationMaxNum = (int) sinVmTotalNum / 1000 * 5;


        if (douVmMigrationMaxNum > 1) {

            // 对双节点服务器中服务器能耗排序

            Collections.sort(doubleServerListAllRanges, Comparator.comparing(ServerHavePurchase::getEnergyConsumptionPerDay).reversed());


            // 对双节点虚拟机进行迁移

            for (int i = 0; i < doubleServerListAllRanges.size(); i++) {
                ServerHavePurchase serverHavePurchase = doubleServerListAllRanges.get(i);
                if (serverHavePurchase.getHoldVM() == null && serverHavePurchase.getHoldVM().size() == 0) {
                    continue;
                } else {
                    List<VMHaveRequest> holdVmList = serverHavePurchase.getHoldVM();
                    for (int k = 0; k < holdVmList.size(); k++) {
                        VMHaveRequest vm = holdVmList.get(k);
                        int vmCore = vm.getCore() / 2;
                        int vmMemory = vm.getMemory() / 2;
                        for (int j = doubleServerListAllRanges.size() - 1; j > i; j--) {
                            ServerHavePurchase targetServer = doubleServerListAllRanges.get(j);
                            int serverRemainCore = targetServer.getServerNodeA().getRemainCore();
                            int serverRemainMem = targetServer.getServerNodeA().getRemainMemory();
                            if (vmCore <= serverRemainCore && vmMemory <= serverRemainMem) {
                                vmMigration(vm, serverHavePurchase, targetServer, 2);
                                List<Integer> serverAndNodeId = new ArrayList<>();
                                serverAndNodeId.add(targetServer.getId());
                                vmMigrationMapEveryDay.put(vm.getId(), serverAndNodeId);
                                if (vmMigrationMapEveryDay.size() >= douVmMigrationMaxNum) {
                                    return vmMigrationMapEveryDay;
                                }
                                k--;  // holdVM中的虚拟机被删掉了，要保证遍历时的id正确
                                break;
                            }
                        }
                    }
                }
            }
            for (int i = 0; i < doubleServerListAllRanges.size(); i++) {
                ServerHavePurchase serverHavePurchase = doubleServerListAllRanges.get(i);
                if (serverHavePurchase.getHoldVM() == null && serverHavePurchase.getHoldVM().size() == 0) {
                    continue;
                } else {
                    List<VMHaveRequest> holdVmList = serverHavePurchase.getHoldVM();
                    for (int k = 0; k < holdVmList.size(); k++) {
                        VMHaveRequest vm = holdVmList.get(k);
                        int vmCore = vm.getCore() / 2;
                        int vmMemory = vm.getMemory() / 2;
                        for (int j = doubleServerListAllRanges.size() - 1; j > i; j--) {
                            ServerHavePurchase targetServer = doubleServerListAllRanges.get(j);
                            int serverRemainCore = targetServer.getServerNodeA().getRemainCore();
                            int serverRemainMem = targetServer.getServerNodeA().getRemainMemory();
                            if (vmCore <= serverRemainCore && vmMemory <= serverRemainMem) {
                                vmMigration(vm, serverHavePurchase, targetServer, 2);
                                List<Integer> serverAndNodeId = new ArrayList<>();
                                serverAndNodeId.add(targetServer.getId());
                                vmMigrationMapEveryDay.put(vm.getId(), serverAndNodeId);
                                if (vmMigrationMapEveryDay.size() >= douVmMigrationMaxNum) {
                                    return vmMigrationMapEveryDay;
                                }
                                k--;  // holdVM中的虚拟机被删掉了，要保证遍历时的id正确
                                break;
                            }
                        }
                    }
                }
            }

        }

        if (sinVmMigrationMaxNum > 1) {
            // 对单节点服务器中服务器能耗排序

            Collections.sort(singleServerListAllRanges, Comparator.comparing(ServerHavePurchase::getEnergyConsumptionPerDay).reversed());


            // 对单节点虚拟机进行迁移

            for (int i = 0; i < singleServerListAllRanges.size(); i++) {
                ServerHavePurchase serverHavePurchase = singleServerListAllRanges.get(i);
                if (serverHavePurchase.getHoldVM() == null && serverHavePurchase.getHoldVM().size() == 0) {
                    continue;
                } else {
                    List<VMHaveRequest> holdVmList = serverHavePurchase.getHoldVM();
                    for (int k = 0; k < holdVmList.size(); k++) {
                        VMHaveRequest vm = holdVmList.get(k);
                        int vmCore = vm.getCore();
                        int vmMemory = vm.getMemory();
                        for (int j = singleServerListAllRanges.size() - 1; j > i; j--) {
                            ServerHavePurchase targetServer = singleServerListAllRanges.get(j);
                            int serverRemainCoreA = targetServer.getServerNodeA().getRemainCore();
                            int serverRemainMemA = targetServer.getServerNodeA().getRemainMemory();
                            int serverRemainCoreB = targetServer.getServerNodeB().getRemainCore();
                            int serverRemainMemB = targetServer.getServerNodeB().getRemainMemory();
                            if (vmCore <= serverRemainCoreA && vmMemory <= serverRemainMemA) {
                                vmMigration(vm, serverHavePurchase, targetServer, 0);
                                List<Integer> serverAndNodeId = new ArrayList<>();
                                serverAndNodeId.add(targetServer.getId());
                                serverAndNodeId.add(0);
                                vmMigrationMapEveryDay.put(vm.getId(), serverAndNodeId);
                                if (vmMigrationMapEveryDay.size() >= sinVmMigrationMaxNum + douVmMigrationMaxNum) {
                                    return vmMigrationMapEveryDay;
                                }
                                k--;  // holdVM中的虚拟机被删掉了，要保证遍历时的id正确
                                break;
                            } else if (vmCore <= serverRemainCoreB && vmMemory <= serverRemainMemB) {
                                vmMigration(vm, serverHavePurchase, targetServer, 1);
                                List<Integer> serverAndNodeId = new ArrayList<>();
                                serverAndNodeId.add(targetServer.getId());
                                serverAndNodeId.add(1);
                                vmMigrationMapEveryDay.put(vm.getId(), serverAndNodeId);
                                if (vmMigrationMapEveryDay.size() >= sinVmMigrationMaxNum + douVmMigrationMaxNum) {
                                    return vmMigrationMapEveryDay;
                                }
                                k--;  // holdVM中的虚拟机被删掉了，要保证遍历时的id正确
                                break;
                            }
                        }
                    }

                }
            }
            for (int i = 0; i < singleServerListAllRanges.size(); i++) {
                ServerHavePurchase serverHavePurchase = singleServerListAllRanges.get(i);
                if (serverHavePurchase.getHoldVM() == null && serverHavePurchase.getHoldVM().size() == 0) {
                    continue;
                } else {
                    List<VMHaveRequest> holdVmList = serverHavePurchase.getHoldVM();
                    for (int k = 0; k < holdVmList.size(); k++) {
                        VMHaveRequest vm = holdVmList.get(k);
                        int vmCore = vm.getCore();
                        int vmMemory = vm.getMemory();
                        for (int j = singleServerListAllRanges.size() - 1; j > i; j--) {
                            ServerHavePurchase targetServer = singleServerListAllRanges.get(j);
                            int serverRemainCoreA = targetServer.getServerNodeA().getRemainCore();
                            int serverRemainMemA = targetServer.getServerNodeA().getRemainMemory();
                            int serverRemainCoreB = targetServer.getServerNodeB().getRemainCore();
                            int serverRemainMemB = targetServer.getServerNodeB().getRemainMemory();
                            if (vmCore <= serverRemainCoreA && vmMemory <= serverRemainMemA) {
                                vmMigration(vm, serverHavePurchase, targetServer, 0);
                                List<Integer> serverAndNodeId = new ArrayList<>();
                                serverAndNodeId.add(targetServer.getId());
                                serverAndNodeId.add(0);
                                vmMigrationMapEveryDay.put(vm.getId(), serverAndNodeId);
                                if (vmMigrationMapEveryDay.size() >= sinVmMigrationMaxNum + douVmMigrationMaxNum) {
                                    return vmMigrationMapEveryDay;
                                }
                                k--;  // holdVM中的虚拟机被删掉了，要保证遍历时的id正确
                                break;
                            } else if (vmCore <= serverRemainCoreB && vmMemory <= serverRemainMemB) {
                                vmMigration(vm, serverHavePurchase, targetServer, 1);
                                List<Integer> serverAndNodeId = new ArrayList<>();
                                serverAndNodeId.add(targetServer.getId());
                                serverAndNodeId.add(1);
                                vmMigrationMapEveryDay.put(vm.getId(), serverAndNodeId);
                                if (vmMigrationMapEveryDay.size() >= sinVmMigrationMaxNum + douVmMigrationMaxNum) {
                                    return vmMigrationMapEveryDay;
                                }
                                k--;  // holdVM中的虚拟机被删掉了，要保证遍历时的id正确
                                break;
                            }
                        }
                    }

                }
            }


        }

        return vmMigrationMapEveryDay;
    }


    // 迁移方案2：在同一range内，将serverList根据能耗由大到小排序，尽量让能耗大的服务器空出来
    public static Map<Integer, List<Integer>> startVmMigrationPlan2(Map<String, List<List<ServerHavePurchase>>> serverHaveBuySinAndDouList) {

        Map<Integer, List<Integer>> vmMigrationMapEveryDay = new HashMap<>();
        List<List<ServerHavePurchase>> singleServerList = serverHaveBuySinAndDouList.get("single");
        List<List<ServerHavePurchase>> doubleServerList = serverHaveBuySinAndDouList.get("double");

        float douVmTotalNum = 0;
        float sinVmTotalNum = 0;

        for (List<ServerHavePurchase> serverHavePurchaseList : doubleServerList) {
            for (ServerHavePurchase serverHavePurchase : serverHavePurchaseList) {
                List<VMHaveRequest> holdVM = serverHavePurchase.getHoldVM();
                douVmTotalNum += holdVM.size();
            }
        }
        for (List<ServerHavePurchase> serverHavePurchaseList : singleServerList) {
            for (ServerHavePurchase serverHavePurchase : serverHavePurchaseList) {
                List<VMHaveRequest> holdVM = serverHavePurchase.getHoldVM();
                sinVmTotalNum += holdVM.size();
            }
        }
        int douVmMigrationMaxNum = (int) douVmTotalNum / 1000 * 5;
        int sinVmMigrationMaxNum = (int) sinVmTotalNum / 1000 * 5;


        if (douVmMigrationMaxNum > 1) {

            // 对双节点服务器中服务器能耗排序
            for (List<ServerHavePurchase> serverEveryRange : doubleServerList) {
                Collections.sort(serverEveryRange, Comparator.comparing(ServerHavePurchase::getEnergyConsumptionPerDay).reversed());
            }

            // 对双节点虚拟机进行迁移
            for (List<ServerHavePurchase> serverEveryRange : doubleServerList) {
                for (int i = 0; i < serverEveryRange.size(); i++) {
                    ServerHavePurchase serverHavePurchase = serverEveryRange.get(i);
                    if (serverHavePurchase.getHoldVM() == null && serverHavePurchase.getHoldVM().size() == 0) {
                        continue;
                    } else {
                        List<VMHaveRequest> holdVmList = serverHavePurchase.getHoldVM();
                        for (int k = 0; k < holdVmList.size(); k++) {
                            VMHaveRequest vm = holdVmList.get(k);
                            int vmCore = vm.getCore() / 2;
                            int vmMemory = vm.getMemory() / 2;
                            for (int j = serverEveryRange.size() - 1; j > i; j--) {
                                ServerHavePurchase targetServer = serverEveryRange.get(j);
                                int serverRemainCore = targetServer.getServerNodeA().getRemainCore();
                                int serverRemainMem = targetServer.getServerNodeA().getRemainMemory();
                                if (vmCore <= serverRemainCore && vmMemory <= serverRemainMem) {
                                    vmMigration(vm, serverHavePurchase, targetServer, 2);
                                    List<Integer> serverAndNodeId = new ArrayList<>();
                                    serverAndNodeId.add(targetServer.getId());
                                    vmMigrationMapEveryDay.put(vm.getId(), serverAndNodeId);
                                    if (vmMigrationMapEveryDay.size() >= douVmMigrationMaxNum) {
                                        return vmMigrationMapEveryDay;
                                    }
                                    k--;  // holdVM中的虚拟机被删掉了，要保证遍历时的id正确
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }

        if (sinVmMigrationMaxNum > 1) {
            // 对单节点服务器中服务器能耗排序
            for (List<ServerHavePurchase> serverEveryRange : singleServerList) {
                Collections.sort(serverEveryRange, Comparator.comparing(ServerHavePurchase::getEnergyConsumptionPerDay).reversed());
            }

            // 对单节点虚拟机进行迁移
            for (List<ServerHavePurchase> serverEveryRange : singleServerList) {
                for (int i = 0; i < serverEveryRange.size(); i++) {
                    ServerHavePurchase serverHavePurchase = serverEveryRange.get(i);
                    if (serverHavePurchase.getHoldVM() == null && serverHavePurchase.getHoldVM().size() == 0) {
                        continue;
                    } else {
                        List<VMHaveRequest> holdVmList = serverHavePurchase.getHoldVM();
                        for (int k = 0; k < holdVmList.size(); k++) {
                            VMHaveRequest vm = holdVmList.get(k);
                            int vmCore = vm.getCore();
                            int vmMemory = vm.getMemory();
                            for (int j = serverEveryRange.size() - 1; j > i; j--) {
                                ServerHavePurchase targetServer = serverEveryRange.get(j);
                                int serverRemainCoreA = targetServer.getServerNodeA().getRemainCore();
                                int serverRemainMemA = targetServer.getServerNodeA().getRemainMemory();
                                int serverRemainCoreB = targetServer.getServerNodeB().getRemainCore();
                                int serverRemainMemB = targetServer.getServerNodeB().getRemainMemory();
                                if (vmCore <= serverRemainCoreA && vmMemory <= serverRemainMemA) {
                                    vmMigration(vm, serverHavePurchase, targetServer, 0);
                                    List<Integer> serverAndNodeId = new ArrayList<>();
                                    serverAndNodeId.add(targetServer.getId());
                                    serverAndNodeId.add(0);
                                    vmMigrationMapEveryDay.put(vm.getId(), serverAndNodeId);
                                    if (vmMigrationMapEveryDay.size() >= sinVmMigrationMaxNum + douVmMigrationMaxNum) {
                                        return vmMigrationMapEveryDay;
                                    }
                                    k--;  // holdVM中的虚拟机被删掉了，要保证遍历时的id正确
                                    break;
                                } else if (vmCore <= serverRemainCoreB && vmMemory <= serverRemainMemB) {
                                    vmMigration(vm, serverHavePurchase, targetServer, 1);
                                    List<Integer> serverAndNodeId = new ArrayList<>();
                                    serverAndNodeId.add(targetServer.getId());
                                    serverAndNodeId.add(1);
                                    vmMigrationMapEveryDay.put(vm.getId(), serverAndNodeId);
                                    if (vmMigrationMapEveryDay.size() >= sinVmMigrationMaxNum + douVmMigrationMaxNum) {
                                        return vmMigrationMapEveryDay;
                                    }
                                    k--;  // holdVM中的虚拟机被删掉了，要保证遍历时的id正确
                                    break;
                                }
                            }
                        }

                    }

                }
            }

        }

        return vmMigrationMapEveryDay;
    }


    private static void vmMigration(VMHaveRequest vm, ServerHavePurchase nServer, ServerHavePurchase tServer, int serverNodeId) {
        nServer.delVM(nServer, vm);
        tServer.addVM(vm, serverNodeId);
    }

    private static void migrationDouVmInRange0(Map<Integer, List<VMHaveRequest>> vmCoreMap, Map<Integer,
                                               List<VMHaveRequest>> vmMemoryMap,
                                               List<List<ServerHavePurchase>> serverListAllRanges, int maxGateValue,
                                               int minGateValue,
                                               Map<Integer, ServerHavePurchase> serverIdMap,
                                               List<Map<Integer, List<Integer>>> vmMigrationMapEveryDay, int migrationMaxNum) {
        int migrationNum=0;
        List<ServerHavePurchase> serverList0 = serverListAllRanges.get(0);
        for(ServerHavePurchase serverHavePurchase:serverList0){

            int remainCore = serverHavePurchase.getServerNodeA().getRemainCore();
            int remainMemory = serverHavePurchase.getServerNodeA().getRemainMemory();
            if(remainCore<=minGateValue && remainMemory>=maxGateValue) {
                List<VMHaveRequest> holdVM = serverHavePurchase.getHoldVM();
                for (int i = 0; i < holdVM.size(); i++) {
                    VMHaveRequest vm = holdVM.get(i);
                    int vmCore = vm.getCore();
                    int vmMemory = vm.getMemory();
                    List<VMHaveRequest> vmListByCore = vmCoreMap.get(vmCore);
                    if (vmListByCore.size() <= 1) {
                        continue;
                    }
                    VMHaveRequest targetVm = selectDouTargetVmByCore(vmListByCore, vmMemory, remainMemory);

                    if (targetVm == null) {
                        continue;
                    }
                    if (migrationNum + 3 > migrationMaxNum) {
                        return;
                    }
                    int targetServerId = targetVm.getServerId();
                    ServerHavePurchase targetServer = serverIdMap.get(targetServerId);
                    int targetServerRemainMemory = targetServer.getServerNodeA().getRemainMemory();
                    int targetServerRemainCore = targetServer.getServerNodeB().getRemainCore();
                    if(targetServerRemainCore-targetServerRemainMemory<Main.gateValue){
                        continue;
                    }
                    boolean isSuccessMiration = migrationBetweenTwoDouVm(vm, serverHavePurchase,targetVm, targetServer,serverIdMap, serverListAllRanges, vmMigrationMapEveryDay);

                    if (isSuccessMiration) {
                        migrationNum += 3;
                        break;
                    }

                }
            }else if(remainCore>=maxGateValue && remainMemory<=minGateValue){
                List<VMHaveRequest> holdVM = serverHavePurchase.getHoldVM();
                for (int i = 0; i < holdVM.size(); i++) {
                    VMHaveRequest vm = holdVM.get(i);
                    int vmCore = vm.getCore();
                    int vmMemory = vm.getMemory();
                    List<VMHaveRequest> vmListByMemory = vmMemoryMap.get(vmMemory);
                    if (vmListByMemory.size() == 1) {
                        continue;
                    }
                    VMHaveRequest targetVm = selectDouTargetVmByMemory(vmListByMemory, vmCore, remainCore);
                    if (targetVm == null) {
                        continue;
                    }
                    if (migrationNum + 3 > migrationMaxNum) {
                        return;
                    }
                    int targetServerId = targetVm.getServerId();
                    ServerHavePurchase targetServer = serverIdMap.get(targetServerId);
                    int targetServerRemainMemory = targetServer.getServerNodeA().getRemainMemory();
                    int targetServerRemainCore = targetServer.getServerNodeB().getRemainCore();

                    if(targetServerRemainMemory-targetServerRemainCore<Main.gateValue){
                        continue;
                    }

                    if(targetVm.getServerId()>serverIdMap.size()){
                        int a=1;
                    }



                    boolean isSuccessMiration = migrationBetweenTwoDouVm(vm,serverHavePurchase, targetVm,targetServer, serverIdMap, serverListAllRanges, vmMigrationMapEveryDay);
                    if(isSuccessMiration){
                        migrationNum += 3;
                        break;
                    }
                }
            }
        }
    }

    private static void migrationSinVmInRange0(Map<Integer, List<VMHaveRequest>> vmCoreMap, Map<Integer,
                                               List<VMHaveRequest>> vmMemoryMap,
                                               List<List<ServerHavePurchase>> serverListAllRanges,
                                               int minGateValue, int maxGateValue,
                                               Map<Integer, ServerHavePurchase> serverIdMap,
                                               List<Map<Integer, List<Integer>>> vmMigrationMapEveryDay2, int migrationMaxNum) {
        int migrationNum=0;
        List<ServerHavePurchase> serverList0 = serverListAllRanges.get(0);
        for(ServerHavePurchase serverHavePurchase:serverList0){

            int remainCoreA = serverHavePurchase.getServerNodeA().getRemainCore();
            int remainMemoryA = serverHavePurchase.getServerNodeA().getRemainMemory();
            if(remainCoreA<=minGateValue && remainMemoryA>=maxGateValue) {
                List<VMHaveRequest> holdVM = serverHavePurchase.getHoldVM();
                for (int i = 0; i < holdVM.size(); i++) {
                    if (migrationNum + 3 > migrationMaxNum) {
                        return;
                    }
                    VMHaveRequest vm = holdVM.get(i);
                    if(vm.getServerNodeId()==1){
                        continue;
                    }
                    int vmCore = vm.getCore();
                    int vmMemory = vm.getMemory();
                    List<VMHaveRequest> vmListByCore = vmCoreMap.get(vmCore);
                    if (vmListByCore.size() <= 1) {
                        continue;
                    }
                    VMHaveRequest targetVm = selectSinTargetVmByCore(vmListByCore, vmMemory, remainMemoryA);
                    if (targetVm == null) {
                        continue;
                    }

                    int targetVmMemory = targetVm.getMemory();
                    int targetVmCore=targetVm.getCore();
                    if(targetVmMemory-vmMemory>remainMemoryA ||targetVmCore!=vmCore ){
                        continue;
                    }

                    int targetServerId = targetVm.getServerId();
                    ServerHavePurchase targetServer = serverIdMap.get(targetServerId);
                    int targetServerNodeId = targetVm.getServerNodeId();
                    int targetServerRemainCore;
                    int targetServerRemainMemory;
                    if(targetServerNodeId==0) {
                        targetServerRemainMemory = targetServer.getServerNodeA().getRemainMemory();
                        targetServerRemainCore = targetServer.getServerNodeA().getRemainCore();
                    }else{
                        targetServerRemainMemory = targetServer.getServerNodeB().getRemainMemory();
                        targetServerRemainCore = targetServer.getServerNodeB().getRemainCore();
                    }
                    if(targetServerRemainMemory-targetServerRemainCore<Main.gateValue){
                        continue;
                    }

                    boolean isSuccessMiration = migrationBetweenTwoSinVm(vm,serverHavePurchase, targetVm,targetServer, serverIdMap, serverListAllRanges, vmMigrationMapEveryDay2);

                    if (isSuccessMiration) {
                        migrationNum += 3;
                        break;
                    }

                }
            }
            else if(remainCoreA>=maxGateValue && remainMemoryA<=minGateValue){
                if (migrationNum + 3 > migrationMaxNum) {
                    return;
                }
                List<VMHaveRequest> holdVM = serverHavePurchase.getHoldVM();
                for (int i = 0; i < holdVM.size(); i++) {
                    VMHaveRequest vm = holdVM.get(i);
                    if(vm.getServerNodeId()==1){
                        continue;
                    }

                    int vmCore = vm.getCore();
                    int vmMemory = vm.getMemory();
                    List<VMHaveRequest> vmListByMemory = vmMemoryMap.get(vmMemory);
                    if (vmListByMemory.size() == 1) {
                        continue;
                    }
                    VMHaveRequest targetVm = selectSinTargetVmByMemory(vmListByMemory, vmCore, remainCoreA);
                    if (targetVm == null) {
                        continue;
                    }
                    int targetServerId = targetVm.getServerId();

                    ServerHavePurchase targetServer = serverIdMap.get(targetServerId);
                    int targetServerNodeId = targetVm.getServerNodeId();
                    int targetServerRemainCore;
                    int targetServerRemainMemory;
                    if(targetServerNodeId==0) {
                        targetServerRemainMemory = targetServer.getServerNodeA().getRemainMemory();
                        targetServerRemainCore = targetServer.getServerNodeA().getRemainCore();
                    }else{
                        targetServerRemainMemory = targetServer.getServerNodeB().getRemainMemory();
                        targetServerRemainCore = targetServer.getServerNodeB().getRemainCore();
                    }
                    if(targetServerRemainMemory-targetServerRemainCore<Main.gateValue){
                        continue;
                    }


                    boolean isSuccessMiration = migrationBetweenTwoSinVm(vm,serverHavePurchase, targetVm,targetServer, serverIdMap, serverListAllRanges, vmMigrationMapEveryDay2);
                    if(isSuccessMiration){
                        migrationNum += 3;
                        break;
                    }
                }
            }

            int remainCoreB = serverHavePurchase.getServerNodeB().getRemainCore();
            int remainMemoryB = serverHavePurchase.getServerNodeB().getRemainMemory();
            if(remainCoreB<=minGateValue && remainMemoryB>=maxGateValue) {
                List<VMHaveRequest> holdVM = serverHavePurchase.getHoldVM();
                for (int i = 0; i < holdVM.size(); i++) {
                    if (migrationNum + 3 > migrationMaxNum) {
                        return;
                    }
                    VMHaveRequest vm = holdVM.get(i);
                    if(vm.getServerNodeId()==0){
                        continue;
                    }
                    int vmCore = vm.getCore();
                    int vmMemory = vm.getMemory();
                    List<VMHaveRequest> vmListByCore = vmCoreMap.get(vmCore);
                    if (vmListByCore.size() <= 1) {
                        continue;
                    }
                    VMHaveRequest targetVm = selectSinTargetVmByCore(vmListByCore, vmMemory, remainMemoryB);
                    if (targetVm == null) {
                        continue;
                    }

                    int targetVmMemory = targetVm.getMemory();
                    int targetVmCore=targetVm.getCore();
                    if(targetVmMemory-vmMemory>remainMemoryB ||targetVmCore!=vmCore ){
                        continue;
                    }
                    int targetServerId = targetVm.getServerId();
                    int serverNodeId = targetVm.getServerNodeId();
                    ServerHavePurchase targetServer = serverIdMap.get(targetServerId);
                    int targetServerRemainCore;
                    int targetServerRemainMemory;
                    if(serverNodeId==0) {
                        targetServerRemainMemory = targetServer.getServerNodeA().getRemainMemory();
                        targetServerRemainCore = targetServer.getServerNodeA().getRemainCore();
                    }else{
                        targetServerRemainMemory = targetServer.getServerNodeB().getRemainMemory();
                        targetServerRemainCore = targetServer.getServerNodeB().getRemainCore();
                    }
                    if(targetServerRemainMemory-targetServerRemainCore<Main.gateValue){
                        continue;
                    }

                    boolean isSuccessMiration = migrationBetweenTwoSinVm(vm,serverHavePurchase, targetVm,targetServer, serverIdMap, serverListAllRanges, vmMigrationMapEveryDay2);

                    if (isSuccessMiration) {
                        migrationNum += 3;
                        break;
                    }

                }
            }
            else if(remainCoreB>=maxGateValue && remainMemoryB<=minGateValue){
                if (migrationNum + 3 > migrationMaxNum) {
                    return;
                }
                List<VMHaveRequest> holdVM = serverHavePurchase.getHoldVM();
                for (int i = 0; i < holdVM.size(); i++) {
                    VMHaveRequest vm = holdVM.get(i);
                    if(vm.getServerNodeId()==0){
                        continue;
                    }

                    int vmCore = vm.getCore();
                    int vmMemory = vm.getMemory();
                    List<VMHaveRequest> vmListByMemory = vmMemoryMap.get(vmMemory);
                    if (vmListByMemory.size() == 1) {
                        continue;
                    }
                    VMHaveRequest targetVm = selectSinTargetVmByMemory(vmListByMemory, vmCore, remainCoreB);
                    if (targetVm == null) {
                        continue;
                    }
                    int targetVmCore = targetVm.getCore();
                    int targetVmMemory = targetVm.getMemory();

                    int targetServerId = targetVm.getServerId();
                    int targetServerNodeId = targetVm.getServerNodeId();
                    ServerHavePurchase targetServer = serverIdMap.get(targetServerId);

                    int targetServerRemainCore;
                    int targetServerRemainMemory;
                    if(targetServerNodeId==0) {
                        targetServerRemainMemory = targetServer.getServerNodeA().getRemainMemory();
                        targetServerRemainCore = targetServer.getServerNodeA().getRemainCore();
                    }else{
                        targetServerRemainMemory = targetServer.getServerNodeB().getRemainMemory();
                        targetServerRemainCore = targetServer.getServerNodeB().getRemainCore();
                    }

                    if(targetServerRemainMemory-targetServerRemainCore<Main.gateValue){
                        continue;
                    }


                    boolean isSuccessMiration = migrationBetweenTwoSinVm(vm,serverHavePurchase, targetVm,targetServer, serverIdMap, serverListAllRanges, vmMigrationMapEveryDay2);
                    if(isSuccessMiration){
                        migrationNum += 3;
                        break;
                    }
                }
            }


        }
    }

    private static boolean migrationBetweenTwoDouVm(VMHaveRequest vm,ServerHavePurchase nowServer,
                                                    VMHaveRequest targetVm,ServerHavePurchase targetServer, Map<Integer, ServerHavePurchase> serverIdMap,
                                                    List<List<ServerHavePurchase>> serverListAllRanges,
                                                    List<Map<Integer, List<Integer>>> vmMigrationMapEveryDay) {
        int nowVmCore = vm.getCore();
        int nowVmMemory = vm.getMemory();

        if(nowServer==null || targetServer==null || targetServer.getId()==nowServer.getId()){
            return false;
        }

        ServerHavePurchase midTempServer;
        for(List<ServerHavePurchase> serverHavePurchaseList :serverListAllRanges){
            for(ServerHavePurchase midServer: serverHavePurchaseList){
                midTempServer=midServer;
                if(nowServer.getId()==midTempServer.getId() || targetServer.getId()==midTempServer.getId()){
                    break;
                }
                int remainCore = midTempServer.getServerNodeA().getRemainCore();
                int remainMemory = midTempServer.getServerNodeA().getRemainMemory();
                if(nowVmCore/2<=remainCore && nowVmMemory/2<=remainMemory){

                    vmMigration(vm,nowServer,midTempServer,2);
                    List<Integer> serverAndNodeId = new ArrayList<>();
                    serverAndNodeId.add(midTempServer.getId());
                    Map<Integer, List<Integer>> tempMap=new HashMap<>();
                    tempMap.put(vm.getId(), serverAndNodeId);
                    vmMigrationMapEveryDay.add(tempMap);

                    vmMigration(targetVm,targetServer,nowServer ,2);
                    List<Integer> serverAndNodeId2 = new ArrayList<>();
                    serverAndNodeId2.add(nowServer.getId());
                    Map<Integer, List<Integer>> tempMap2=new HashMap<>();
                    tempMap2.put(targetVm.getId(), serverAndNodeId2);
                    vmMigrationMapEveryDay.add(tempMap2);

                    vmMigration(vm,midTempServer,targetServer ,2);
                    List<Integer> serverAndNodeId3 = new ArrayList<>();
                    serverAndNodeId3.add(targetServer.getId());
                    Map<Integer, List<Integer>> tempMap3=new HashMap<>();
                    tempMap3.put(vm.getId(), serverAndNodeId3);
                    vmMigrationMapEveryDay.add(tempMap3);
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean migrationBetweenTwoSinVm(VMHaveRequest vm, ServerHavePurchase nowServer, VMHaveRequest targetVm,
                                                    ServerHavePurchase targetServer, Map<Integer, ServerHavePurchase> serverIdMap,
                                                    List<List<ServerHavePurchase>> serverListAllRanges,
                                                    List<Map<Integer, List<Integer>>> vmMigrationMapEveryDay2) {

        if(nowServer==null || targetServer==null || targetServer.getId()==nowServer.getId()){
            return false;
        }
        int nowVmCore = vm.getCore();
        int nowVmMemory = vm.getMemory();

        int nowServerNodeId = vm.getServerNodeId();
        if(nowServerNodeId==1){
            int a=1;
        }
        int targetServerNodeId=targetVm.getServerNodeId();


        ServerHavePurchase midTempServer;
        for(List<ServerHavePurchase> serverHavePurchaseList :serverListAllRanges){
            for(ServerHavePurchase midServer: serverHavePurchaseList){
                midTempServer=midServer;
                if(nowServer.getId()==midTempServer.getId() || targetServer.getId()==midTempServer.getId()){
                    break;
                }
                int remainCoreA = midTempServer.getServerNodeA().getRemainCore();
                int remainMemoryA = midTempServer.getServerNodeA().getRemainMemory();
                if(nowVmCore<=remainCoreA && nowVmMemory<=remainMemoryA){
                    vmMigration(vm,nowServer,midTempServer,0);
                    List<Integer> serverAndNodeId = new ArrayList<>();
                    serverAndNodeId.add(midTempServer.getId());
                    serverAndNodeId.add(0);
                    Map<Integer, List<Integer>> tempMap=new HashMap<>();
                    tempMap.put(vm.getId(), serverAndNodeId);
                    vmMigrationMapEveryDay2.add(tempMap);

                    vmMigration(targetVm,targetServer,nowServer ,nowServerNodeId);
                    List<Integer> serverAndNodeId2 = new ArrayList<>();
                    serverAndNodeId2.add(nowServer.getId());
                    serverAndNodeId2.add(nowServerNodeId);
                    Map<Integer, List<Integer>> tempMap2=new HashMap<>();
                    tempMap2.put(targetVm.getId(), serverAndNodeId2);
                    vmMigrationMapEveryDay2.add(tempMap2);

                    vmMigration(vm,midTempServer,targetServer ,targetServerNodeId);
                    List<Integer> serverAndNodeId3 = new ArrayList<>();
                    serverAndNodeId3.add(targetServer.getId());
                    serverAndNodeId3.add(targetServerNodeId);
                    Map<Integer, List<Integer>> tempMap3=new HashMap<>();
                    tempMap3.put(vm.getId(), serverAndNodeId3);
                    vmMigrationMapEveryDay2.add(tempMap3);
                    return true;
                }
            }
        }
        return false;
    }


    private static VMHaveRequest selectDouTargetVmByMemory(List<VMHaveRequest> vmListByMemory, int vmCore, int remainCore) {
        int maxCore=0;
        int selectIndex=-1;
        for (int i = 0; i < vmListByMemory.size(); i++) {
            VMHaveRequest vm = vmListByMemory.get(i);
            int core = vm.getCore();
            if (core > vmCore && (core - vmCore)/2 <= remainCore && (core - vmCore)/2 > remainCore/2) {
                if (maxCore < core) {
                    maxCore = core;
                    selectIndex=i;
                }
            }
        }
        if(selectIndex==-1){
            return null;
        }else{
            return vmListByMemory.get(selectIndex);
        }
    }

    private static VMHaveRequest selectDouTargetVmByCore(List<VMHaveRequest> vmListByCore, int vmMemory, int remainMemory) {
        int maxMemory=0;
        int selectIndex=-1;

        for (int i = 0; i < vmListByCore.size(); i++) {
            VMHaveRequest vm = vmListByCore.get(i);
            int memory = vm.getMemory();
            if (memory > vmMemory && (memory - vmMemory)/2 <= remainMemory && (memory - vmMemory)/2 > remainMemory/2) {
                if (maxMemory < memory) {
                    maxMemory = memory;
                    selectIndex=i;
                }
            }
        }
        if(selectIndex==-1){
            return null;
        }else{
            return vmListByCore.get(selectIndex);
        }

    }
    private static VMHaveRequest selectSinTargetVmByMemory(List<VMHaveRequest> vmListByMemory, int vmCore, int remainCore) {
        int maxCore=0;
        int selectIndex=-1;
        for (int i = 0; i < vmListByMemory.size(); i++) {
            VMHaveRequest vm = vmListByMemory.get(i);
            int core = vm.getCore();
            if (core > vmCore && (core - vmCore) <= remainCore && (core - vmCore) > remainCore/2) {
                if (maxCore < core) {
                    maxCore = core;
                    selectIndex=i;
                }
            }
        }
        if(selectIndex==-1){
            return null;
        }else{
            return vmListByMemory.get(selectIndex);
        }
    }

    private static VMHaveRequest selectSinTargetVmByCore(List<VMHaveRequest> vmListByCore, int vmMemory, int remainMemory) {
        int maxMemory=0;
        int selectIndex=-1;

        for (int i = 0; i < vmListByCore.size(); i++) {
            VMHaveRequest vm = vmListByCore.get(i);
            int memory = vm.getMemory();
            if (memory > vmMemory && memory - vmMemory <= remainMemory  && memory - vmMemory > remainMemory/2) {
                if (maxMemory < memory) {
                    maxMemory = memory;
                    selectIndex=i;
                }
            }
        }
        if(selectIndex==-1){
            return null;
        }else{
            return vmListByCore.get(selectIndex);
        }

    }


}
