package com.huawei.java.main.entity;

import java.util.LinkedList;
import java.util.List;

/**
 * @Author mkm_xjtu
 * @Date 2021/3/12
 */

public class ServerHavePurchase extends Server {
    private int id;
    private ServerNode serverNodeA;
    private ServerNode serverNodeB;
    private List<VMHaveRequest> holdVM=new LinkedList<>();
    private int haveCost=0;
    private int holdVmNum;
    private float mcRatioSum;

    public ServerHavePurchase() {
    }

    public ServerHavePurchase(Server server, ServerNode serverNodeA, ServerNode serverNodeB) {

        super(server.getTypeName(), server.getCore(), server.getMemory(), server.getPurchaseCost(), server.getEnergyConsumptionPerDay());
        this.serverNodeA = serverNodeA;
        this.serverNodeB = serverNodeB;
        this.haveCost += this.getPurchaseCost();
    }

    public ServerHavePurchase(Server server, int id, ServerNode serverNodeA, ServerNode serverNodeB) {

        super(server.getTypeName(), server.getCore(), server.getMemory(), server.getPurchaseCost(), server.getEnergyConsumptionPerDay());
        this.holdVM = new LinkedList<>();
        this.id = id;
        this.serverNodeA = serverNodeA;
        this.serverNodeB = serverNodeB;
        this.haveCost += this.getPurchaseCost();
    }
//
//    public ServerHavePurchase(Server server, int id, ServerNode serverNodeA, ServerNode serverNodeB) {
//
//        super(server.getTypeName(), server.getCore(), server.getMemory(), server.getValueForMoney(),server.getPurchaseCost(), server.getEnergyConsumptionPerDay());
//        this.holdVM = new LinkedList<>();
//        this.id = id;
//        this.serverNodeA = serverNodeA;
//        this.serverNodeB = serverNodeB;
//    }

    public ServerHavePurchase(String typeName, int core, int memory, float valueForMoney, int purchaseCost, int energyConsumptionPerDay, int id, ServerNode serverNodeA, ServerNode serverNodeB) {
        super(typeName, core, memory, valueForMoney, purchaseCost, energyConsumptionPerDay);
        this.id = id;
        this.serverNodeA = serverNodeA;
        this.serverNodeB = serverNodeB;
        this.haveCost += this.getPurchaseCost();
    }

    public ServerHavePurchase(String typeName, int core, int memory, int purchaseCost, int energyConsumptionPerDay, int id, ServerNode serverNodeA, ServerNode serverNodeB) {
        super(typeName, core, memory, purchaseCost, energyConsumptionPerDay);
        this.id = id;
        this.serverNodeA = serverNodeA;
        this.serverNodeB = serverNodeB;
        this.haveCost += this.getPurchaseCost();
    }
    public ServerHavePurchase(String typeName, int core, int memory, int purchaseCost, int energyConsumptionPerDay ,ServerNode serverNodeA, ServerNode serverNodeB) {
        super(typeName, core, memory, purchaseCost, energyConsumptionPerDay);
        this.serverNodeA = serverNodeA;
        this.serverNodeB = serverNodeB;
        this.haveCost += this.getPurchaseCost();
    }

    public float getMcRatioSum() {
        return mcRatioSum;
    }
    public void setMcRatioSum() {
        this.mcRatioSum=this.serverNodeA.getRemainMemory()+this.serverNodeA.getRemainCore()+
                this.serverNodeB.getRemainMemory()+this.serverNodeB.getRemainCore();
    }

//    public void setMcRatioSum() {
//        List<VMHaveRequest> holdVM = this.getHoldVM();
//        float vmCoreSum=0;
//        float vmMemSum=0;
//        for(VMHaveRequest vm: holdVM){
//            vmCoreSum+=vm.getCore();
//            vmMemSum+=vm.getMemory();
//        }
//        this.mcRatioSum= 2*vmCoreSum*vmMemSum/(vmCoreSum+vmMemSum);
//    }

    public int getHoldVmNum() {
        return holdVmNum;
    }

    public void setHoldVmNum(int holdVmNum) {
        this.holdVmNum = holdVmNum;
    }

    public int getHaveCost() {
        return haveCost;
    }

    public void setHaveCost(int haveCost) {
        this.haveCost = haveCost;
    }

    public List<VMHaveRequest> getHoldVM() {
        return holdVM;
    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ServerNode getServerNodeA() {
        return serverNodeA;
    }

    public void setServerNodeA(ServerNode serverNodeA) {
        this.serverNodeA = serverNodeA;
    }

    public ServerNode getServerNodeB() {
        return serverNodeB;
    }

    public void setServerNodeB(ServerNode serverNodeB) {
        this.serverNodeB = serverNodeB;
    }

    public void addVM(VMHaveRequest vm, int serverNodeId) {
        //双
        if (serverNodeId == 2) {
            this.serverNodeA.subCore(vm.getCore() / 2);
            this.serverNodeB.subCore(vm.getCore() / 2);
            this.serverNodeA.subMem(vm.getMemory() / 2);
            this.serverNodeB.subMem(vm.getMemory() / 2);
            vm.setServerNodeId(2);
        } else if (serverNodeId == 0) {
            //单A
            this.serverNodeA.subCore(vm.getCore());
            this.serverNodeA.subMem(vm.getMemory());
            vm.setServerNodeId(0);
        } else if (serverNodeId == 1) {
            //单B
            this.serverNodeB.subCore(vm.getCore());
            this.serverNodeB.subMem(vm.getMemory());
            vm.setServerNodeId(1);
        }

//        System.err.println(this.getServerNodeA().getRemainCore());
//        System.err.println(this.getServerNodeA().getRemainMemory());
//        System.err.println(this.getServerNodeB().getRemainCore());
//        System.err.println(this.getServerNodeB().getRemainMemory());
        vm.setServerId(this.id);
        this.holdVM.add(vm);

    }

    public void delVM(ServerHavePurchase server, VMHaveRequest vm) {

        if(vm.getServerNodeId()==0){
            int remainCore = server.getServerNodeA().getRemainCore();
            int remainMemory = server.getServerNodeA().getRemainMemory();
            server.getServerNodeA().setRemainMemory(remainMemory+vm.getMemory());
            server.getServerNodeA().setRemainCore(remainCore+vm.getCore());
            server.getHoldVM().remove(vm);
        }
        else if(vm.getServerNodeId()==1){
            int remainCore = server.getServerNodeB().getRemainCore();
            int remainMemory = server.getServerNodeB().getRemainMemory();
            server.getServerNodeB().setRemainMemory(remainMemory+vm.getMemory());
            server.getServerNodeB().setRemainCore(remainCore+vm.getCore());
            server.getHoldVM().remove(vm);
        }else{  //双节点运行
            int remainCoreA = server.getServerNodeA().getRemainCore();
            int remainMemoryA = server.getServerNodeA().getRemainMemory();
            int remainCoreB = server.getServerNodeB().getRemainCore();
            int remainMemoryB = server.getServerNodeB().getRemainMemory();
            server.getServerNodeA().setRemainMemory(remainMemoryA+vm.getMemory()/2);
            server.getServerNodeA().setRemainCore(remainCoreA+vm.getCore()/2);
            server.getServerNodeB().setRemainMemory(remainMemoryB+vm.getMemory()/2);
            server.getServerNodeB().setRemainCore(remainCoreB+vm.getCore()/2);
            server.getHoldVM().remove(vm);
        }
//        vm.setServerId(-1);
//        vm.setServerNodeId(-1);
    }
    public void addCostADay() {
        this.haveCost += this.getEnergyConsumptionPerDay();
    }
}
