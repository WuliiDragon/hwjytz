package com.huawei.java.main.util;

import com.huawei.java.main.entity.Server;
import com.huawei.java.main.entity.ServerHavePurchase;
import com.huawei.java.main.entity.ServerNode;
import com.huawei.java.main.entity.VMHaveRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author mkm_xjtu
 * @Date 2021/3/12
 */
public class Utils {
    /**
     * @param serverList
     * @param vm
     * @param id:当前服务器的id
     * @return
     */
    public static ServerHavePurchase selectServerByVM(List<ServerHavePurchase> serverHavePurchaseList,
                                                      List<Server> serverList, VMHaveRequest vm, int id) {

        int deployType = vm.isDeployType();

        // 在以前购买的服务器里面查找是否可以分配虚拟机
        Map<String, Integer> result = checkHavePurchaseListForVM(serverHavePurchaseList, vm);
        int index = result.get("server");
        int deployMode = result.get("deployMode");
        ServerHavePurchase serverHavePurchase = null;
        //找到合适
        if (index != -1) {
            //装
            serverHavePurchase = serverHavePurchaseList.get(index);
            serverHavePurchase.addVM(vm, deployMode);
            return null;
        }
        //买
        serverHavePurchase = buyServer(serverList, deployType, vm, id);
        //装
        deployMode = checkPutModel(serverHavePurchase, vm);
        serverHavePurchase.addVM(vm, deployMode);
        return serverHavePurchase;
    }

    public static List<Server> randomServerList;


    public static ServerHavePurchase buyServer(List<Server> serverList, int deployType, VMHaveRequest vm, int id) {
        int vmCore = vm.getCore();
        int vmMemory = vm.getMemory();


        for (Server server : serverList) {
            int serverCore = server.getCore();
            int serverMemory = server.getMemory();

            if (deployType == 0) {
                //单节点 只需要服务器的一半能放的下
                if (serverCore / 2 >= vmCore && serverMemory / 2 >= vmMemory) {
                    return BuyServer(vm, id, server, serverCore, serverMemory);
                }
            } else {
                //双节点 需要两个
                if (serverCore / 2 >= vmCore / 2 && serverMemory / 2 >= vmMemory / 2) {
                    return BuyServer(vm, id, server, serverCore, serverMemory);
                }
            }
        }
        return null;
    }

    private static ServerHavePurchase BuyServer(VMHaveRequest vm, int id, Server server, int serverCore, int serverMemory) {
        ServerHavePurchase serverHavePurchase;
        ServerNode serverNodeA = new ServerNode(1, id, serverCore / 2, serverMemory / 2);
        ServerNode serverNodeB = new ServerNode(2, id, serverCore / 2, serverMemory / 2);

        serverHavePurchase = new ServerHavePurchase(server, id, serverNodeA, serverNodeB);
        return serverHavePurchase;
    }

    private static int checkPutModel(ServerHavePurchase server, VMHaveRequest vm) {
        int type = vm.isDeployType();

        //单
        if (type == 0) {
            int reCA = server.getServerNodeA().getRemainCore() - vm.getCore();
            int reMA = server.getServerNodeA().getRemainMemory() - vm.getMemory();
            int reCB = server.getServerNodeB().getRemainCore() - vm.getCore();
            int reMB = server.getServerNodeB().getRemainMemory() - vm.getMemory();
            boolean AisOK = (reCA >= 0 && reMA >= 0);
            boolean BisOK = (reCB >= 0 && reMB >= 0);


            if (AisOK && BisOK) {
                boolean ARemainLargeB = (reCA >= reCB) && (reMA >= reMB);
                if (ARemainLargeB) {
                    return 0;
                } else {
                    return 1;
                }
            } else if (AisOK) {
                return 0;
            } else if (BisOK) {
                return 1;
            } else {
                return -1;
            }

        } else {
            //双

            int reCA = (server.getServerNodeA().getRemainCore() - vm.getCore() / 2);
            int reMA = (server.getServerNodeA().getRemainMemory() - vm.getMemory() / 2);
            int reCB = (server.getServerNodeB().getRemainCore() - vm.getCore() / 2);
            int reMB = (server.getServerNodeB().getRemainMemory() - vm.getMemory() / 2);

            boolean AisOK = (reCA >= 0 && reMA >= 0);
            boolean BisOK = (reCB >= 0 && reMB >= 0);

            boolean ABisOK = AisOK && BisOK;
            if (ABisOK) {
                return 2;
            } else {
                return -1;
            }
        }

    }

    private static Map<String, Integer> checkHavePurchaseListForVM(List<ServerHavePurchase> serverHavePurchaseList, VMHaveRequest vm) {

        Map<String, Integer> result = new HashMap<String, Integer>();
        for (ServerHavePurchase server : serverHavePurchaseList) {
            int deployMode = checkPutModel(server, vm);
            if (deployMode != -1) {
                result.put("server", serverHavePurchaseList.indexOf(server));
                result.put("deployMode", deployMode);
                return result;
            }


        }
        result.put("server", -1);
        result.put("deployMode", -1);
        return result;
    }

    public static void handleVmRequest(VMHaveRequest vm) {




    }


    private static Server selectServerByCoreAndMemory(List<Server> serverList, int core, int memory, int deployType) {
        //双节点
        if (deployType == 1) {
            for (Server server : serverList) {
                int serverCore = server.getCore();
                int serverMemory = server.getMemory();
                if (serverCore >= core && serverMemory >= memory) {

                }
            }
        }
        return null;
    }

    public static String buildPath = "../../data/training-";

}
