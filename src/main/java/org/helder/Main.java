package org.helder;

import java.math.BigDecimal;

public class Main {

    public static void main(String[] args) {
        // Example of Azure D3 Compute Type
        var d3vm = new VmType("D3", new BigDecimal("0.0585"), new int[] { 2, 4, 8, 16, 32, 48, 64 });

        // Input data
        int cpuPerProcess = 2;
        int numberOfProcesses = 16;

        System.out.printf("\nNumber of Processes: %s", numberOfProcesses);
        System.out.printf("\nCPUs per Process: %s", cpuPerProcess);

        var vmAllocator = new VmAllocator.Builder()
                .cpuPerProcess(cpuPerProcess)
                .numberOfProcesses(numberOfProcesses)
                .minimumVMCount(1)
                .cpuOverheadPerVm(1)
                .build();
        var allocations = vmAllocator.allocate(d3vm);

        System.out.println("\n\nAllocation analysis:");
        allocations.forEach(System.out::println);

        // Find best allocation by waste rate
        System.out.println("\nBest allocation: ");

        vmAllocator.findBestAllocation(d3vm).ifPresentOrElse(System.out::println,
                () -> System.out.println("No good allocation found."));
    }

}
