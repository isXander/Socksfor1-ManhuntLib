package dev.isxander.manhunt.packets.client

import dev.isxander.manhunt.client.ManhuntGameClient
import dev.isxander.manhunt.packets.MANHUNT_START
import dev.isxander.manhunt.packets.MANHUNT_STOP
import dev.isxander.manhunt.packets.NEW_TROPHY_POS
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking

fun receiveManhuntStart() {
    ClientPlayNetworking.registerGlobalReceiver(MANHUNT_START) { client, handler, buf, responseSender ->
        ManhuntGameClient.started = true
    }
}

fun receiveManhuntStop() {
    ClientPlayNetworking.registerGlobalReceiver(MANHUNT_STOP) { client, handler, buf, responseSender ->
        ManhuntGameClient.started = false
    }
}

fun receiveTrophyPos() {
    ClientPlayNetworking.registerGlobalReceiver(NEW_TROPHY_POS) { client, handler, buf, responseSender ->
        val trophyPos = buf.readBlockPos()

        println("New Trophy Location: $trophyPos")
        ManhuntGameClient.trophyPos = trophyPos
    }
}
