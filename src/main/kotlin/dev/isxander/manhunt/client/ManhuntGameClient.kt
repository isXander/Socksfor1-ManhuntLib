package dev.isxander.manhunt.client

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.BlockPos

object ManhuntGameClient {
    var started: Boolean = false
    var speedrunnerPos: BlockPos? = null
    var trophyPos: BlockPos? = null
}
