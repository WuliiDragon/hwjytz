package com.huawei.java.main.decision;

import com.huawei.java.main.entity.VMHaveRequest;

import java.util.List;

/**
 * @author Lofit .LaeL
 * @email GreatLandmark@outlook.com
 * @date 2021/3/14
 */
public class DataStatistics {
    /**
     * @param vmList           输入参数
     * @param singleNodeVmList 输出参数
     * @param doubleNodeVmList 输出参数
     */
    public static void divideSingleOrDoubleNodeVm(List<VMHaveRequest> vmList, List<VMHaveRequest> singleNodeVmList, List<VMHaveRequest> doubleNodeVmList) {
        vmList.forEach(vm -> {
            // 是否双节点部署用 0 和 1 表示， 0 表示单节点部署， 1 表示双节点部署。
            if (vm.isDeployType() == 0) {
                singleNodeVmList.add(vm);
            } else {
                doubleNodeVmList.add(vm);
            }
        });
    }
}
