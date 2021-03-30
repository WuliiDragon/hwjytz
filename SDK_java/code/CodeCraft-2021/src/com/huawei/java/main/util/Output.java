package com.huawei.java.main.util;

import com.huawei.java.main.Main;
import com.huawei.java.main.entity.ServerHavePurchase;
import com.huawei.java.main.entity.VMHaveRequest;

import java.util.*;

/**
 * @Author mkm_xjtu
 * @Date 2021/3/12
 */
public class Output {


    /**
     * @param serverMapBuyToday:购买服务器的总的列表,Map(name,num)
     * @param vmMigrationMap：每天虚拟机迁移的map表示几号虚拟机迁移到几号服务器的那个节点 List存放的是迁移虚拟机涉及的服务器id和节点，如果不涉及节点，则只存服务器id
     * @param vmRequestToday:表示每天发出的虚拟机请求，
     * @return 返回购买的所有服务器idMap
     */
    public static void everydayOutput(Map<String, List<List<ServerHavePurchase>>> serverHaveBuySinAndDouList,
                                      Map<String, List<List<ServerHavePurchase>>> serverMapBuyToday,
                                      Map<Integer, List<Integer>> vmMigrationMap,
                                      List<VMHaveRequest> vmRequestToday,
                                      Map<Integer, ServerHavePurchase> serverIdMap) {
        everydayOutputBuyServer(serverHaveBuySinAndDouList,serverMapBuyToday,serverIdMap);
//        everydayOutputVMMigration(vmMigrationMap);
        everydayOutputVMMigration_range0(vmMigrationMap, Main.vmMigrationMapEveryDay2);
        everydayOutputVMLoadOnServer(vmRequestToday);

    }


    /**
     * @param serverNameNumBuyEverydayMap
     * @param serverIdMap
     * @return 返回所有购买过的服务器的IdMap
     */
    public static Map<Integer, ServerHavePurchase> InitBuyServerOutput(Map<String, List<List<ServerHavePurchase>>> serverNameNumBuyEverydayMap,
                                                                       Map<Integer, ServerHavePurchase> serverIdMap) {
        if (serverNameNumBuyEverydayMap == null || serverNameNumBuyEverydayMap.size() == 0) {
            System.out.println("(purchase, 0)");
            return serverIdMap;
        }

        List<List<ServerHavePurchase>> douNodeServerFourRangeList = serverNameNumBuyEverydayMap.get("double");
        List<List<ServerHavePurchase>> sinNodeServerFourRangeList = serverNameNumBuyEverydayMap.get("single");

        Map<String, List<ServerHavePurchase>> serverNameTreeMap = new LinkedHashMap<>();

        for (List<ServerHavePurchase> everyRangeList : sinNodeServerFourRangeList) {

            for (ServerHavePurchase serverHavePurchase : everyRangeList) {
                String typeName = serverHavePurchase.getTypeName();
                if (!serverNameTreeMap.containsKey(typeName)) {
                    List<ServerHavePurchase> serverList = new ArrayList<>();
                    serverList.add(serverHavePurchase);
                    serverNameTreeMap.put(typeName, serverList);
                } else {
                    List<ServerHavePurchase> serverList = serverNameTreeMap.get(typeName);
                    serverList.add(serverHavePurchase);
                    serverNameTreeMap.put(typeName, serverList);
                }
            }
        }

        for (List<ServerHavePurchase> everyRangeList : douNodeServerFourRangeList) {
            for (ServerHavePurchase serverHavePurchase : everyRangeList) {
                String typeName = serverHavePurchase.getTypeName();
                if (!serverNameTreeMap.containsKey(typeName)) {
                    List<ServerHavePurchase> serverList = new ArrayList<>();
                    serverList.add(serverHavePurchase);
                    serverNameTreeMap.put(typeName, serverList);
                } else {
                    List<ServerHavePurchase> serverList = serverNameTreeMap.get(typeName);
                    serverList.add(serverHavePurchase);
                    serverNameTreeMap.put(typeName, serverList);
                }
            }
        }
        System.out.println("(purchase, " + serverNameTreeMap.size() + ")");

        int id = 0;
        // 先单节点后双节点
        for (String name : serverNameTreeMap.keySet()) {
            List<ServerHavePurchase> serverPurchaseList = serverNameTreeMap.get(name);
            for (ServerHavePurchase server : serverPurchaseList) {
                server.setId(id);
                server.getServerNodeA().setServerId(id);
                server.getServerNodeB().setServerId(id);
                id += 1;
            }
            System.out.println("(" + name + ", " + serverNameTreeMap.get(name).size() + ")");
        }
        //统计单双节点购买的总服务器数量
        for (List<ServerHavePurchase> rangeServerList : douNodeServerFourRangeList) {
            for (ServerHavePurchase server : rangeServerList) {
                serverIdMap.put(server.getId(), server);
            }
        }
        for (List<ServerHavePurchase> rangeServerList : sinNodeServerFourRangeList) {
            for (ServerHavePurchase server : rangeServerList) {
                serverIdMap.put(server.getId(), server);
            }
        }
        return serverIdMap;
    }

    public static void everydayOutputBuyServer(Map<String, List<List<ServerHavePurchase>>> serverHaveBuySinAndDouList,
                                               Map<String, List<List<ServerHavePurchase>>> serverMapBuyToday,
                                               Map<Integer, ServerHavePurchase> serverIdMap) {
        // 不买的情况
        if(serverMapBuyToday==null || serverMapBuyToday.size()==0){
            System.out.println("(purchase, 0)");
            return ;
        }
        boolean isEmpty=true;
        for(String name:serverMapBuyToday.keySet()){
            List<List<ServerHavePurchase>> lists = serverMapBuyToday.get(name);
            for(List<ServerHavePurchase> s:lists){
                if(s!=null && s.size()!=0){
                    isEmpty=false;
                }
            }
        }
        if(isEmpty){
            System.out.println("(purchase, 0)");
            return ;
        }

        //得到购买的所有服务器列表
        Map<String,List<ServerHavePurchase>> serverNameNumMap=new LinkedHashMap<>();
        List<ServerHavePurchase> serverHavePurchaseList=new ArrayList<>();
        for(String sinOrDou:serverMapBuyToday.keySet()){
            List<List<ServerHavePurchase>> lists = serverMapBuyToday.get(sinOrDou);
            for (int i = 0; i < lists.size(); i++) {
                List<ServerHavePurchase> serverList = lists.get(i);
                if (serverList == null || serverList.size() == 0) {
                    continue;
                }
                for (ServerHavePurchase serverHavePurchase : serverList) {
                    serverHavePurchaseList.add(serverHavePurchase);
                    serverHaveBuySinAndDouList.get(sinOrDou).get(i).add(serverHavePurchase);
                }
            }
        }
//        Collections.sort(serverHavePurchaseList, Comparator.comparing(ServerHavePurchase::getId));


        //将购买的服务器存放在map中，map<name,List<Server>>
        for(ServerHavePurchase serverHavePurchase:serverHavePurchaseList){
            String typeName = serverHavePurchase.getTypeName();
            if(serverNameNumMap.containsKey(typeName)){
                List<ServerHavePurchase> serverHavePurchaseList1 = serverNameNumMap.get(typeName);
                serverHavePurchaseList1.add(serverHavePurchase);
                serverNameNumMap.put(serverHavePurchase.getTypeName(),serverHavePurchaseList1);
            }else{
                List<ServerHavePurchase> serverHavePurchaseList1=new ArrayList<>();
                serverHavePurchaseList1.add(serverHavePurchase);
                serverNameNumMap.put(serverHavePurchase.getTypeName(),serverHavePurchaseList1);
            }
        }

        System.out.println("(purchase, "+serverNameNumMap.size()+")");

        // 将购买的服务器根据输出赋予id
        for (String name : serverNameNumMap.keySet()) {
            List<ServerHavePurchase> serverHavePurchaseList1 = serverNameNumMap.get(name);
            for(int i=0;i<serverHavePurchaseList1.size();++i){
                ServerHavePurchase serverHavePurchase = serverHavePurchaseList1.get(i);
                int id = serverIdMap.size();
                serverHavePurchase.setId(id);
                serverHavePurchase.getServerNodeA().setServerId(id);
                serverHavePurchase.getServerNodeB().setServerId(id);
                for(VMHaveRequest vm:serverHavePurchase.getHoldVM()){
                    vm.setServerId(id);
                }
                serverIdMap.put(serverHavePurchase.getId(),serverHavePurchase);
            }

            System.out.println("(" + name + ", " + serverNameNumMap.get(name).size() + ")");
        }
    }


        public static void everydayOutputVMMigration_range0(Map<Integer, List<Integer>> vmMigrationMap,
                                                 List<Map<Integer, List<Integer>>> vmMigrationMap2) {
        //输出迁移多少虚拟机
        if ((vmMigrationMap == null || vmMigrationMap.isEmpty()) && (vmMigrationMap2 == null || vmMigrationMap2.isEmpty())) {
            System.out.println("(migration, 0)");
        } else if((vmMigrationMap==null || vmMigrationMap.size()==0) ){
            System.out.println("(migration, " + vmMigrationMap2.size() + ")");
            for(Map<Integer, List<Integer>> map: vmMigrationMap2) {
                for (Integer vmId : map.keySet()) {
                    //判断迁移虚拟机是否涉及节点问题
                    if (map.get(vmId).size() == 1) {
                        System.out.println("(" + vmId + ", " + map.get(vmId).get(0) + ")");
                    } else {
                        Integer serverNodeId = map.get(vmId).get(1);
                        if (serverNodeId == 0) {
                            System.out.println("(" + vmId + ", " + map.get(vmId).get(0) + ", " + "A)");
                        } else if (map.get(vmId).get(1) == 1) {
                            System.out.println("(" + vmId + ", " + map.get(vmId).get(0) + ", " + "B)");
                        }
                    }
                }
            }

        }else if(vmMigrationMap2 == null || vmMigrationMap2.isEmpty()) {
            System.out.println("(migration, " + vmMigrationMap.size() + ")");
            for (Integer vmId : vmMigrationMap.keySet()) {
                //判断迁移虚拟机是否涉及节点问题
                if (vmMigrationMap.get(vmId).size() == 1) {
                    System.out.println("(" + vmId + ", " + vmMigrationMap.get(vmId).get(0) + ")");
                } else {
                    if (vmMigrationMap.get(vmId).get(1) == 0) {
                        System.out.println("(" + vmId + ", " + vmMigrationMap.get(vmId).get(0) + ", " + "A)");
                    } else if (vmMigrationMap.get(vmId).get(1) == 1) {
                        System.out.println("(" + vmId + ", " + vmMigrationMap.get(vmId).get(0) + ", " + "B)");
                    }
                }
            }

        }else{
            int num = vmMigrationMap.size() + vmMigrationMap2.size();
            System.out.println("(migration, " + num + ")");
            for (Integer vmId : vmMigrationMap.keySet()) {
                //判断迁移虚拟机是否涉及节点问题
                if (vmMigrationMap.get(vmId).size() == 1) {
                    System.out.println("(" + vmId + ", " + vmMigrationMap.get(vmId).get(0) + ")");
                } else {
                    if (vmMigrationMap.get(vmId).get(1) == 0) {
                        System.out.println("(" + vmId + ", " + vmMigrationMap.get(vmId).get(0) + ", " + "A)");
                    } else if (vmMigrationMap.get(vmId).get(1) == 1) {
                        System.out.println("(" + vmId + ", " + vmMigrationMap.get(vmId).get(0) + ", " + "B)");
                    }
                }
            }
            for(Map<Integer, List<Integer>> map: vmMigrationMap2) {
                for (Integer vmId : map.keySet()) {
                    //判断迁移虚拟机是否涉及节点问题
                    if (map.get(vmId).size() == 1) {
                        System.out.println("(" + vmId + ", " + map.get(vmId).get(0) + ")");
                    } else {
                        Integer serverNodeId = map.get(vmId).get(1);
                        if (serverNodeId == 0) {
                            System.out.println("(" + vmId + ", " + map.get(vmId).get(0) + ", " + "A)");
                        } else if (map.get(vmId).get(1) == 1) {
                            System.out.println("(" + vmId + ", " + map.get(vmId).get(0) + ", " + "B)");
                        }
                    }
                }
            }
        }
    }

        public static void everydayOutputVMMigration(Map<Integer, List<Integer>> vmMigrationMap) {
        //输出迁移多少虚拟机
        if (vmMigrationMap == null || vmMigrationMap.isEmpty()) {
            System.out.println("(migration, 0)");
        } else {
            System.out.println("(migration, " + vmMigrationMap.size() + ")");
            for ( Integer vmId : vmMigrationMap.keySet()) {
                //判断迁移虚拟机是否涉及节点问题
                if (vmMigrationMap.get(vmId).size() == 1) {
                    System.out.println("(" + vmId + ", " + vmMigrationMap.get(vmId).get(0) + ")");
                } else {
                    if(vmMigrationMap.get(vmId).get(1)==0){
                        System.out.println("(" + vmId + ", " + vmMigrationMap.get(vmId).get(0) + ", " + "A)");
                    }else if(vmMigrationMap.get(vmId).get(1)==1){
                        System.out.println("(" + vmId + ", " + vmMigrationMap.get(vmId).get(0) + ", " + "B)");
                    }
                }
            }
        }
    }




    /**
     * @param vmRequestToday:每天发出的虚拟机请求
     */
    public static void everydayOutputVMLoadOnServer(List<VMHaveRequest> vmRequestToday) {
        // 输出虚拟机的分配

        for (VMHaveRequest vm : vmRequestToday) {
            if (vm.getAddOrDel() == 1) {
                if (vm.getServerNodeId() == 2) {
                    System.out.println("(" + vm.getServerId() + ")");
                } else if (vm.getServerNodeId() == 0) {
                    System.out.println("(" + vm.getServerId() + ", " + "A)");
                } else {
                    System.out.println("(" + vm.getServerId() + ", " + "B)");
                }
            }
        }
    }

    public static void outPutServerFreeRateInaDay(List<List<ServerHavePurchase>> sinServerList, List<List<ServerHavePurchase>> douServerList) {


        int type = 1;
        for (List<ServerHavePurchase> sinServerListType : sinServerList) {
            StringBuilder freeCStr = new StringBuilder();
            freeCStr.append(type+ " single Core");
            StringBuilder freeMStr = new StringBuilder();
            freeMStr.append(type+ " single Mem");
            type++;
            for (ServerHavePurchase s : sinServerListType) {
                int remainC = s.getServerNodeA().getRemainCore() + s.getServerNodeB().getRemainCore();
                int remainM = s.getServerNodeA().getRemainMemory() + s.getServerNodeB().getRemainMemory();

                float freeC =  ((float) remainC / s.getCore());
                float freeM =  ((float) remainM / s.getMemory());
                freeCStr.append("\t" + freeC);
                freeMStr.append("\t" + freeM);
            }

            System.err.println(freeCStr + "\n");
            System.err.println(freeMStr + "\n");
            System.err.println("====================================");
        }

        type = 1;
        for (List<ServerHavePurchase> sinServerListType : sinServerList) {
            StringBuilder freeCStr = new StringBuilder();
            freeCStr.append(type+ " Dou Core");
            StringBuilder freeMStr = new StringBuilder();
            freeMStr.append(type+ " Dou Mem");
            type++;
            for (ServerHavePurchase s : sinServerListType) {
                int remainC = s.getServerNodeA().getRemainCore() + s.getServerNodeB().getRemainCore();
                int remainM = s.getServerNodeA().getRemainMemory() + s.getServerNodeB().getRemainMemory();

                float freeC =  ((float) remainC / s.getCore());
                float freeM =  ((float) remainM / s.getMemory());
                freeCStr.append("\t" + freeC);
                freeMStr.append("\t" + freeM);
            }

            System.err.println(freeCStr + "\n");
            System.err.println(freeMStr + "\n");
            System.err.println("====================================");
        }
    }


    //子函数
    private static Map<String, Integer> outputServerNameAndNum(List<ServerHavePurchase> serverPurchaseList,
                                                               int serverHavePurchaseNumYesterday) {
        HashMap<String, Integer> map = new HashMap<>();
        for (int i = serverHavePurchaseNumYesterday; i < serverPurchaseList.size(); ++i) {
            String typeName = serverPurchaseList.get(i).getTypeName();
            if (map.containsKey(typeName)) {
                map.put(typeName, map.get(typeName) + 1);
            } else {
                map.put(typeName, 1);
            }
        }
        return map;
    }


    public static void outPutServerFreeRateFinal() {

    }
}
