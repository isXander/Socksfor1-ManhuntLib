package dev.isxander.manhunt.server

import dev.isxander.manhunt.ManhuntStopState
import dev.isxander.manhunt.Trophy
import dev.isxander.manhunt.api.ManhuntGameType
import dev.isxander.manhunt.packets.server.sendStartState
import dev.isxander.manhunt.packets.server.sendStopState
import dev.isxander.manhunt.packets.server.sendTrophyPos
import dev.isxander.manhunt.registry.ManhuntRegistry
import dev.isxander.manhunt.utils.sendPacketToAllPlayers
import io.ejekta.kambrik.text.KambrikTextBuilder
import io.ejekta.kambrik.text.textLiteral
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.block.Blocks
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.network.MessageType
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.LiteralText
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Util
import net.minecraft.util.math.BlockPos
import net.minecraft.world.Heightmap
import net.minecraft.world.World
import java.util.*
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class ManhuntGame(
    val gameType: ManhuntGameType,
    val server: MinecraftServer,
    val world: ServerWorld,
    val speedrunnerUuid: UUID,
    val trophyRadius: Int
) {
    val speedrunner: ServerPlayerEntity
        get() = server.playerManager.getPlayer(speedrunnerUuid)!!

    val trophies = mutableListOf<Trophy>()

    var currentTrophyIndex: Int = 0
        private set

    val currentTrophyGoal: Trophy?
        get() = trophies.getOrNull(currentTrophyIndex)

    var started = false
        private set

    fun start() {
        check(!started) { "Instance already started!" }

        registerTrophies()
        readyPlayers()

        val trophy = trophies[currentTrophyIndex]
        world.setBlockState(trophy.blockPos, ManhuntRegistry.TROPHY_BLOCK.defaultState)
        sendTrophyPos(speedrunner, trophy.blockPos)

        registerEvents()

        gameType.onGameStart(this)
        sendPacketToAllPlayers(world) { sendStartState(it, speedrunner) }

        started = true
    }

    private fun registerTrophies() {
        val centerX = speedrunner.blockX
        val centerZ = speedrunner.blockZ

        val gameTrophies = gameType.provideTrophies()
        while (trophies.size < gameTrophies.size) {
            val random = world.random
            val randomRadius = trophyRadius * sqrt(random.nextDouble())
            val theta = random.nextDouble() * 2 * Math.PI

            val trophyX = centerX + randomRadius * cos(theta)
            val trophyZ = centerZ + randomRadius * sin(theta)

            val trophyY: Int = if (world.registryKey == World.NETHER) {
                val minY = 32 // above lava sea
                var maxY = minY
                var finalHeight = 0
                for (height in minY until 319) { // find bedrock level (done for if mods have modified generation)
                    if (world.getBlockState(
                            BlockPos(
                                trophyX,
                                height.toDouble(),
                                trophyZ
                            )
                        ).block == Blocks.BEDROCK
                    ) break
                    maxY = height
                }
                var mostAirBlocksAbove = 0
                // Optimal position = position with most air blocks above -> lowers chance of it being in a random cave
                for (height in minY until maxY) {
                    if (world.getBlockState(
                            BlockPos(
                                trophyX,
                                height.toDouble() - 1,
                                trophyZ
                            )
                        ).isSolidBlock(
                            world, BlockPos(
                                trophyX,
                                height.toDouble() - 1,
                                trophyZ
                            )
                        )
                    ) {
                        var airBlocksAbove = 0
                        for (h in height until maxY) {
                            if (world.getBlockState(
                                    BlockPos(
                                        trophyX,
                                        h.toDouble(),
                                        trophyZ
                                    )
                                ).isAir
                            ) airBlocksAbove++
                            else break
                        }
                        if (airBlocksAbove > mostAirBlocksAbove) {
                            finalHeight = height
                            mostAirBlocksAbove = airBlocksAbove
                        }
                    }
                }
                // if no good spot is found, try again
                if (finalHeight == 0 || mostAirBlocksAbove < 3) continue
                finalHeight
            } else {
                world.getTopY(Heightmap.Type.WORLD_SURFACE, trophyX.toInt(), trophyZ.toInt())
            }
            val trophyPos = BlockPos(trophyX, trophyY.toDouble(), trophyZ)

            println("Registering trophy at $trophyPos")
            trophies.add(Trophy(gameTrophies[trophies.size], trophyPos))
        }
    }

    private fun readyPlayers() {
        for (player in world.players) {
            if (player == speedrunner) {
                player.sendMessage(prefixed {
                    add("You are the speedrunner! You have ${trophies.size} ${if (trophies.size > 1) "trophies" else "trophy"} to collect! Follow the compass to find them!" {
                        format(Formatting.RED)
                    })
                }, MessageType.CHAT, Util.NIL_UUID)
            } else {
                player.sendMessage(prefixed {
                    add("You are a hunter! Hunt the speedrunner, " {
                        format(Formatting.GREEN)
                    })
                    add(speedrunner.displayName)
                }, MessageType.CHAT, Util.NIL_UUID)
            }
            player.giveItemStack(ItemStack(Items.COMPASS))
        }
    }

    fun breakTrophy(playerEntity: ServerPlayerEntity, pos: BlockPos) {
        if (pos !in trophies.map { it.blockPos }) {
            println("Deleting unknown trophy block!")
            world.setBlockState(pos, Blocks.AIR.defaultState)
            return
        }

        if (pos != currentTrophyGoal?.blockPos || playerEntity != speedrunner) {
            world.setBlockState(pos, ManhuntRegistry.TROPHY_BLOCK.defaultState)
            return
        }

        world.setBlockState(pos, Blocks.AIR.defaultState)
        currentTrophyGoal!!.onAchieved.invoke(this)
        gameType.onTrophyCollected(this)
        server.playerManager.broadcast(prefixed {
            add(speedrunner.displayName)
            add(textLiteral(" has collected ${currentTrophyIndex + 1}/${trophies.size} ${if (trophies.size <= 1) "trophy" else "trophies"}!"))
        }, MessageType.CHAT, Util.NIL_UUID)
        nextTrophy()
    }

    private fun nextTrophy() {
        currentTrophyIndex++

        if (currentTrophyIndex !in trophies.indices) {
            stop(ManhuntStopState.SPEEDRUNNER_WINS)
            return
        }

        val trophy = currentTrophyGoal!!

        if (trophy.blockPos.y != world.bottomY)
            world.setBlockState(trophy.blockPos, ManhuntRegistry.TROPHY_BLOCK.defaultState)

        sendTrophyPos(speedrunner, trophy.blockPos)
    }

    fun stop(state: ManhuntStopState) {
        if (!started) return

        trophies.forEach { world.setBlockState(it.blockPos, Blocks.AIR.defaultState) }

        sendPacketToAllPlayers(world) { sendStopState(it) }
        gameType.onStop(state)

        started = false
    }

    private fun registerEvents() {
        ServerTickEvents.END_SERVER_TICK.register {
            if (started)
                sendPacketToAllPlayers(world) { sendStartState(it, speedrunner) }
        }

        ServerTickEvents.END_WORLD_TICK.register { world ->
            for (trophy in trophies.subList(currentTrophyIndex, trophies.size)) {
                val blockPos = trophy.blockPos

                if (blockPos.y == world.bottomY && world.isChunkLoaded(blockPos)) {
                    trophy.blockPos = world.getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, blockPos)
                    if (trophy == currentTrophyGoal) {
                        world.setBlockState(trophy.blockPos, ManhuntRegistry.TROPHY_BLOCK.defaultState)
                    }
                }
            }
        }
    }

    private fun prefixed(func: KambrikTextBuilder<LiteralText>.() -> Unit = {}): Text {
        val builder = KambrikTextBuilder(textLiteral("[Manhunt] ") {
            format(Formatting.RED, Formatting.BOLD)
        })
        builder.func()
        return builder.root
    }
}
