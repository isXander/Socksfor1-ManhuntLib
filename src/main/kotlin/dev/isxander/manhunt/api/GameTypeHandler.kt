package dev.isxander.manhunt.api

import net.fabricmc.loader.api.FabricLoader

object GameTypeHandler {
    val types = mutableMapOf<String, ManhuntGameType>()

    fun registerListeners() {
        FabricLoader.getInstance().getEntrypoints("manhunt", ManhuntGameType::class.java).forEach {
            types[it.id] = it
        }
    }
}
