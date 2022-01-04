package dev.isxander.manhunt.client

import dev.isxander.manhunt.packets.client.receiveManhuntStart
import dev.isxander.manhunt.packets.client.receiveManhuntStop
import dev.isxander.manhunt.packets.client.receiveModCheck
import dev.isxander.manhunt.packets.client.receiveTrophyPos
import net.fabricmc.api.ClientModInitializer

object ManhuntClient : ClientModInitializer {
    override fun onInitializeClient() {
        registerPacketReceivers()
    }

    private fun registerPacketReceivers() {
        receiveTrophyPos()
        receiveManhuntStart()
        receiveManhuntStop()
        receiveModCheck()
    }
}
