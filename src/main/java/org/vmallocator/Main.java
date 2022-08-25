package org.vmallocator;

import java.math.BigDecimal;

public class Main {

    public static void main(String[] args) {
        // Example of Azure D3 Compute Type
        var d3vm = new VMType("D3", new BigDecimal("0.0585"), new short[] { 2, 4, 8, 16, 32, 48, 64 });
        // var das5 = new VMType("Das v5", new BigDecimal("0.043"), new short[] { 2, 4,
        // 8, 16, 32, 48, 64, 96 });

        // Input data
        int cpuPerProcess = 2;
        int numberOfProcesses = 16;

        System.out.printf("\nNumber of Processes: %s", numberOfProcesses);
        System.out.printf("\nCPUs per Process: %s", cpuPerProcess);

        var vmAllocator = new VMAllocator.Builder()
                .cpuPerProcess(cpuPerProcess)
                .numberOfProcesses(numberOfProcesses)
                .minimumVMCount(1)
                .cpuOverheadPerVm(1)
                .build();

        var allocations = vmAllocator.allocate(d3vm);

        System.out.println("\n\nAllocation analysis:");

        allocations.stream().map(info -> " - ".concat(info.toString())).forEach(System.out::println);

        // Find best allocation
        System.out.println("\nBest allocation: ");

        var bestAllocation = vmAllocator.findBestAllocation(d3vm);
        bestAllocation.ifPresentOrElse(b -> {
            System.out.printf(" - %s\n", b);
            System.out.printf(" - Cost for %s per hour: $%s\n", d3vm.name(),
                    d3vm.costPerCPU().multiply(BigDecimal.valueOf((long) b.totalBillableCPUs())));
        }, () -> System.out.println("No good allocation found."));

    }

}
