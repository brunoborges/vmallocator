package org.vmallocator;

import java.math.BigDecimal;

public record AllocationInfo(
                int vmSize,
                int numberOfVMs,
                int allocatableCPUsPerVM,
                int requestedCPUsPerVM,
                int totalIdleCPUs,
                int totalBillableCPUs,
                BigDecimal totalCostPerHour) {
}
