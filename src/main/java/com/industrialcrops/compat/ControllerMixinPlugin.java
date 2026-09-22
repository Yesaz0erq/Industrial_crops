package com.industrialcrops.compat;

import java.util.List;
import java.util.Set;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.service.MixinService;

/** Select the adapter without loading an optional mod before Mixin transforms it. */
public final class ControllerMixinPlugin implements IMixinConfigPlugin {
    @Override public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        boolean staticAdapter = mixinClassName.endsWith(".SimpleSorterStaticControllerMixin");
        if (!staticAdapter && !mixinClassName.endsWith(".SimpleSorterControllerMixin")) return true;
        try {
            ClassNode target = MixinService.getService().getBytecodeProvider().getClassNode(targetClassName);
            return target.methods.stream()
                    .filter(method -> method.name.equals("requestSort") && method.desc.equals("()V"))
                    .anyMatch(method -> ((method.access & Opcodes.ACC_STATIC) != 0) == staticAdapter);
        } catch (ClassNotFoundException | java.io.IOException missing) {
            return false;
        }
    }

    @Override public void onLoad(String mixinPackage) {}
    @Override public String getRefMapperConfig() { return null; }
    @Override public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}
    @Override public List<String> getMixins() { return null; }
    @Override public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
    @Override public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}
