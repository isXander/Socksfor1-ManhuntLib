package dev.isxander.manhunt.server

import dev.isxander.manhunt.ManhuntStopState
import dev.isxander.manhunt.Trophy
import dev.isxander.manhunt.api.ManhuntGameType
import dev.isxander.manhunt.packets.server.sendStartState
import dev.isxander.manhunt.packets.server.sendStopState
import dev.isxander.manhunt.packets.server.sendTrophyPos
import dev.isxander.manhunt.registry.ManhuntRegistry
import dev.isxander.manhunt.utils.sendPacketToAllPlayers
import io.ejekta.kambrik.text.sendMessage
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.block.Blocks
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Formatting
import net.minecraft.util.math.BlockPos
import net.minecraft.world.Heightmap
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class ManhuntGame(val gameType: ManhuntGameType, val world: ServerWorld, val speedrunner: ServerPlayerEntity, val trophyRadius: Int) {
    val trophies = mutableListOf<Trophy>()

    var currentTrophyIndex: Int = 0
        private set

    val currentTrophyGoal: Trophy?
        get() = trophies.getOrNull(currentTrophyIndex)

    var started = false
        private set

    init {
        ServerEntityCombatEvents.AFTER_KILLED_OTHER_ENTITY.register { world, attacker, victim ->
            if (victim == speedrunner) {
                stop(ManhuntStopState.HUNTERS_WIN)
            }
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

    fun start() {
        check(!started) { "Instance already started!" }

        registerTrophies()
        readyPlayers()

        val trophy = trophies[currentTrophyIndex]
        world.setBlockState(trophy.blockPos, ManhuntRegistry.TROPHY_BLOCK.defaultState)
        sendTrophyPos(speedrunner, trophy.blockPos)

        gameType.onGameStart(this)
        sendPacketToAllPlayers(world) { sendStartState(it) }

        started = true
    }

    private fun registerTrophies() {
        val centerX = speedrunner.blockX
        val centerZ = speedrunner.blockZ

        for (trophy in gameType.provideTrophies()) {
            val random = world.random
            val randomRadius = trophyRadius * sqrt(random.nextDouble())
            val theta = random.nextDouble() * 2 * Math.PI

            val trophyX = centerX + randomRadius * cos(theta)
            val trophyZ = centerZ + randomRadius * sin(theta)
            val trophyY = world.getTopY(Heightmap.Type.WORLD_SURFACE, trophyX.toInt(), trophyZ.toInt())
            val trophyPos = BlockPos(trophyX, trophyY.toDouble(), trophyZ)

            println("Registering trophy at $trophyPos")
            trophies.add(Trophy(trophy, trophyPos))
        }
    }

    private fun readyPlayers() {
        for (player in world.players) {
            if (player == speedrunner) {
                player.sendMessage("You are the speedrunner! You have ${trophies.size} ${if (trophies.size > 1) "trophies" else "trophy"} to collect! Follow the compass to find them!") {
                    format(Formatting.RED)
                    bold = true
                }
                speedrunner.giveItemStack(ItemStack(Items.COMPASS))
            } else {
                player.sendMessage("You are a hunter! Hunt the speedrunner, ${speedrunner.displayName}!") {
                    format(Formatting.GREEN)
                    bold = true
                }
            }
        }
    }

    fun breakTrophy(playerEntity: ServerPlayerEntity, pos: BlockPos) {
        if (pos !in trophies.map { it.blockPos }) {
            println("Deleting unknown trophy block!")
            world.setBlockState(pos, Blocks.AIR.defaultState)
            return
        }

        if (pos != currentTrophyGoal?.blockPos || playerEntity != speedrunner) return

        world.setBlockState(pos, Blocks.AIR.defaultState)
        currentTrophyGoal!!.onAchieved.invoke(this)
        gameType.onTrophyCollected(this)
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
        check(started) { "Instance not started!" }

        trophies.forEach { world.setBlockState(it.blockPos, Blocks.AIR.defaultState) }
        speedrunner.inventory.remove({ it.item == Items.COMPASS }, 1, speedrunner.inventory)

        sendPacketToAllPlayers(world) { sendStopState(it) }
        gameType.onStop(state)

        started = false
    }
}
