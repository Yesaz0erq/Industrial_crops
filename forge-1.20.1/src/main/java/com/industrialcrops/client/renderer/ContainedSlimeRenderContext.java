package com.industrialcrops.client.renderer;

public final class ContainedSlimeRenderContext {
    private static final ThreadLocal<Boolean> ACTIVE = ThreadLocal.withInitial(() -> false);

    private ContainedSlimeRenderContext() {
    }

    public static boolean isActive() {
        return ACTIVE.get();
    }

    public static void setActive(boolean active) {
        ACTIVE.set(active);
    }
}
