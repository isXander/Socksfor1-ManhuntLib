package dev.isxander.manhunt.registry

import dev.isxander.manhunt.registry.block.TrophyBlock
import io.ejekta.kambrik.registration.KambrikAutoRegistrar

object ManhuntRegistry : KambrikAutoRegistrar {
    val TROPHY_BLOCK = "trophy" forBlock TrophyBlock()
}
