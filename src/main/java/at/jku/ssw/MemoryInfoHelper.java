package at.jku.ssw;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;

public class MemoryInfoHelper {

    public static void performGCAndPrintInfo(String state) {
        System.gc();

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.gc();

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        printMemoryInfo(state);
    }

    public static void printMemoryInfo(String state) {
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage memHeap = memoryMXBean.getHeapMemoryUsage();
        long initial = memHeap.getUsed();

        if (state.isEmpty()) {
            System.out.printf("%.3f MB%n", initial / 1024.0 / 1024.0);
        } else {
            System.out.printf(state + ": %.3f MB%n", initial / 1024.0 / 1024.0);
        }
    }

}
