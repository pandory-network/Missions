package com.kryeit.event;

import com.kryeit.missions.MissionManager;
import com.kryeit.missions.mission_types.vanilla.EatMission;
import io.github.fabricators_of_create.porting_lib.entity.events.LivingEntityUseItemEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class EatHandler implements LivingEntityUseItemEvents.LivingUseItemFinish {

    @Override
    public @Nullable ItemStack onUseItem(LivingEntity entity, ItemStack itemStack, int duration, ItemStack used) {
        if(entity instanceof ServerPlayer player) {
            ResourceLocation item = BuiltInRegistries.ITEM.getKey(itemStack.getItem());
            MissionManager.incrementMission(player.getUUID(), EatMission.class, item, 1);
        }
        return null;
    }
}
