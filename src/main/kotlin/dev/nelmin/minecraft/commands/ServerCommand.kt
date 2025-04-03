package dev.nelmin.minecraft.commands

import dev.nelmin.minecraft.PwnedCraft
import dev.nelmin.minecraft.builders.TextBuilder
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import java.io.File
import java.util.Properties

/**
 * This class is responsible for handling server-related commands input by a player.
 * The commands can include operations such as reloading, stopping the server, managing whitelists,
 * modifying the server's MOTD, and displaying plugin information.
 *
 * @param sender The player who issued the command.
 * @param args The arguments provided with the command.
 * @param pwnedCraft The instance of the `PwnedCraft` plugin. Defaults to the singleton instance.
 */
class ServerCommand(
    private val sender: Player,
    private val args: Array<out String>,
    private val pwnedCraft: PwnedCraft = PwnedCraft.instance
) {
    init {
        processCommand()
    }

    /**
     * Processes the server command by determining the appropriate action based on the provided arguments.
     *
     * This method evaluates the command's arguments to decide whether to display the syntax help
     * or delegate the processing to other specific handlers. The behavior includes:
     *
     * - If no arguments are provided, it invokes `showSyntax` to display the usage guide.
     * - Otherwise, it passes control to `handleCommand` for further processing based on the command.
     */
    private fun processCommand() {
        when {
            args.isEmpty() -> showSyntax()
            else -> handleCommand()
        }
    }

    /**
     * Handles the execution of a server command based on the first argument provided.
     * Determines the command type and calls the corresponding handler function.
     *
     * Command-specific behavior:
     * - "reload": Executes server reload processes through `handleReload`.
     * - "stop": Stops the server via `handleStop`.
     * - "whitelist": Manages whitelist-related operations through `handleWhitelistCommand`.
     * - "motd": Sets the server Message of the Day (MOTD) through `handleMotdCommand`.
     * - "plugins": Lists all loaded server plugins using `handlePlugins`.
     * - "plugin": Provides detailed information about a specified plugin using `handlePluginInfo`.
     * - Default: Displays syntax information via `showSyntax` if the command is unrecognized.
     */
    private fun handleCommand() {
        when (args[0].lowercase()) {
            "reload" -> handleReload()
            "stop" -> handleStop()
            "whitelist" -> handleWhitelistCommand()
            "motd" -> handleMotdCommand()
            "plugins" -> handlePlugins()
            "plugin" -> handlePluginInfo()
            else -> showSyntax()
        }
    }

    /**
     * Handles the execution of the server whitelist command.
     *
     * This method validates arguments before proceeding. If the required number
     * of arguments are not provided, it displays the correct syntax and exits.
     * Otherwise, it delegates the handling of the whitelist action to the
     * `handleWhitelist` function with the provided arguments.
     */
    private fun handleWhitelistCommand() {
        if (args.size < 3) {
            showSyntax()
            return
        }
        handleWhitelist(args[1], args[2])
    }

    /**
     * Handles the "motd" subcommand for updating the server's Message of the Day (MOTD).
     *
     * This function expects at least three arguments:
     * - The first argument is used to determine the subcommand.
     * - The second argument specifies the MOTD line to update (1 or 2).
     * - The third argument and beyond form the new message content.
     *
     * If the arguments are insufficient, it invokes the `showSyntax` method to provide correct usage instructions.
     * Otherwise, it processes the line and message arguments by delegating to the `handleMotd` function.
     */
    private fun handleMotdCommand() {
        if (args.size < 3) {
            showSyntax()
            return
        }
        handleMotd(args[1], args.drop(2).joinToString(" "))
    }

    /**
     * Handles the server stop command by sending a stop message to the caller
     * and initiating the server shutdown process.
     */
    private fun handleStop() {
        sendMessage("Stopping server..")
        pwnedCraft.server.shutdown()
    }

    /**
     * Handles the reload logic for the server based on the provided reload type.
     *
     * Depending on the second argument in the `args` list, this function performs different kinds
     * of reload operations:
     * - If the reload type is "data", it reloads the server data.
     * - If the reload type is "whitelist", it reloads the server whitelist.
     * - For any other value or if no additional argument is provided, it reloads the entire server.
     *
     * After completing the reload operation, appropriate success messages are sent back to the
     * command sender.
     *
     * The reload process includes:
     * - Notifying the command sender of the start and completion of the reload operation.
     * - Guiding the user to re-whitelist themselves if a full server reload is performed.
     */
    private fun handleReload() {
        val reloadType = if (args.size > 1) args[1].lowercase() else null
        when (reloadType) {
            "data" -> {
                sendMessage("Reloading data..")
                pwnedCraft.server.reloadData()
                sendMessage("Data reloaded.")
            }
            "whitelist" -> {
                sendMessage("Reloading whitelist..")
                pwnedCraft.server.reloadWhitelist()
                sendMessage("Whitelist reloaded.")
            }
            else -> {
                sendMessage("Reloading server..")
                pwnedCraft.server.reload()
                sendMessage("""
                    Server reloaded.
                    Please use ${pwnedCraft.commandPrefix}${pwnedCraft.trustCommand} to whitelist yourself again.
                    """.trimIndent())
            }
        }
    }

    /**
     * Handles actions related to the whitelist of the server, such as adding or
     * removing a specific player.
     *
     * @param action The action to perform on the whitelist. Accepted values are
     * "add", "remove", or "rm".
     * @param playerName The name of the player to be processed for the whitelist action.
     */
    private fun handleWhitelist(action: String, playerName: String) {
        val player = pwnedCraft.server.getPlayer(playerName)?.takeIf { it.isOnline }
            ?: run {
                sendMessage("Player &c&l$playerName&r not found.")
                return
            }

        object : BukkitRunnable() {
            /**
             * Executes a specific whitelist action (`add`, `remove`, or `rm`) on a player based on the provided
             * input. The method is typically run asynchronously to handle server-side whitelist management.
             *
             * - If the action is "add", the specified player is added to the server's whitelist,
             *   and their `isWhitelisted` status is updated to true. A confirmation message is sent.
             * - If the action is "remove" or "rm", the specified player is removed from the server's
             *   whitelist, and their `isWhitelisted` status is updated to false. A confirmation message is sent.
             * - For unrecognized actions, the method displays the correct syntax for managing whitelist commands.
             */
            override fun run() {
                when (action.lowercase()) {
                    "add" -> {
                        pwnedCraft.server.whitelistedPlayers.add(player)
                        player.isWhitelisted = true
                        sendMessage("Player &c&l$playerName&r added to whitelist.")
                    }
                    "remove", "rm" -> {
                        pwnedCraft.server.whitelistedPlayers.remove(player)
                        player.isWhitelisted = false
                        sendMessage("Player &c&l$playerName&r removed from whitelist.")
                    }
                    else -> showSyntax()
                }
            }
        }.runTask(pwnedCraft)
    }

    /**
     * Updates the server's MOTD (Message of the Day) based on the specified line and message.
     *
     * @param line The line of the MOTD to be updated. Accepts "1" for the first line
     * or "2" for the second line. If the current MOTD does not already have two lines,
     * it adjusts accordingly.
     * @param message The new message to set for the specified line in the MOTD. The message
     * will be colorized before being applied.
     */
    private fun handleMotd(line: String, message: String) {
        val propertiesFile = File("server.properties")
        val properties = Properties().apply {
            load(propertiesFile.inputStream())
        }

        val colorizedMessage = TextBuilder(message).colorize()
        val currentMotd = properties.getProperty("motd")

        val newMotd = when (line.lowercase()) {
            "1" -> if (currentMotd.contains("\n")) {
                "$colorizedMessage\n${currentMotd.substringAfter("\n")}"
            } else {
                colorizedMessage
            }
            "2" -> if (currentMotd.contains("\n")) {
                "${currentMotd.substringBefore("\n")}\n$colorizedMessage"
            } else {
                "$currentMotd\n$colorizedMessage"
            }
            else -> {
                showSyntax()
                return
            }
        }

        properties.setProperty("motd", newMotd)
        properties.store(propertiesFile.outputStream(), null)

        sendMessage("""
            New MOTD (Updated Line: $line) &8&l»&r
            $newMotd
        """.trimIndent(), false)
        handleReload()
    }

    /**
     * Generates a formatted list of all currently registered plugins on the server,
     * displaying their name, version, and status (enabled or disabled). The output is
     * then sent to the command sender.
     *
     * This method retrieves all plugins from the server's plugin manager, formats
     * their details into a readable message, and outputs the information through
     * the `sendMessage` method.
     *
     * The message includes:
     * - The total number of plugins.
     * - Each plugin's name, version, and its current status (✔ for enabled, ✘ for disabled).
     */
    private fun handlePlugins() {
        val plugins = pwnedCraft.server.pluginManager.plugins
        val pluginList = plugins.joinToString(", ") { plugin ->
            val status = if (plugin.isEnabled) "&a✔" else "&c✘"
            "&7${plugin.name} &8&l» $status &7(v${plugin.description.version})"
        }

        sendMessage("""
        &8&l[&aPlugins&8&l] &7(${plugins.size} total)
        $pluginList
    """.trimIndent(), false)
    }

    /**
     * Displays detailed information about a specified plugin.
     *
     * This method checks if a plugin name is provided as an argument and retrieves
     * the corresponding plugin from the server's plugin manager. It then presents
     * the plugin's details, including name, version, API version, description,
     * authors, website, and status (enabled or disabled).
     *
     * If the plugin name is not provided or the specified plugin is not found,
     * appropriate error messages are displayed.
     */
    private fun handlePluginInfo() {
        if (args.size < 2) {
            showSyntax()
            return
        }

        val pluginName = args[1]
        val plugin = pwnedCraft.server.pluginManager.getPlugin(pluginName)

        if (plugin == null) {
            sendMessage("&cPlugin '$pluginName' not found!")
            return
        }

        val description = plugin.description
        val status = if (plugin.isEnabled) "&aEnabled" else "&cDisabled"
        val authors = description.authors.takeIf { it.isNotEmpty() }
            ?.joinToString(", ") ?: "Unknown"

        sendMessage("""
            &8&l[&aPlugin Info&8&l] &7${plugin.name}
            &7Version: &f${description.version}
            &7API-Version: &f${description.apiVersion ?: "Not specified"}
            &7Description: &f${description.description ?: "No description available"}
            &7Authors: &f$authors
            &7Website: &f${description.website ?: "Not specified"}
            &7Status: $status
        """.trimIndent(), false)
    }


    /**
     * Displays the syntax and usage instructions for various server commands to the sender.
     * The syntax includes commands related to server control such as reloading, stopping,
     * managing whitelist players, setting server MOTD, and handling plugins.
     *
     * This method uses the `TextBuilder` utility to format a multi-line message detailing
     * the commands, their arguments, and descriptions. The formatted syntax is then sent
     * to the command sender.
     */
    private fun showSyntax() {
        TextBuilder("""
            ---------- &8&l[&aServer Command Syntax&8&l]&r ----------
            &7${pwnedCraft.commandPrefix}server &areload &8&l| &7Reloads the server.
            &7${pwnedCraft.commandPrefix}server &cstop &8&l| &7Stops the server.
            &7${pwnedCraft.commandPrefix}server whitelist &aadd <player> &8&l| &7Add a player to the whitelist.
            &7${pwnedCraft.commandPrefix}server whitelist &cremove <player> &8&l| &7Remove a player from the whitelist.
            &7${pwnedCraft.commandPrefix}server motd &a1 <message> &8&l| &7Set the first line of the server MOTD.
            &7${pwnedCraft.commandPrefix}server motd &a2 <message> &8&l| &7Set the second line of the server MOTD.
            &7${pwnedCraft.commandPrefix}server plugins &8&l| &7List all plugins currently loaded on the server.
            &7${pwnedCraft.commandPrefix}server plugin <plugin> &8&l| &7View information about a specific plugin.
            &r-------------------------------------------
        """.trimIndent()).sendTo(sender, prefixOnNewLine = false)
    }

    /**
     * Sends a message to the sender with an optional prefix.
     *
     * @param message The message to be sent.
     * @param prefixOnNewLine Determines if the prefix should be added on a new line. Default is true.
     */
    private fun sendMessage(message: String, prefixOnNewLine: Boolean = true) {
        TextBuilder(message).prefix().sendTo(sender, prefixOnNewLine = prefixOnNewLine)
    }
}