package dev.nelmin.minecraft.commands

import dev.nelmin.minecraft.PwnedCraft
import dev.nelmin.minecraft.builders.TextBuilder
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

/**
 * Represents the main command handler for item-related commands in the game.
 * Handles various subcommands such as giving items, enchanting, adding lore, and renaming.
 *
 * @property sender The player who initiated the command.
 * @property args The arguments provided for the command.
 * @property pwnedCraft The instance of the main plugin class, used to access its configuration and utilities.
 */
class ItemCommand(
    private val sender: Player,
    private val args: Array<out String>,
    private val pwnedCraft: PwnedCraft = PwnedCraft.instance
) {
    init {
        processCommand()
    }

    /**
     * Processes the input command arguments for the `ItemCommand` class.
     *
     * This method validates whether the necessary arguments are provided.
     * If no arguments are given, it invokes `showSyntax()` to display the correct command usage.
     * Otherwise, it delegates further processing to the `handleCommand()` method.
     *
     * The method ensures that appropriate actions are taken based on the command's context,
     * either guiding the user on usage or handling the command execution.
     */
    private fun processCommand() {
        if (args.isEmpty()) {
            showSyntax()
            return
        }
        handleCommand()
    }

    /**
     * Handles the parsing and execution of the subcommands related to item manipulation.
     *
     * This method determines the subcommand to execute based on the first argument `args[0]`,
     * invoking the appropriate method for further processing. If the command does not match
     * a known subcommand, the syntax help is displayed.
     *
     * Subcommands handled:
     * - `give` → Delegates to `handleGive()` for giving items.
     * - `enchant` → Delegates to `handleEnchant()` for applying enchantments to held items.
     * - `addlore` → Delegates to `handleAddLore()` for adding lore to held items.
     * - `rename` → Delegates to `handleRename()` for renaming held items.
     * - Default → Invokes `showSyntax()` to display available commands and usage information.
     *
     * This method should be called after arguments are validated and the intended operation
     * is determined from the first argument.
     */
    private fun handleCommand() {
        when (args[0].lowercase()) {
            "give" -> handleGive()
            "enchant" -> handleEnchant()
            "addlore" -> handleAddLore()
            "rename" -> handleRename()
            else -> showSyntax()
        }
    }

    /**
     * Handles the "give" subcommand for the "/item" command.
     *
     * The method processes the player's request to give an item, validates the material name and amount,
     * and provides feedback to the player about the operation. The given item is added to the player's inventory.
     * If the material is invalid or the arguments are incomplete, appropriate error messages are shown.
     *
     * Behavior:
     * - If fewer than two arguments are provided, it displays usage instructions.
     * - The material name provided is validated as a valid `Material`.
     * - The amount is parsed, defaulting to 1 if unspecified or invalid.
     * - If the material name is valid, the specified amount of the item is created and added to the player's inventory.
     * - In case of an invalid material name, an error message is sent to the player.
     *
     * Preconditions:
     * - The sender is expected to have an accessible inventory.
     *
     * Validation:
     * - The material name is checked to ensure it matches a valid `Material` in the API.
     * - The amount is parsed as an integer and defaults to 1 if parsing fails.
     *
     * Error Handling:
     * - Displays an error message if the material name is invalid or arguments are insufficient.
     */
    private fun handleGive() {
        if (args.size < 2) {
            TextBuilder("Usage: /item give <material> [amount]")
                .prefix()
                .sendTo(sender)
            return
        }

        val materialName = args[1].uppercase()
        val amount = if (args.size >= 3) args[2].toIntOrNull() ?: 1 else 1

        try {
            val material = Material.valueOf(materialName)
            val item = ItemStack(material, amount)
            sender.inventory.addItem(item)

            TextBuilder("Given $amount ${material.name.lowercase()} to ${sender.name}")
                .prefix()
                .sendTo(sender)
        } catch (e: IllegalArgumentException) {
            TextBuilder("Invalid material name: $materialName")
                .prefix()
                .sendTo(sender)
        }
    }

    /**
     * Handles the process of adding an enchantment to an item held by the command sender.
     *
     * This method verifies the command input for the correct number of arguments and checks if the sender
     * is holding a valid item. If the conditions are not met, corresponding feedback messages are sent.
     *
     * If a valid enchantment name and level are provided, the method adds the enchantment to the held item,
     * ensuring the level is clamped within a safe range between 1 and 255. Feedback messages are sent to
     * notify the sender of successful or failed operations.
     *
     * Exceptions during execution are caught and a failure message is sent to the sender.
     *
     * Preconditions:
     * - The command sender must provide at least three arguments.
     * - The sender must be holding an item in their main hand.
     * - The enchantment name must match a valid enchantment registered in the server.
     *
     * Input:
     * - `args[1]`: The name of the enchantment, which is case-insensitively matched and converted to uppercase.
     * - `args[2]`: The desired level of the enchantment. Defaults to 1 if parsing fails.
     *
     * Effects:
     * - Adds a specified enchantment to the item in the sender's main hand, using the provided or default level.
     *
     * Error Handling:
     * - Insufficient arguments result in sending a usage message to the sender.
     * - Holding no item results in sending an appropriate feedback message.
     * - Invalid enchantment names or failures during the operation result in sending an error message to the sender.
     */
    private fun handleEnchant() {
        if (args.size < 3) {
            TextBuilder("Usage: /item enchant <enchantment> <level>")
                .prefix()
                .sendTo(sender)
            return
        }

        val item = sender.inventory.itemInMainHand
        if (item.type == Material.AIR) {
            TextBuilder("You must hold an item to enchant it")
                .prefix()
                .sendTo(sender)
            return
        }

        try {
            val enchantment = Enchantment.getByName(args[1].uppercase())
            val level = args[2].toIntOrNull() ?: 1

            if (enchantment == null) {
                TextBuilder("Invalid enchantment name")
                    .prefix()
                    .sendTo(sender)
                return
            }

            val safeLevel = level.coerceIn(1, 255)

            item.addUnsafeEnchantment(enchantment, safeLevel)
            TextBuilder("Added ${enchantment.key.key} level $safeLevel to your item")
                .prefix()
                .sendTo(sender)
        } catch (e: Exception) {
            TextBuilder("Failed to add enchantment")
                .prefix()
                .sendTo(sender)
        }
    }

    /**
     * Adds a custom lore text to the item held in the player's main hand.
     *
     * This method validates the command arguments, ensures that the player is holding a valid item,
     * and appends the specified lore text to the item's existing lore. Feedback is sent to the player
     * indicating success or failure.
     *
     * Validates the following:
     * - Ensures the command arguments are sufficient.
     * - Confirms the player is holding a non-air item in their main hand.
     *
     * If the item is valid and lorification is successful, the new lore is applied to the item,
     * and a confirmation message is sent to the player.
     *
     * Usage scenario:
     * - Called when a player uses the "addlore" subcommand of the item-related command system.
     */
    private fun handleAddLore() {
        if (args.size < 2) {
            TextBuilder("Usage: /item addlore <text>")
                .prefix()
                .sendTo(sender)
            return
        }

        val item = sender.inventory.itemInMainHand
        if (item.type == Material.AIR) {
            TextBuilder("You must hold an item to add lore")
                .prefix()
                .sendTo(sender)
            return
        }

        val loreText = args.slice(1 until args.size).joinToString(" ")
        val meta = item.itemMeta
        if (meta != null) {
            val lore = meta.lore ?: mutableListOf()
            lore.add(loreText)
            meta.lore = lore
            item.itemMeta = meta

            TextBuilder("Added lore to your item")
                .prefix()
                .sendTo(sender)
        }
    }

    /**
     * Handles the renaming of an item held by the sender.
     *
     * This method checks if the sender has provided a new name and if they are holding a valid item.
     * If the sender fails to provide the required arguments or is not holding an item, an appropriate
     * error message will be sent to them. Otherwise, the item in the sender's main hand will be renamed
     * using the new name provided in the arguments.
     *
     * Behavior:
     * - If less than two arguments are provided, a usage message is sent to the sender.
     * - If the sender is holding an empty hand (AIR), a message prompts them to hold an item first.
     * - If the sender provides valid input and is holding an item, the item's display name will be updated
     *   and a success message will be sent.
     *
     * Preconditions:
     * - The sender should provide a valid new name in the command arguments.
     * - The sender should be holding an item in their main hand.
     */
    private fun handleRename() {
        if (args.size < 2) {
            TextBuilder("Usage: /item rename <name>")
                .prefix()
                .sendTo(sender)
            return
        }

        val item = sender.inventory.itemInMainHand
        if (item.type == Material.AIR) {
            TextBuilder("You must hold an item to rename it")
                .prefix()
                .sendTo(sender)
            return
        }

        val newName = args.slice(1 until args.size).joinToString(" ")
        val meta = item.itemMeta
        if (meta != null) {
            meta.setDisplayName(newName)
            item.itemMeta = meta

            TextBuilder("Renamed your item to: $newName")
                .prefix()
                .sendTo(sender)
        }
    }

    /**
     * Displays the command syntax for item-related commands to the command sender.
     *
     * This method constructs and sends a formatted text containing the syntax of various
     * commands such as giving items, enchanting items, adding lore, and renaming items.
     * Each command is listed with its respective usage instructions.
     *
     * The syntax is dynamically injected with the command prefix defined in the `pwnedCraft` field.
     * The message is sent to the sender without adding an extra prefix to a new line.
     */
    private fun showSyntax() {
        TextBuilder("""
            ---------- &8&l[&aItem Command Syntax&8&l]&r ----------
            &7${pwnedCraft.commandPrefix}item &agive <material> [amount] &8&l| &7Give yourself items.
            &7${pwnedCraft.commandPrefix}item &aenchant <enchantment> <level> &8&l| &7Enchant held item.
            &7${pwnedCraft.commandPrefix}item &aaddlore <text> &8&l| &7Add lore to held item.
            &7${pwnedCraft.commandPrefix}item &arename <name> &8&l| &7Rename held item.
            &r-------------------------------------------
        """.trimIndent()).sendTo(sender, prefixOnNewLine = false)
    }
}