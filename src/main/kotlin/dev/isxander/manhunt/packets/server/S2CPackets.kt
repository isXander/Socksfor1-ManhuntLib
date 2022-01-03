package dev.isxander.manhunt.packets.server

import dev.isxander.manhunt.ManhuntStopState
import dev.isxander.manhunt.packets.*
import io.ejekta.kambrik.ext.packets.writeEnum
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.BlockPos

fun sendStartState(player: ServerPlayerEntity) {
    ServerPlayNetworking.send(player, MANHUNT_START, PacketByteBufs.empty())
}

fun sendStopState(player: ServerPlayerEntity) {
    ServerPlayNetworking.send(player, MANHUNT_STOP, PacketByteBufs.empty())
}

fun sendTrophyPos(player: ServerPlayerEntity, pos: BlockPos) {
    val pbb = PacketByteBufs.create()
    pbb.writeBlockPos(pos)

    ServerPlayNetworking.send(player, NEW_TROPHY_POS, pbb)
}
