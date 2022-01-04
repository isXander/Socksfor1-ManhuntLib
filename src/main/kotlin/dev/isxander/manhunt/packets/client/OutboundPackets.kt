package dev.isxander.manhunt.packets.client

import dev.isxander.manhunt.packets.BREAK_TROPHY
import dev.isxander.manhunt.packets.MOD_CONFIRM
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.minecraft.util.math.BlockPos

fun sendBreakTrophy(pos: BlockPos) {
    val pbb = PacketByteBufs.create()
    pbb.writeBlockPos(pos)

    ClientPlayNetworking.send(BREAK_TROPHY, pbb)
}

fun sendModConfirm(mods: List<String>) {
    val pbb = PacketByteBufs.create()
    pbb.writeString(mods.joinToString(","))

    ClientPlayNetworking.send(MOD_CONFIRM, pbb)
}
