package com.huawei.java.main.util;

import com.huawei.java.main.entity.VM;
import com.huawei.java.main.entity.VMHaveRequest;

import java.util.List;

/**
 * @Author mkm_xjtu
 * @Date 2021/3/12
 */
public class VMBinarySearch {
    public static VM vmBinarySearch(List<VM> vmList, int low, int high, String key) {
        if (key.compareTo(vmList.get(low).getTypeName()) <= 0
                || key.compareTo(vmList.get(high).getTypeName()) > 0
                || low > high) {
            return null;
        }
        while (low <= high) {
            int middle = (low + high) / 2;
            if (vmList.get(middle).getTypeName().compareTo(key) > 0) {
                //比关键字大则关键字在左区域
                high = middle - 1;
            } else if (vmList.get(middle).getTypeName().compareTo(key) < 0) {
                //比关键字小则关键字在右区域
                low = middle + 1;
            } else {
                return vmList.get(middle);
            }
        }
        return null;
    }

    /**
     * @param vmHaveRequestListAllDay：今天前所有请求的虚拟机
     * @param vmHaveRequestListEveryday:今天请求的虚拟机
     * @param id：虚拟机id
     * @return
     */
    public static VMHaveRequest vmSearchByIdForDel(List<List<VMHaveRequest>> vmHaveRequestListAllDay,
                                                   List<VMHaveRequest> vmHaveRequestListEveryday, Integer id) {
        for (List<VMHaveRequest> vmListEveryday : vmHaveRequestListAllDay) {
            VMHaveRequest vmHaveRequest = vmListEveryday.stream()
                    .filter(vm -> (id.equals(vm.getId())))
                    .findAny()
                    .orElse(null);
            // 如果能查到要删除的vm，且虚拟机是以前添加进来的
            if (vmHaveRequest != null && vmHaveRequest.getAddOrDel() == 1) {
                return vmHaveRequest;
            }
        }

        VMHaveRequest vmHaveRequest = vmHaveRequestListEveryday.stream()
                .filter(vm -> (id.equals(vm.getId())))
                .findAny()
                .orElse(null);
        if (vmHaveRequest.getAddOrDel() == 1) {
            return vmHaveRequest;
        }
        return null;
    }


}
