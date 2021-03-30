package com.huawei.java.main.decision;

import com.huawei.java.main.Main;
import com.huawei.java.main.entity.Server;
import com.huawei.java.main.entity.ServerHavePurchase;

import java.util.ArrayList;
import java.util.List;

/**
 * 静态划分区间
 *
 * @author Lofit .LaeL
 * @email GreatLandmark@outlook.com
 * @date 2021/3/14
 */
public class DivideIntervalzyl {
    /**
     * 首和尾暂不合并为一个区间。
     * 例如：划分为intervalNum = 4个区间; 小于等于threshold1和大于threshold4 为一个去区间。
     */
    //    ====================================================================================================================================================================================
    public static int calRangeIndex(float mcRate, float range[]) {

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

    public static List<List<ServerHavePurchase>> divideServerByRemainMemCoreEightRange1(List<List<ServerHavePurchase>> serverListAllRange, int douOrSin, float range[]) {

        List<List<ServerHavePurchase>> list = new ArrayList<List<ServerHavePurchase>>();
        for (int i = 0; i < range.length ; i++) {
            List<ServerHavePurchase> interval = new ArrayList<>();
            list.add(interval);
        }


        if (douOrSin == 0) {
            for (List<ServerHavePurchase> serverListEveryRange : serverListAllRange) {
                for (ServerHavePurchase serverHavePurchase : serverListEveryRange) {
                    float rate = (float) serverHavePurchase.getServerNodeA().getRemainMemory() / serverHavePurchase.getServerNodeA().getRemainCore();
                    int index = calRangeIndex(rate, Main.rangeVM);
                    list.get(index).add(serverHavePurchase);
                }
            }
        } else if (douOrSin == 1) {
            for (List<ServerHavePurchase> serverListEveryRange : serverListAllRange) {
                for (ServerHavePurchase serverHavePurchase : serverListEveryRange) {
                    float remainMemoryA = serverHavePurchase.getServerNodeA().getRemainMemory();
                    float remainMemoryB = serverHavePurchase.getServerNodeB().getRemainMemory();
                    float remainCoreA = serverHavePurchase.getServerNodeA().getRemainCore();
                    float remainCoreB = serverHavePurchase.getServerNodeB().getRemainCore();
                    float rate = 0.0f;


                    if (remainMemoryA + remainCoreA >= remainMemoryB + remainCoreB) {
                          rate = (float) serverHavePurchase.getServerNodeA().getRemainMemory() / serverHavePurchase.getServerNodeA().getRemainCore();
                    } else {
                         rate = (float) serverHavePurchase.getServerNodeB().getRemainMemory() / serverHavePurchase.getServerNodeB().getRemainCore();
                    }


                    int index = calRangeIndex(rate, Main.rangeVM);
//                    int index1 = calRangeIndex(rate1, Main.rangeVM);
//                    int index2 = calRangeIndex(rate2, Main.rangeVM);
//                    if(index1!=index2){
//                        list.get(index1).add(serverHavePurchase);
//                        list.get(index2).add(serverHavePurchase);
//                    }else{
//                        list.get(index1).add(serverHavePurchase);
//                    }

                    list.get(index).add(serverHavePurchase);

                }
            }
        }
        return list;
    }


    public static List<List<Server>> divideServerByMemCoreEightRange(List<Server> serverList, float range[]) {

        List<List<Server>> list = new ArrayList<List<Server>>();
        for (int i = 0; i < range.length + 2; i++) {
            List<Server> interval = new ArrayList<>();
            list.add(interval);
        }
        serverList.forEach(server -> {
            float rate = (float) server.getMemory() / server.getCore();
            int index = calRangeIndex(rate, Main.rangeVM);
            list.get(index).add(server);


        });

        return list;
    }


}
