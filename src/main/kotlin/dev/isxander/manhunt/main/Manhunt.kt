package dev.isxander.manhunt.main

import dev.isxander.manhunt.ManhuntStopState
import dev.isxander.manhunt.api.GameTypeHandler
import dev.isxander.manhunt.packets.server.receiveBreakTrophy
import dev.isxander.manhunt.packets.server.receiveModConfirm
import dev.isxander.manhunt.packets.server.sendModCheck
import dev.isxander.manhunt.server.ManhuntGame
import io.ejekta.kambrik.command.addCommand
import io.ejekta.kambrik.command.suggestionList
import io.ejekta.kambrik.text.sendFeedback
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents
import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.sound.SoundEvents

object Manhunt : ModInitializer {
    var game: ManhuntGame? = null

    override fun onInitialize() {
        GameTypeHandler.registerListeners()
        registerCommands()
        registerPackets()
    }

    private fun registerCommands() {
        CommandRegistrationCallback.EVENT.register { dispatcher, dedicated ->
            dispatcher.addCommand("manhunt") {
                "start"  {
                    argString("gameType", suggestionList { GameTypeHandler.types.keys.toList() }) { gameType ->
                        argument(EntityArgumentType.player(), "speedrunner") { speedrunnerArg ->
                            argInt("trophyRadius") { trophyRadius ->
                                requires { game?.started == false }
                                runs {
                                    val speedrunner = speedrunnerArg().getPlayer(source)
                                    source.sendFeedback("Starting manhunt, speedrunner is ") { add(speedrunner.name) }
                                    source.player?.playSound(SoundEvents.ITEM_LODESTONE_COMPASS_LOCK, 5f, 1f)

                                    game = ManhuntGame(GameTypeHandler.types[gameType()]!!, source.server, source.world, speedrunner, trophyRadius())
                                    game!!.start()
                                }
                            }
                        }
                    }
                }

                "stop" {
                    requires { game?.started == true }
                    runs {
                        game!!.stop(ManhuntStopState.FORCE_STOP)
                        game = null
                        source.sendFeedback("Stopped manhunt")
                    }
                }
            }
        }
    }

    private fun registerPackets() {
        receiveBreakTrophy()
        receiveModConfirm()
    }
}
