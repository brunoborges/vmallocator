package org.helder;

public record AllocationInfo(
        int vmSize,
        int allocatableCPUs,
        int requestedCPUsPerVm,
        int idleCPUs,
        int numberOfVms,
        int billableCPUs,
        double wasteRate) {
}
