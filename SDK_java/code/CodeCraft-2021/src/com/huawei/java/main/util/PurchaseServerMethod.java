package com.huawei.java.main.util;

import com.huawei.java.main.Main;
import com.huawei.java.main.entity.Server;
import com.huawei.java.main.entity.ServerHavePurchase;
import com.huawei.java.main.entity.VMHaveRequest;

import java.util.*;

/**
 * @Author mkm_xjtu
 * @Date 2021/3/14
 */

/**
 *
 */
public class PurchaseServerMethod {

    public static List<Integer> getVmCoreAndMemSumByOneList(List<VMHaveRequest> vmHaveRequestList){
        int coreSum=0,memSum=0;
        for(VMHaveRequest vm:vmHaveRequestList){
            coreSum+=vm.getCore();
            memSum+=vm.getMemory();
        }
        List<Integer> params = new ArrayList<>();
        params.add(coreSum);
        params.add(memSum);
        return params;
    }


    /**
     * @param serverTypeList:按着core主降序，mem次降序
     * @param sumCore：虚拟机的总核数
     * @param sumMem：总内存
     * @param extendRate:多出的比例，如1.1表示服务器的总内存多出10%作为阈度
     * @return 返回map<服务器名，购买数量>
     */
    public static Map<String,Integer> purchaseServerPlan1(List<Server> serverTypeList , float sumCore, float sumMem,float extendRate){

        //当前购买的服务器的容量
        int nowCoreSum=0;
        int nowMemSum=0;
        //该分组中所有类型的服务器的总容量
        int serverCoreSum=0;
        int serverMemSum=0;
        Map<String,Integer> serverNameIntMap=new HashMap<>();
        /**
         * 先算虚拟机的总内存和总核数是服务器的倍数，直接买对应的倍数倍。每种买times个
         */

        float coreWeight=sumMem/(sumCore+sumMem);
        float memWeight=sumCore/(sumCore+sumMem);

        for(Server server:serverTypeList){
            float purchaseCost = (float)server.getPurchaseCost();
            float valueForMoney=purchaseCost/server.getCore()*coreWeight+purchaseCost/server.getMemory()*memWeight;
            server.setValueForMoney(valueForMoney);
        }
        Collections.sort(serverTypeList, Comparator.comparing(Server::getValueForMoney));

        for (int i = 0; i < Math.min(Main.topSelectToBuy,serverTypeList.size()) ; i++) {
            Server server = serverTypeList.get(i);
            serverCoreSum+=server.getCore();
            serverMemSum+=server.getMemory();
        }

        int coreTimes= (int) (sumCore*extendRate/serverCoreSum);
        int memTimes= (int) (sumMem*extendRate/serverMemSum);
        int times=Math.max(coreTimes,memTimes);

        //买前五种性价比最好的
        for (int i = 0; i < Math.min(Main.topSelectToBuy,serverTypeList.size()); i++) {
            Server server = serverTypeList.get(i);
            String serverName = server.getTypeName();
            nowCoreSum += server.getCore() * times;
            nowMemSum += server.getMemory() * times;
            if (serverNameIntMap.containsKey(serverName)) {
                int serverNum = serverNameIntMap.get(serverName);
                serverNameIntMap.put(serverName, serverNum + times);
            } else {
                serverNameIntMap.put(serverName, times);
            }
            if (nowCoreSum >= sumCore * extendRate && nowMemSum >= sumMem * extendRate) {
                break;
            }
        }
        /**
         * 剩余部分从性价比最大的再开始，每种买一个
         */
        for (int i = 0; i < Math.min(Main.topSelectToBuy,serverTypeList.size()) ; i++) {
            Server server = serverTypeList.get(i);
            String serverName = server.getTypeName();
            nowCoreSum += server.getCore();
            nowMemSum += server.getMemory();
            if (serverNameIntMap.containsKey(serverName)) {
                int serverNum = serverNameIntMap.get(serverName);
                serverNameIntMap.put(serverName, serverNum + 1);
            } else {
                serverNameIntMap.put(serverName, 1);
            }
            if (nowCoreSum >= sumCore * extendRate && nowMemSum >= sumMem * extendRate) {
                return serverNameIntMap;
            }
        }
        return serverNameIntMap;
    }



}
