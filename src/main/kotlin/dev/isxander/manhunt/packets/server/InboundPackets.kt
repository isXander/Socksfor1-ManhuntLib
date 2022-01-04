package dev.isxander.manhunt.packets.server

import dev.isxander.manhunt.main.Manhunt
import dev.isxander.manhunt.packets.BREAK_TROPHY
import dev.isxander.manhunt.packets.MOD_CONFIRM
import dev.isxander.manhunt.utils.requiredMods
import io.ejekta.kambrik.text.textLiteral
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking

fun receiveBreakTrophy() {
    ServerPlayNetworking.registerGlobalReceiver(BREAK_TROPHY) { server, player, handler, buf, responseSender ->
        val trophyPos = buf.readBlockPos()
        Manhunt.game?.breakTrophy(player, trophyPos)
    }
}

fun receiveModConfirm() {
    ServerPlayNetworking.registerGlobalReceiver(MOD_CONFIRM) { server, player, handler, buf, responseSender ->
        val modIds = buf.readString().split(",")
        if (!modIds.containsAll(requiredMods)) {
            player.networkHandler.disconnect(textLiteral("You do not have all the required mods installed! [${modIds.joinToString(", ")}]"))
        }
    }
}
