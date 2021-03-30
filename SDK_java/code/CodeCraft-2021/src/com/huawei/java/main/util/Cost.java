package com.huawei.java.main.util;

/**
 * @author Lofit .LaeL
 * @email GreatLandmark@outlook.com
 * 2021/3/12 20:37
 */

import com.huawei.java.main.Main;
import com.huawei.java.main.entity.Server;
import com.huawei.java.main.entity.ServerHavePurchase;

import java.util.List;
import java.util.Map;

public class Cost {
    /**
     * Calculate cost of each day
     *
     * @param purchasedServer
     * @param activeServer
     * @return int
     */
    public static int calculateDayCost(List<Server> purchasedServer, List<Server> activeServer) {
        int totalDayCost = 0;
        for (int i = 0; i < purchasedServer.size(); i++) {
            totalDayCost += purchasedServer.get(i).getPurchaseCost();
        }
        for (int i = 0; i < activeServer.size(); i++) {
            totalDayCost += activeServer.get(i).getEnergyConsumptionPerDay();
        }
        return totalDayCost;
    }

    /**
     * @param purchasedServer 购买的server列表
     * @return int
     */
    public static int calculatePurchasedCost(List<Server> purchasedServer) {
        int purchasedCost = 0;
        for (int i = 0; i < purchasedServer.size(); i++) {
            purchasedCost += purchasedServer.get(i).getPurchaseCost();
        }
        return purchasedCost;
    }


    public static void AddCostADay(Map<String, List<List<ServerHavePurchase>>> serverHaveBuySinAndDouList) {

        // 单节点运行的服务器
        List<List<ServerHavePurchase>> sinServerList = serverHaveBuySinAndDouList.get("single");
        // 双节点运行的服务器
        List<List<ServerHavePurchase>> douServerList = serverHaveBuySinAndDouList.get("double");

        for (List<ServerHavePurchase> serverList : sinServerList) {

            for (ServerHavePurchase serverHavePurchase : serverList) {
                if (serverHavePurchase.getHoldVM().size() != 0) {
                    serverHavePurchase.addCostADay();
                }

            }


        }
        for (List<ServerHavePurchase> serverList : douServerList) {

            for (ServerHavePurchase serverHavePurchase : serverList) {
                if (serverHavePurchase.getHoldVM().size() != 0) {
                    serverHavePurchase.addCostADay();
                }
            }
        }
    }

    public static void AddCostAllDay(Map<String, List<List<ServerHavePurchase>>> serverHaveBuySinAndDouList) {

        // 单节点运行的服务器
        List<List<ServerHavePurchase>> sinServerList = serverHaveBuySinAndDouList.get("single");
        // 双节点运行的服务器
        List<List<ServerHavePurchase>> douServerList = serverHaveBuySinAndDouList.get("double");

        for (List<ServerHavePurchase> serverList : sinServerList) {

            for (ServerHavePurchase serverHavePurchase : serverList) {
                Main.totalCost+=serverHavePurchase.getHaveCost();
            }


        }
        for (List<ServerHavePurchase> serverList : douServerList) {
            for (ServerHavePurchase serverHavePurchase : serverList) {
                Main.totalCost+=serverHavePurchase.getHaveCost();
            }
        }

    }

}
