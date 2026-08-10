package com.industrialcrops.crop;

import net.minecraft.util.Mth;

public enum CropQuality {
    NORMAL("normal", 0xFFFFFF),
    GOOD("good", 0x55FF55),
    EXCELLENT("excellent", 0x5555FF),
    FINE("fine", 0xAA00AA),
    SUPREME("supreme", 0xFFAA00),
    SUPER("super", 0xFFFFFF);

    private static final CropQuality[] VALUES = values();
    private final String translationSuffix;
    private final int color;

    CropQuality(String translationSuffix, int color) {
        this.translationSuffix = translationSuffix;
        this.color = color;
    }

    public int tier() {
        return ordinal();
    }

    public String translationKey() {
        return "quality.industrialcrops." + translationSuffix;
    }

    public int color() {
        return color;
    }

    public double yieldMultiplier() {
        return 1.0D + tier() * 0.5D;
    }

    public double growthDurationMultiplier() {
        return 1.0D - tier() * 0.1D;
    }

    public static CropQuality byTier(int tier) {
        return VALUES[Mth.clamp(tier, 0, VALUES.length - 1)];
    }
}
