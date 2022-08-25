package org.vmallocator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class VMAllocator {

    private Map<VMType, List<AllocationInfo>> allocationsCache = new HashMap<>();
    private Builder config;

    private VMAllocator(Builder data) {
        this.config = data;
    }

    public List<AllocationInfo> allocate(VMType vmType) {
        if (vmType == null) {
            throw new IllegalArgumentException("vmType cannot be null");
        }

        if (allocationsCache.containsKey(vmType)) {
            return allocationsCache.get(vmType);
        }

        var vmSizes = vmType.sizes();
        var allocations = new ArrayList<AllocationInfo>();

        for (int vmSize : vmSizes) {
            // Skip sizes that can't hold the number of CPUs per processor plus CPU overhead
            if (vmSize < config.cpuPerProcess + config.cpuOverheadPerVm) {
                continue;
            }

            // Find how many CPUs are available for allocation per VM
            int allocatableCPUsPerVM = vmSize - config.cpuOverheadPerVm;

            // Find how many CPUs per VM are needed to allocate as many processes as
            // possible.
            // If the number of processes is not a multiple of the number of CPUs per VM,
            // then the number of CPUs per VM will be rounded down to the closest integer.
            // This results in "wasted" CPUs that are not allocated to any process. More
            // below.
            int requestedCPUsPerVM = (int) Math.floor((double) allocatableCPUsPerVM / config.cpuPerProcess)
                    * config.cpuPerProcess;

            // Find how many CPUs will actually be consumed by all processes
            int totalConsumedCPUs = config.numberOfProcesses * config.cpuPerProcess;

            // Now that we know how many CPUs per VM are needed, we can calculate how many
            // VMs are needed. We must round up to the closest integer to ensure that we
            // have enough VMs to allocate all processes.
            int numberOfVMs = (int) Math.ceil(
                    Math.ceil((double) requestedCPUsPerVM / allocatableCPUsPerVM)
                            * (double) totalConsumedCPUs / requestedCPUsPerVM);

            // If the number of VMs is less than the minimum, set it to the minimum
            numberOfVMs = (int) Math.max(config.minimumVMCount, numberOfVMs);

            // But no matter what, the total amount of CPUs across all VMs will still be
            // billable
            int totalBillableCPUs = numberOfVMs * vmSize;

            // Then we can calculate how many CPUs will always be idle
            int totalIdleCPUs = totalBillableCPUs - totalConsumedCPUs;

            var totalCostPerHour = vmType.costPerCPU().multiply(BigDecimal.valueOf(totalBillableCPUs));

            // Add the allocation info to the list
            allocations.add(
                    new AllocationInfo(vmSize, numberOfVMs, allocatableCPUsPerVM, requestedCPUsPerVM, totalIdleCPUs,
                            totalBillableCPUs, totalCostPerHour));
        }

        allocationsCache.put(vmType, allocations);

        return allocations;
    }

    public Optional<AllocationInfo> findBestAllocation(VMType vmType) {
        var allocations = allocate(vmType);
        var comparator = Comparator.comparingDouble(AllocationInfo::totalBillableCPUs)
                .thenComparingInt(AllocationInfo::vmSize);

        // Find one with minimal amount of billable CPUs and the smallest VM size, for best resiliency
        return allocations.stream().min(comparator);
    }

    // Builder pattern
    static final class Builder {
        private int cpuPerProcess;
        private int numberOfProcesses;
        private int minimumVMCount = 1;
        private int cpuOverheadPerVm = 0;

        public Builder cpuPerProcess(int cpuPerProcess) {
            if (cpuPerProcess <= 0) {
                throw new IllegalArgumentException("cpuPerProcess must be greater than 0");
            }
            this.cpuPerProcess = cpuPerProcess;
            return this;
        }

        public Builder numberOfProcesses(int numberOfProcesses) {
            if (numberOfProcesses <= 0) {
                throw new IllegalArgumentException("numberOfProcesses must be greater than 0");
            }
            this.numberOfProcesses = numberOfProcesses;
            return this;
        }

        public Builder minimumVMCount(int minimumVMCount) {
            if (minimumVMCount <= 0) {
                throw new IllegalArgumentException("minimumVMCount must be greater than 0");
            }
            this.minimumVMCount = minimumVMCount;
            return this;
        }

        public Builder cpuOverheadPerVm(int cpuOverheadPerVm) {
            if (cpuOverheadPerVm < 0) {
                throw new IllegalArgumentException("cpuOverheadPerVm cannot be negative");
            }
            this.cpuOverheadPerVm = cpuOverheadPerVm;
            return this;
        }

        public VMAllocator build() {
            return new VMAllocator(this);
        }
    }

}
