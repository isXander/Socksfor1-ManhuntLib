package dev.isxander.manhunt.api

import dev.isxander.manhunt.ManhuntStopState
import dev.isxander.manhunt.server.ManhuntGame

interface ManhuntGameType {
    val id: String

    fun onGameStart(game: ManhuntGame) {}
    fun onTrophyCollected(game: ManhuntGame) {}

    fun onStop(state: ManhuntStopState) {}

    fun provideTrophies(): List<(ManhuntGame) -> Unit>
}
