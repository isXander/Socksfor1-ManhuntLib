package dev.isxander.manhunt.mixins;

import dev.isxander.manhunt.client.ManhuntGameClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(targets = "net/minecraft/client/item/ModelPredicateProviderRegistry$2")
public abstract class MixinModelPredicateProviderRegistry {
    @ModifyVariable(method = "unclampedCall", at = @At(value = "STORE"), ordinal = 0)
    public BlockPos modifyCompasPos(BlockPos pos, ItemStack itemStack, @Nullable ClientWorld clientWorld, @Nullable LivingEntity livingEntity, int i) {
        Entity entity = livingEntity != null ? livingEntity : itemStack.getHolder();
        if (entity == null) return null;

        boolean inGame = ManhuntGameClient.INSTANCE.getStarted();

        if (inGame) return ManhuntGameClient.INSTANCE.getTrophyPos();
        return pos;
    }
}
