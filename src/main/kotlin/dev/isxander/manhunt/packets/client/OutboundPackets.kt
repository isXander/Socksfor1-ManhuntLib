package dev.isxander.manhunt.packets.client

import dev.isxander.manhunt.packets.BREAK_TROPHY
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.minecraft.util.math.BlockPos

fun sendBreakTrophy(pos: BlockPos) {
    val pbb = PacketByteBufs.create()
    pbb.writeBlockPos(pos)

    ClientPlayNetworking.send(BREAK_TROPHY, pbb)
}
