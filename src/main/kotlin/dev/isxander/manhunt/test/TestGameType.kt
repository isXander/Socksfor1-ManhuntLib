package dev.isxander.manhunt.test

import dev.isxander.manhunt.ManhuntStopState
import dev.isxander.manhunt.api.ManhuntGameType
import dev.isxander.manhunt.server.ManhuntGame

object TestGameType : ManhuntGameType {
    override val id: String = "test"

    override fun onGameStart(game: ManhuntGame) = println("Game Started!")
    override fun onTrophyCollected(game: ManhuntGame) = println("Trophy Collected!")
    override fun onStop(state: ManhuntStopState) = println("Stopped: $state")
    override fun provideTrophies(): List<(ManhuntGame) -> Unit> = listOf(
        { println("Test trophy 1") },
        { println("Test trophy 2") }
    )
}
