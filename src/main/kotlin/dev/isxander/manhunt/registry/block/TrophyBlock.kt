package dev.isxander.manhunt.registry.block

import dev.isxander.manhunt.packets.client.sendBreakTrophy
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.MapColor
import net.minecraft.block.Material
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.sound.BlockSoundGroup
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class TrophyBlock : Block(FabricBlockSettings.of(Material.METAL, MapColor.GOLD).dropsNothing().strength(3.0F, 6.0F).sounds(BlockSoundGroup.METAL)) {
    override fun onBreak(world: World, pos: BlockPos, state: BlockState, player: PlayerEntity) {
        super.onBreak(world, pos, state, player)
        if (world.isClient) {
            sendBreakTrophy(pos)
        }
    }
}
