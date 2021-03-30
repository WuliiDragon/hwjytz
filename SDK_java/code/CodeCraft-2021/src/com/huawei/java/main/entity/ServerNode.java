package com.huawei.java.main.entity;

/**
 * @Author mkm_xjtu
 * @Date 2021/3/11
 */
public class ServerNode {


    private int serverId;
    private int id;  // 0-A;1-B;
    private int remainCore;
    private int remainMemory;
    private VMHaveRequest vm;


    public ServerNode() {
    }


    public ServerNode(int id, int serverId, int remainCore, int remainMemory) {
        this.id = id;
        this.serverId = serverId;
        this.remainCore = remainCore;
        this.remainMemory = remainMemory;
        this.vm = vm;
    }
    public ServerNode(int id, int remainCore, int remainMemory) {
        this.id = id;
        this.remainCore = remainCore;
        this.remainMemory = remainMemory;
        this.vm = vm;
    }


    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRemainCore() {
        return remainCore;
    }

    public void setRemainCore(int remainCore) {
        this.remainCore = remainCore;
    }

    public int getRemainMemory() {
        return remainMemory;
    }

    public void subMem(int t) {
        this.remainMemory -= t;

    }

    public void addMem(int t) {
        this.remainMemory += t;
    }

    public void subCore(int t) {
        this.remainCore -= t;
    }

    public void addCore(int t) {
        this.remainCore += t;
    }


    public void setRemainMemory(int remainMemory) {
        this.remainMemory = remainMemory;
    }

    public VMHaveRequest getVm() {
        return vm;
    }

    public void setVm(VMHaveRequest vm) {
        this.vm = vm;
    }

}
