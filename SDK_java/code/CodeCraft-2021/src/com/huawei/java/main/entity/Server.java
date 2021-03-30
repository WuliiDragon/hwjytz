package com.huawei.java.main.entity;

import java.awt.geom.FlatteningPathIterator;

public class Server implements Comparable<Server> {
    //
    private String typeName;
    private int core;
    private int memory;
    private float valueForMoney;
    private int purchaseCost;
    private int energyConsumptionPerDay;

    public Server() {
    }


    public Server(String typeName, int core, int memory, float valueForMoney, int purchaseCost, int energyConsumptionPerDay) {
        this.typeName = typeName;
        this.core = core;
        this.memory = memory;
        this.valueForMoney = valueForMoney;
        this.purchaseCost = purchaseCost;
        this.energyConsumptionPerDay = energyConsumptionPerDay;
    }

    public Server(String typeName, int core, int memory, int purchaseCost, int energyConsumptionPerDay) {
        this.typeName = typeName;
        this.core = core;
        this.memory = memory;
        this.purchaseCost = purchaseCost;
        this.energyConsumptionPerDay = energyConsumptionPerDay;
    }

    @Override
    public int compareTo(Server o) {

        if (this.core > o.getCore()) {
            return ( o.getCore()-this.core);
        }
        if (this.core < o.getCore()) {
            return (o.getCore()-this.core);
        }
        // 按name排序
        if (this.memory > o.getMemory()) {
            return  o.getMemory()-this.memory ;
        }
        if (this.memory < o.getMemory()) {
            return  o.getMemory()-this.memory ;
        }
        return 0;
    }

    @Override
    public Server clone() throws CloneNotSupportedException {
        return (Server) super.clone();
    }

    public void computeValueForMoney(float vmMemory, float vmCore, int requestTotalDay, int nowDay){
        float coreWeight= vmCore/(vmCore+vmMemory);
        float memWeight=vmMemory/(vmCore+vmMemory);

        float money = (float)this.purchaseCost+(requestTotalDay-nowDay)*energyConsumptionPerDay;
        float valueForMoney=money/this.core*coreWeight+money/this.memory*memWeight;
        this.setValueForMoney(valueForMoney);
    }
//    public void computeValueForMoney(float vmMemory, float vmCore, int requestTotalDay, int nowDay){
//        float coreWeight=vmMemory/(vmCore+vmMemory);
//        float memWeight=vmCore/(vmCore+vmMemory);
//        float money = (float)this.purchaseCost;
//        float valueForMoney=money/this.core*coreWeight+money/this.memory*memWeight;
//        this.setValueForMoney(valueForMoney);
//    }

    public float getValueForMoney() {
        return valueForMoney;
    }

    public void setValueForMoney(float valueForMoney) {
        this.valueForMoney = valueForMoney;
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


    public int getPurchaseCost() {
        return purchaseCost;
    }

    public void setPurchaseCost(int purchaseCost) {
        this.purchaseCost = purchaseCost;
    }

    public int getEnergyConsumptionPerDay() {
        return energyConsumptionPerDay;
    }

    public void setEnergyConsumptionPerDay(int energyConsumptionPerDay) {
        this.energyConsumptionPerDay = energyConsumptionPerDay;
    }


}