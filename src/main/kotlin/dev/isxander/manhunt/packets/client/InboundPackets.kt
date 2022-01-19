package dev.isxander.manhunt.packets.client

import dev.isxander.manhunt.client.ManhuntGameClient
import dev.isxander.manhunt.packets.MANHUNT_START
import dev.isxander.manhunt.packets.MANHUNT_STOP
import dev.isxander.manhunt.packets.MOD_CHECK
import dev.isxander.manhunt.packets.NEW_TROPHY_POS
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.loader.api.FabricLoader

fun receiveManhuntStart() {
    ClientPlayNetworking.registerGlobalReceiver(MANHUNT_START) { client, handler, buf, responseSender ->
        ManhuntGameClient.started = true
        ManhuntGameClient.speedrunnerPos = buf.readBlockPos()
    }
}

fun receiveManhuntStop() {
    ClientPlayNetworking.registerGlobalReceiver(MANHUNT_STOP) { client, handler, buf, responseSender ->
        ManhuntGameClient.started = false
        ManhuntGameClient.speedrunnerPos = null
    }
}

fun receiveTrophyPos() {
    ClientPlayNetworking.registerGlobalReceiver(NEW_TROPHY_POS) { client, handler, buf, responseSender ->
        val trophyPos = buf.readBlockPos()

        println("New Trophy Location: $trophyPos")
        ManhuntGameClient.trophyPos = trophyPos
    }
}

fun receiveModCheck() {
    ClientPlayNetworking.registerGlobalReceiver(MOD_CHECK) { client, handler, buf, responseSender ->
        sendModConfirm(FabricLoader.getInstance().allMods.map { it.metadata.id })
    }
}
