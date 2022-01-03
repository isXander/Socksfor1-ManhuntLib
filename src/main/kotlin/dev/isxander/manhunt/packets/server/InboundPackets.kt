package dev.isxander.manhunt.packets.server

import dev.isxander.manhunt.main.Manhunt
import dev.isxander.manhunt.packets.BREAK_TROPHY
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking

fun receiveBreakTrophy() {
    ServerPlayNetworking.registerGlobalReceiver(BREAK_TROPHY) { server, player, handler, buf, responseSender ->
        val trophyPos = buf.readBlockPos()
        Manhunt.game?.breakTrophy(player, trophyPos)
    }
}
