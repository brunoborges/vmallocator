package org.helder;

import java.math.BigDecimal;

public record VmType(String name, BigDecimal costPerCPU, int[] sizes) {}
