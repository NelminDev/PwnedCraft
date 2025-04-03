package dev.nelmin.minecraft.commands

import dev.nelmin.minecraft.PwnedCraft
import dev.nelmin.minecraft.builders.TextBuilder
import org.bukkit.Bukkit
import org.bukkit.entity.Player

/**
 * Represents a command that allows a player to perform actions or send messages on behalf of another player.
 *
 * @param sender The player who issued the sudo command.
 * @param args The arguments provided with the sudo command.
 * @param pwnedCraft The instance of the PwnedCraft plugin used to access shared configurations and utilities.
 */
class SudoCommand(
    private val sender: Player,
    private val args: Array<out String>,
    private val pwnedCraft: PwnedCraft = PwnedCraft.instance
) {
    init {
        processCommand()
    }

    /**
     * Processes the command provided by the user. This includes validating the number of arguments
     * and delegating to appropriate handling methods based on the command type.
     *
     * If the number of arguments is insufficient, the syntax guide is displayed to the sender.
     * Calls `handleCommand` for further specific command processing.
     */
    private fun processCommand() {
        if (args.size < 3) {
            showSyntax()
            return
        }
        handleCommand()
    }

    /**
     * Handles the execution of a sudo command for a specific player. This function verifies the existence of the target
     * player, determines the action to be performed, and processes the corresponding content.
     *
     * The acceptable actions are:
     * - "cmd": Executes a command as the specified player.
     * - "msg": Sends a chat message as the specified player.
     *
     * If the target player is not found, a notification is sent to the command sender. If the action is invalid, the
     * syntax for the sudo command is displayed.
     *
     * The method expects the following parameters in the `args` array:
     * - args[0]: The target player's name.
     * - args[1]: The action to be performed ("cmd" or "msg").
     * - args[2...]: The content related to the action (e.g., command or message).
     *
     * This method interacts with the `Bukkit` API to retrieve player information, execute commands, or send messages, and
     * leverages `TextBuilder` for message formatting.
     */
    private fun handleCommand() {
        val targetPlayer = Bukkit.getPlayer(args[0])
        if (targetPlayer == null) {
            sender.sendMessage(TextBuilder("Player &c&l${args[0]}&r not found!").prefix().colorize())
            return
        }

        val action = args[1].lowercase()
        val content = args.drop(2).joinToString(" ")

        when (action) {
            "cmd" -> handleCommand(targetPlayer, content)
            "msg" -> handleMessage(targetPlayer, content)
            else -> showSyntax()
        }
    }

    /**
     * Executes a specified command as the target player and sends a message to the sender
     * indicating the action performed.
     *
     * @param targetPlayer The player on whose behalf the command will be executed.
     * @param command The command to be executed by the target player.
     */
    private fun handleCommand(targetPlayer: Player, command: String) {
        Bukkit.getScheduler().runTask(pwnedCraft, Runnable {
            val cleanCommand = command.removePrefix("/")
            Bukkit.dispatchCommand(targetPlayer, cleanCommand)

            sender.sendMessage(
                TextBuilder("Performed command '$command' as ${targetPlayer.name}")
                    .prefix()
                    .colorize()
            )
        })
    }

    /**
     * Sends a chat message as the target player.
     * Schedules the chat operation on the main server thread to avoid async chat errors.
     *
     * @param targetPlayer The player to send the message as
     * @param message The message to send
     */
    private fun handleMessage(targetPlayer: Player, message: String) {
        // Schedule the chat operation on the main server thread
        Bukkit.getScheduler().runTask(pwnedCraft, Runnable {
            targetPlayer.chat(message)
            sender.sendMessage(
                TextBuilder("Sent message as ${targetPlayer.name}")
                    .prefix()
                    .colorize()
            )
        })
    }

    /**
     * Sends the syntax guide message for the "sudo" command to the sender.
     * This method outputs information about the correct usage of the command
     * to help users understand its required parameters and structure.
     *
     * The displayed syntax includes:
     * - The player to target
     * - The action type: either "cmd" (command) or "msg" (message)
     * - The content associated with the specified action
     */
    private fun showSyntax() {
        sender.sendMessage(
            TextBuilder("""
                ---------- &8&l[&aSudo Command Syntax&8&l]&r ----------
                &7${pwnedCraft.commandPrefix}sudo <player> <cmd/msg> <content>
                &r--------------------------------------------
            """.trimIndent()).colorize(prefixOnNewLine = false)
        )
    }
}