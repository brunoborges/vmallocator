package org.vmallocator;

import java.math.BigDecimal;

public record AllocationInfo(
                int vmSize,
                int numberOfVMs,
                double allocatableCPUsPerVM,
                double requestedCPUsPerVM,
                double totalIdleCPUs,
                int totalBillableCPUs,
                BigDecimal totalCostPerHour) {
}
