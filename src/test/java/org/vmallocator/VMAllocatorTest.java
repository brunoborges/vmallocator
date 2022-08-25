package org.vmallocator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;

import org.junit.Test;

public class VMAllocatorTest {

    @Test
    public void smallestAmountsEqualsToVMSizes() {
        var vmSizes = new short[] { 2, 4, 8, 16, 32, 48, 64 };
        var d3vm = new VMType("Test", new BigDecimal("0.1"), vmSizes);

        var vmAllocator = new VMAllocator.Builder()
                .cpuPerProcess(1)
                .numberOfProcesses(1)
                .minimumVMCount(1)
                .cpuOverheadPerVm(0)
                .build();

        var allocations = vmAllocator.allocate(d3vm);

        assertEquals(vmSizes.length, allocations.size());
    }

    @Test
    public void testMinimumVMCount() {
        var vmSizes = new short[] { 2, 4, 8, 16, 32, 48, 64 };
        var d3vm = new VMType("Test", new BigDecimal("0.1"), vmSizes);

        var vmAllocator = new VMAllocator.Builder()
                .cpuPerProcess(1)
                .numberOfProcesses(1)
                .minimumVMCount(200)
                .cpuOverheadPerVm(0)
                .build();

        var allocations = vmAllocator.allocate(d3vm);

        for (var allocation : allocations) {
            assertTrue(allocation.numberOfVMs() == 200);
        }
    }

}
