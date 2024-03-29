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
    /**
     * Modifies the compass pointer location to the trophy.
     */
    @ModifyVariable(method = "unclampedCall", at = @At(value = "STORE"), ordinal = 0)
    public BlockPos modifyCompassPos(BlockPos pos, ItemStack itemStack, @Nullable ClientWorld clientWorld, @Nullable LivingEntity livingEntity, int i) {
        Entity entity = livingEntity != null ? livingEntity : itemStack.getHolder();
        if (entity == null) return null;

        ManhuntGameClient manhunt = ManhuntGameClient.INSTANCE;

        boolean inGame = manhunt.getStarted();

        if (inGame) {
            if (manhunt.getTrophyPos() != null)
                return manhunt.getTrophyPos();
            else if (manhunt.getSpeedrunnerPos() != null)
                return manhunt.getSpeedrunnerPos();
        }
        return pos;
    }
}
