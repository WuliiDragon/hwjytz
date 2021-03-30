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
    public static Map<Integer, VMHaveRequest> vmIdMap = new HashMap<Integer, VMHaveRequest>();
    public static Map<String, Server> serverTypeMap = new HashMap<>();

    public static List<List<VMHaveRequest>> initVMRequest() {
        List<List<VMHaveRequest>> vmHavePurchaseListAllDay = new ArrayList<>();
        return vmHavePurchaseListAllDay;
    }

    public static List<Server> serverTypeListOrderByEnergy(List<Server> serverTypeList) throws CloneNotSupportedException {
        serverTypeList.sort(Comparator.comparing(Server::getEnergyConsumptionPerDay));
        return serverTypeList;
    }


    public static void readServer(List<Server> serverTypeList) {
        int num = Integer.parseInt(scanner.nextLine().trim());
        while (num-- > 0) {
            String stringLineServer = scanner.nextLine();
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
    }

    public static List<VMHaveRequest> readDay() {

        int dayNum = Integer.parseInt(scanner.nextLine().trim());
        List<VMHaveRequest> vmHaveRequestListADay = new ArrayList<VMHaveRequest>();
        for (int j = 0; j < dayNum; ++j) {
            String stringLineRequest = scanner.nextLine().trim();
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
                vmHaveRequestListADay.add(vmHaveRequest);
            } else {
                Integer vmId = Integer.parseInt(requestDetails[1]);
                VMHaveRequest vm = vmIdMap.get(vmId);
                VMHaveRequest vmHaveRequest = new VMHaveRequest(vm.getId(), vm, 0);

                vmHaveRequestListADay.add(vmHaveRequest);
            }
        }
        return vmHaveRequestListADay;
    }


    public static void readVM(List<VM> vmTypeList) {
        int vmNum = Integer.parseInt(scanner.nextLine().trim());

        while (vmNum-- > 0) {
            String stringLineVM = scanner.nextLine();
            String[] vmDetails = stringLineVM.substring(1, stringLineVM.length() - 1).split(", ");
            VM vm = new VM(vmDetails[0], Integer.parseInt(vmDetails[1]), Integer.parseInt(vmDetails[2]), Integer.parseInt(vmDetails[3]));
            vmTypeMap.put(vmDetails[0], vm);
        }
    }

    private static Scanner scanner;
    public static int T = 0;
    public static int K = 0;

    public static void initialize(List<Server> serverTypeList,
                                  List<VM> vmTypeList,
                                  List<List<VMHaveRequest>> vmHaveRequestListCurrent) {


        if (!Main.isMatch) {
            File file = new File(Main.path);
            if (file.isFile() && file.exists()) {
                try {
                    scanner = new Scanner(file);
                } catch (Exception e) {
                    e.printStackTrace();
                    scanner = null;
                }
            } else {
                scanner = null;
            }
        } else {
            scanner = new Scanner(System.in);
        }

        readServer(serverTypeList);
        readVM(vmTypeList);


        String[] T_K = scanner.nextLine().trim().split(" ");
        T = Integer.parseInt(T_K[0]);
        K = Integer.parseInt(T_K[1]);


        for (int i = 0; i < K; i++) {
            List<VMHaveRequest> VMHaveRequestADay = readDay();
            vmHaveRequestListCurrent.add(VMHaveRequestADay);
        }

    }

}
