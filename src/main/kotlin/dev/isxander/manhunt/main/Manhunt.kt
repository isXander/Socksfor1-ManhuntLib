package dev.isxander.manhunt.main

import dev.isxander.manhunt.ManhuntStopState
import dev.isxander.manhunt.api.GameTypeHandler
import dev.isxander.manhunt.packets.server.receiveBreakTrophy
import dev.isxander.manhunt.server.ManhuntGame
import io.ejekta.kambrik.command.addCommand
import io.ejekta.kambrik.command.suggestionList
import io.ejekta.kambrik.text.sendFeedback
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.sound.SoundEvents

object Manhunt : ModInitializer {
    var game: ManhuntGame? = null

    override fun onInitialize() {
        GameTypeHandler.registerListeners()
        registerCommands()
        registerC2SPackets()
    }

    private fun registerCommands() {
        CommandRegistrationCallback.EVENT.register { dispatcher, dedicated ->
            dispatcher.addCommand("manhunt") {
                "start" {
                    argString("gameType", suggestionList { GameTypeHandler.types.keys.toList() }) { gameType ->
                        argument(EntityArgumentType.player(), "speedrunner") { speedrunnerArg ->
                            argInt("trophyRadius") { trophyRadius ->
                                runs {
                                    try {
                                        requires { game?.started == false }

                                        val speedrunner = speedrunnerArg().getPlayer(source)
                                        source.sendFeedback("Starting manhunt, speedrunner is ") { add(speedrunner.name) }
                                        source.player?.playSound(SoundEvents.ITEM_LODESTONE_COMPASS_LOCK, 5f, 1f)

                                        game = ManhuntGame(GameTypeHandler.types[gameType()]!!, source.world, speedrunner, trophyRadius())
                                        game!!.start()
                                    } catch (e: Exception) {
                                        source.sendFeedback("Failed to start manhunt")
                                        e.printStackTrace()
                                    }
                                }
                            }
                        }
                    }
                }

                "stop" runs {
                    requires { game?.started == true }

                    game!!.stop(ManhuntStopState.FORCE_STOP)
                    source.sendFeedback("Stopped manhunt")
                }
            }
        }
    }

    private fun registerC2SPackets() {
        receiveBreakTrophy()
    }
}
