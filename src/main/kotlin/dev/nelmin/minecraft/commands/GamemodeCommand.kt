package dev.nelmin.minecraft.commands

import dev.nelmin.minecraft.PwnedCraft
import dev.nelmin.minecraft.builders.TextBuilder
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.entity.Player

/**
 * The `GamemodeCommand` class is responsible for handling the gamemode command logic within the PwnedCraft plugin.
 * This command allows players to change their own or another player's gamemode by specifying the desired mode and optionally a target player.
 *
 * @constructor Creates a new instance of the `GamemodeCommand` class.
 * @param sender The player who executed the command.
 * @param args The arguments provided with the command.
 * @param pwnedCraft The instance of the PwnedCraft plugin managing the command.
 */
class GamemodeCommand(
    private val sender: Player,
    private val args: Array<out String>,
    private val pwnedCraft: PwnedCraft = PwnedCraft.instance
) {
    init {
        processCommand()
    }

    /**
     * Processes the command provided by the user.
     *
     * If the `args` field is empty, it invokes the `showSyntax` method to display the command syntax and terminates processing.
     * Otherwise, the command is forwarded to the `handleCommand` method for further handling.
     *
     * This function is designed to manage command validation and delegation for the gamemode-specific commands.
     */
    private fun processCommand() {
        if (args.isEmpty()) {
            showSyntax()
            return
        }
        handleCommand()
    }

    /**
     * Handles the `/gamemode` command by parsing the game mode and setting it for the target player.
     *
     * The command follows the syntax: `/gamemode <mode> [player]`.
     * If the game mode specified in the arguments is invalid, it displays the command syntax.
     * If the target player is not found (when specified), it notifies the command sender.
     * If everything is valid, it updates the game mode of the target player asynchronously
     * and sends a confirmation message to the command sender.
     *
     * Behavior:
     * - Parses the game mode from the first argument using `parseGameMode`.
     * - If the mode is invalid, calls `showSyntax` to display the correct command usage.
     * - Resolves the target player from the second argument, or defaults to the sender if not provided.
     * - If the target player is not found, sends an error message to the sender.
     * - Sets the target player's game mode asynchronously and sends a success message.
     *
     * Preconditions:
     * - The `args` array should contain at least one element specifying the game mode.
     * - `pwnedCraft` must be properly initialized for scheduling tasks.
     * - `sender` must be a valid player or console executing the command.
     *
     * Side Effects:
     * - Sends messages to the command sender and, if necessary, to the resolved player.
     * - Updates the game mode of the target player.
     */
    private fun handleCommand() {
        val gameMode = parseGameMode(args[0])
        if (gameMode == null) {
            showSyntax()
            return
        }

        val target = if (args.size > 1) {
            pwnedCraft.server.getPlayer(args[1])
        } else {
            sender
        }

        if (target == null) {
            TextBuilder("Player not found")
                .prefix()
                .sendTo(sender)
            return
        }

        Bukkit.getScheduler().runTask(pwnedCraft, Runnable {
            target.gameMode = gameMode
            TextBuilder("Set ${target.name}'s gamemode to ${gameMode.name.lowercase()}")
                .prefix()
                .sendTo(sender)
        })
    }

    /**
     * Parses the input string to determine the corresponding game mode.
     *
     * @param input the string representation of the game mode, which can be a number (e.g., "0", "1"),
     *              full name (e.g., "survival", "creative"), or an abbreviation (e.g., "s", "c").
     * @return the GameMode corresponding to the input, or null if the input does not match any valid game mode.
     */
    private fun parseGameMode(input: String): GameMode? {
        return when (input.lowercase()) {
            "0", "survival", "s" -> GameMode.SURVIVAL
            "1", "creative", "c" -> GameMode.CREATIVE
            "2", "adventure", "a" -> GameMode.ADVENTURE
            "3", "spectator", "sp" -> GameMode.SPECTATOR
            else -> null
        }
    }

    /**
     * Displays the syntax for the "gamemode" command to the sender.
     * The syntax includes the main command, available modes, and aliases.
     * It is formatted and color-coded for readability.
     *
     * The displayed syntax:
     * - Command structure: `/gamemode <mode> [player]`
     * - Modes: Survival, Creative, Adventure, and Spectator, represented by their respective numbers or names.
     * - Aliases: `gm`, `gamemode`.
     *
     * This method utilizes the `TextBuilder` class to construct and send the formatted syntax to the sender without adding
     * a prefix to new lines.
     */
    private fun showSyntax() {
        TextBuilder("""
            ---------- &8&l[&aGamemode Command Syntax&8&l]&r ----------
            &7${pwnedCraft.commandPrefix}gamemode &a<mode> [player] &8&l| &7Change gamemode.
            &7Modes: &a0/survival, 1/creative, 2/adventure, 3/spectator
            &7Aliases: &agm, gamemode
            &r-------------------------------------------
        """.trimIndent()).sendTo(sender, prefixOnNewLine = false)
    }
}