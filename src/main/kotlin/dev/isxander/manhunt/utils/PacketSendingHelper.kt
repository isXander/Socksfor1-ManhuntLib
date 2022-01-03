package dev.isxander.manhunt.utils

import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld

fun sendPacketToAllPlayers(world: ServerWorld, lambda: (ServerPlayerEntity) -> Unit) {
    world.players.forEach(lambda::invoke)
}
