package com.huawei.java.main.entity;

/**
 * @Author mkm_xjtu
 * @Date 2021/3/11
 */

public class VM implements Comparable<VM> {
    private String typeName;
    private int core;
    private int memory;


    // 是否双节点部署用 0 和 1 表示， 0 表示单节点部署， 1 表示双节点部署。
    private int deployType;


    public VM() {
    }

    public VM(String typeName, int core, int memory, int deployType) {
        this.typeName = typeName;
        this.core = core;
        this.memory = memory;
        this.deployType = deployType;
    }

    @Override
    public int compareTo(VM o) {
        return this.typeName.compareTo(o.getTypeName());
    }

    public int getDeployType() {
        return deployType;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public int getCore() {
        return core;
    }

    public void setCore(int core) {
        this.core = core;
    }

    public int getMemory() {
        return memory;
    }

    public void setMemory(int memory) {
        this.memory = memory;
    }

    public int isDeployType() {
        return deployType;
    }

    public void setDeployType(int deployType) {
        this.deployType = deployType;
    }
}
