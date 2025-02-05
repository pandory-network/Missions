package com.kryeit.mixin.create;

import com.kryeit.MinecraftServerSupplier;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import static com.kryeit.Missions.cachedTrainPlayerPositions;

@Mixin(value = AbstractContraptionEntity.class)
public class AbstractContraptionEntityMixin {

    @Inject(method = "getDismountLocationForPassenger", at = @At("HEAD"))
    private void onDismount(LivingEntity entityLiving, CallbackInfoReturnable<Vec3> cir) {
        if (entityLiving instanceof ServerPlayer user) {
            if (cachedTrainPlayerPositions.isEmpty() && MinecraftServerSupplier.getServer() != null) return;

            Iterator<Map.Entry<UUID, Vec3>> iterator = cachedTrainPlayerPositions.entrySet().iterator();

            while (iterator.hasNext()) {
                Map.Entry<UUID, Vec3> entry = iterator.next();
                UUID player = entry.getKey();

                if (player.equals(user.getUUID())) {
                    iterator.remove();
                }
            }
        }
    }
}
