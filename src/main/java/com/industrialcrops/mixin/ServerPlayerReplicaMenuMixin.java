package com.industrialcrops.mixin;

import com.industrialcrops.replication.UniversalReplicaData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.OptionalInt;
import java.util.function.Consumer;
import net.minecraft.network.RegistryFriendlyByteBuf;

/** Keeps copied-machine menus synchronized and valid against their virtual state. */
@Mixin(ServerPlayer.class)
public abstract class ServerPlayerReplicaMenuMixin {
    @Inject(
            method = "openMenu(Lnet/minecraft/world/MenuProvider;Ljava/util/function/Consumer;)Ljava/util/OptionalInt;",
            at = @At("RETURN")
    )
    private void industrialcrops$captureReplicaMenu(MenuProvider provider,
            Consumer<RegistryFriendlyByteBuf> extraDataWriter,
            CallbackInfoReturnable<OptionalInt> cir) {
        if (cir.getReturnValue().isPresent()) {
            ServerPlayer player = (ServerPlayer) (Object) this;
            UniversalReplicaData.registerActiveMenu(player.containerMenu);
        }
    }

    @Redirect(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/inventory/AbstractContainerMenu;broadcastChanges()V"
            )
    )
    private void industrialcrops$broadcastReplicaMenu(AbstractContainerMenu menu) {
        UniversalReplicaData.broadcastMenuChanges(menu);
    }

    @Redirect(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/inventory/AbstractContainerMenu;stillValid(Lnet/minecraft/world/entity/player/Player;)Z"
            )
    )
    private boolean industrialcrops$validateReplicaMenu(AbstractContainerMenu menu, Player player) {
        return UniversalReplicaData.checkMenuValidity(menu, player);
    }
}
