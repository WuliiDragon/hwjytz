package com.huawei.java.main.util;

import com.huawei.java.main.Main;
import com.huawei.java.main.entity.Server;
import com.huawei.java.main.entity.VM;
import com.huawei.java.main.entity.VMHaveRequest;

import java.io.File;
import java.util.*;

/**
 * @Author mkm_xjtu
 * @Date 2021/3/11
 */
public class Initialize {
    public static List<Server> initServer() {
        return new ArrayList<Server>();
    }

    public static List<VM> initVM() {
        return new ArrayList<VM>();
    }

    public static int sumCore;
    public static int sumMem;

    public static Map<String, VM> vmTypeMap = new HashMap<>();
    public static Map<Integer, VMHaveRequest> vmIdMap = new HashMap<>();
    public static Map<String, Server> serverTypeMap = new HashMap<>();

    public static List<List<VMHaveRequest>> initVMRequest() {
        List<List<VMHaveRequest>> vmHavePurchaseListAllDay = new ArrayList<>();
        return vmHavePurchaseListAllDay;
    }
    public static List<Server> serverTypeListOrderByEnergy(List<Server>  serverTypeList) throws CloneNotSupportedException {

        serverTypeList.sort(Comparator.comparing(Server::getEnergyConsumptionPerDay));

        return serverTypeList;
    }

    public static void initialize(List<Server> serverTypeList,
                                  List<VM> vmTypeList,
                                  List<List<VMHaveRequest>> vmHaveRequestListAllDay) {
        Scanner scan;
        if (!Main.isMatch) {
            File file = new File(Main.path);
            if (file.isFile() && file.exists()) {
                try {
                    scan = new Scanner(file);
                } catch (Exception e) {
                    e.printStackTrace();
                    scan = null;
                }
            } else {
                scan = null;
            }
        } else {
            scan = new Scanner(System.in);
        }

        // server
        int num = Integer.parseInt(scan.nextLine().trim());

        while (num-- > 0) {
            String stringLineServer = scan.nextLine();
            String[] serverDetails = stringLineServer.substring(1, stringLineServer.length() - 1).split(", ");
            //添加服务器信息

            Server s = new Server(serverDetails[0],
                    Integer.parseInt(serverDetails[1]),
                    Integer.parseInt(serverDetails[2]),
                    Integer.parseInt(serverDetails[3]),
                    Integer.parseInt(serverDetails[4]));
            serverTypeMap.put(serverDetails[0], s);

            serverTypeList.add(s);
        }

        // VM
        int vmNum = Integer.parseInt(scan.nextLine().trim());

        while (vmNum-- > 0) {
            String stringLineVM = scan.nextLine();
            String[] vmDetails = stringLineVM.substring(1, stringLineVM.length() - 1).split(", ");
            //添加虚拟机信息


            VM vm = new VM(vmDetails[0], Integer.parseInt(vmDetails[1]), Integer.parseInt(vmDetails[2]), Integer.parseInt(vmDetails[3]));
            vmTypeMap.put(vmDetails[0], vm);
        }

        //对vmTypeList根据name字段排序，方便根据请求查找虚拟机信息
//        Collections.sort(serverTypeList);
//        Collections.sort(vmTypeList);
        /**
         * 第三轮是用户请求天数，由于每一天有不同请求数量，所以单独读取
         */
        //requestDayNum 用户请求的天数

        int requestDayNum = Integer.parseInt(scan.nextLine().trim());


        for (int i = 0; i < requestDayNum; ++i) {
            List<VMHaveRequest> vmHaveRequestListEveryday = new ArrayList<>();
            //dayNum 用户每天请求的数据量
            int dayNum = Integer.parseInt(scan.nextLine().trim());
            for (int j = 0; j < dayNum; ++j) {
                String stringLineRequest = scan.nextLine().trim();
                String[] requestDetails = stringLineRequest.substring(1, stringLineRequest.length() - 1).split(", ");
                String addOrDel = requestDetails[0];
                if ("add".equals(addOrDel)) {
                    String typeName = requestDetails[1];
                    Integer vmId = Integer.parseInt(requestDetails[2]);

                    VM vm = vmTypeMap.get(typeName);
                    VMHaveRequest vmHaveRequest = new VMHaveRequest(vmId, vm, 1);
                    sumCore += vm.getCore();
                    sumMem += vm.getMemory();
                    vmIdMap.put(vmId, vmHaveRequest);
                    vmHaveRequestListEveryday.add(vmHaveRequest);
                } else {
                    Integer vmId = Integer.parseInt(requestDetails[1]);
                    VMHaveRequest vm = vmIdMap.get(vmId);
                    VMHaveRequest vmHaveRequest = new VMHaveRequest(vm.getId(), vm, 0);

                    vmHaveRequestListEveryday.add(vmHaveRequest);
                }
            }

            vmHaveRequestListAllDay.add(vmHaveRequestListEveryday);
        }

    }

}
