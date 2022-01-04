package dev.isxander.manhunt.packets

import net.minecraft.util.Identifier

// Server -> Client
val MANHUNT_START = Identifier("manhunt", "start_manhunt")
val MANHUNT_STOP = Identifier("manhunt", "stop_manhunt")
val NEW_TROPHY_POS = Identifier("manhunt", "new_trophy_pos")
val MOD_CHECK = Identifier("manhunt", "mod_check")

// Client -> Server
val BREAK_TROPHY = Identifier("manhunt", "break_trophy")
val MOD_CONFIRM = Identifier("manhunt", "mod_check_confirm")
