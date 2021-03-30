package com.huawei.java.main.entity;

/**
 * @Author mkm_xjtu
 * @Date 2021/3/12
 */
public class VMHaveRequest extends VM {
    private int id;
    private int serverId;

    //0A 1B 2AB
    private int serverNodeId;
    private int addOrDel; // 1: add; 0: delete

    public VMHaveRequest(int id, String typeName, int core, int memory, int deployType, int addOrDel) {
        super(typeName, core, memory, deployType);
        this.id = id;
        this.serverId = serverId;
        this.serverNodeId = serverNodeId;
        this.addOrDel = addOrDel;
    }

    public VMHaveRequest() {
    }

    public VMHaveRequest(int id, VM vm, int addOrDel) {
        super(vm.getTypeName(), vm.getCore(), vm.getMemory(), vm.isDeployType());
        this.id = id;
        this.addOrDel = addOrDel;


    }

    public VMHaveRequest(String typeName, int core, int memory, int deployType, int id, int serverId, int serverNodeId) {
        super(typeName, core, memory, deployType);
        this.id = id;
        this.serverId = serverId;
        this.serverNodeId = serverNodeId;
    }

    public int getAddOrDel() {
        return addOrDel;
    }

    public void setAddOrDel(int addOrDel) {
        this.addOrDel = addOrDel;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public int getServerNodeId() {
        return serverNodeId;
    }

    public void setServerNodeId(int serverNodeId) {
        this.serverNodeId = serverNodeId;
    }
}
