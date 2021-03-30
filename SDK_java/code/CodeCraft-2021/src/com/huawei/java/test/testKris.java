package com.huawei.java.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.zip.CheckedOutputStream;

public class testKris {


    public static void main(String args[]) throws IOException, ExecutionException, InterruptedException {

        int nThreads = 2;
        int n=2;
        int m=2;
        int[][]mat={{1,2},{3,4}};
        ExecutorService executorService = Executors.newFixedThreadPool(nThreads);
        List<Future<Integer>> tasks = new ArrayList<>(n);
        for (int i = 0; i < n; ++i) {
            int[] vec = mat[i];
            tasks.add(executorService.submit(() -> {
                int sum = 0;
                for (int j = 0; j < m; ++j) {
                    sum += vec[j];
                }
                return sum;
            }));
        }
        int sumMat = 0;
        for (Future<Integer> task : tasks) {
            sumMat += task.get();
        }
        System.out.println(sumMat);
        executorService.shutdown(); // 必需，否则程序无法正常结束

    }


}
