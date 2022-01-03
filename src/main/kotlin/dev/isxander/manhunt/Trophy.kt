package dev.isxander.manhunt

import dev.isxander.manhunt.server.ManhuntGame
import net.minecraft.util.math.BlockPos

class Trophy(val onAchieved: (ManhuntGame) -> Unit, val blockPos: BlockPos)
