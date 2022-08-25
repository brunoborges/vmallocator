package org.vmallocator;

import java.math.BigDecimal;

public record VMType(String name, BigDecimal costPerCPU, short[] sizes) {}
