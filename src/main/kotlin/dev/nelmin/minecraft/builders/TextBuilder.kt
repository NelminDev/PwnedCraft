package dev.nelmin.minecraft.builders

import dev.nelmin.minecraft.PwnedCraft
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.Player

/**
 * A utility class for constructing and modifying text messages.
 * This class provides methods to add prefixes, suffixes, modify the text,
 * and send the constructed messages to players or the console.
 */
class TextBuilder(private var message: String) {

    /**
     * Adds a default prefix to the current message using the globally defined plugin prefix.
     *
     * @return The current instance of TextBuilder with the applied default prefix.
     */
    fun prefix(): TextBuilder {
        return prefix(PwnedCraft.instance.prefix)
    }

    /**
     * Appends the specified prefix to the current message.
     *
     * @param prefix The prefix string to be prepended to the message.
     * @return The current instance of TextBuilder with the updated message.
     */
    fun prefix(prefix: String): TextBuilder {
        this.message = "$prefix $message"
        return this
    }

    /**
     * Appends the specified suffix to the current message of the TextBuilder.
     *
     * @param suffix The string to be appended as the suffix to the current message.
     * @return The updated instance of TextBuilder with the suffix appended.
     */
    fun suffix(suffix: String): TextBuilder {
        this.message = "$message $suffix"
        return this
    }

    /**
     * Sets the message content for the `TextBuilder` instance.
     *
     * @param message The new message to set.
     * @return The current `TextBuilder` instance.
     */
    fun message(message: String): TextBuilder {
        this.message = message
        return this
    }

    /**
     * Replaces all occurrences of the specified search value in the current message with the provided replacement value.
     *
     * @param search the value to search for within the message
     * @param replacement the value to replace the search value with
     * @return the updated TextBuilder instance with the modified message
     */
    fun replace(search: Any, replacement: Any): TextBuilder {
        this.message = this.message.replace(search.toString(), replacement.toString())
        return this
    }

    /**
     * Replaces occurrences of specified keys in the text with their corresponding values.
     *
     * @param map a map where each key represents a value to be replaced in the text,
     * and each value represents the replacement to be applied.
     * @return the updated TextBuilder instance after applying all replacements.
     */
    fun replace(map: Map<Any, Any>): TextBuilder {
        map.forEach { (search, replacement) -> replace(search, replacement) }
        return this
    }

    /**
     * Sends the current text message to a specified player.
     * The message can optionally be colorized before being sent.
     *
     * @param player The player to whom the message will be sent.
     * @param colorized Determines whether the message should be colorized using color codes. Defaults to true.
     * @return This TextBuilder instance for method chaining.
     */
    fun sendTo(player: Player, colorized: Boolean = true, prefixOnNewLine: Boolean = true): TextBuilder {
        player.sendMessage(if (colorized) colorize(prefixOnNewLine = prefixOnNewLine) else message)
        return this
    }

    /**
     * Sends the current message to a list of players. The message can be optionally colorized.
     *
     * @param players The list of players who will receive the message.
     * @param colorized Determines if the message should be colorized before sending. Defaults to true.
     * @return The current instance of TextBuilder for method chaining.
     */
    fun sendTo(players: List<Player>, colorized: Boolean = true, prefixOnNewLine: Boolean = true): TextBuilder {
        players.forEach { player: Player ->
            player.sendMessage(if (colorized) colorize(prefixOnNewLine = prefixOnNewLine) else message)
        }
        return this
    }

    /**
     * Sends the formatted message to the console in a colorized format.
     *
     * @return The current instance of TextBuilder with the processed message.
     */
    fun sendToConsole(prefixOnNewLine: Boolean = true): TextBuilder {
        Bukkit.getConsoleSender().sendMessage(colorize(prefixOnNewLine = prefixOnNewLine))
        return this
    }

    /**
     * Translates alternate color codes in the current message based on a specified legacy color code character
     * and optionally applies a prefix to new lines.
     *
     * @param legacyCharacter The character used to indicate color codes in the text. Defaults to '&'.
     * @param prefixOnNewLine Determines whether a prefix should be added to new lines in the text. Defaults to true.
     * @return The colorized string with alternate color codes translated and optional prefixes applied.
     */
    fun colorize(legacyCharacter: Char = '&', prefixOnNewLine: Boolean = true): String {
        return ChatColor.translateAlternateColorCodes(legacyCharacter, if (prefixOnNewLine) get() else getNPON())
    }

    /**
     * Retrieves the processed message by replacing newline characters with a combination of newline
     * and the globally defined prefix obtained from the `prefix()` method.
     *
     * @return The processed message with prefixed newline characters.
     */
    fun get(): String {
        return message.replace("\n", "\n${prefix()} ")
    }

    /**
     * Retrieves the current message content of the TextBuilder instance.
     *
     * @return The current message as a string.
     */
    fun getNPON(): String {
        return message
    }

    /**
     * Returns the colorized message string.
     * This is called when the TextBuilder is used in string contexts.
     */
    override fun toString(): String {
        return colorize(prefixOnNewLine = false)
    }
}