package dev.nelmin.minecraft.commands

import dev.nelmin.minecraft.PwnedCraft
import dev.nelmin.minecraft.builders.TextBuilder
import org.bukkit.entity.Player
import java.io.File

/**
 * Represents a system command handler in the PwnedCraft plugin. This class processes and executes
 * commands issued by a player related to virtual file system operations and system details.
 *
 * @property sender The player who triggered the command.
 * @property args The arguments provided by the player.
 * @property pwnedCraft An instance of the PwnedCraft plugin.
 */
class SystemCommand(
    private val sender: Player,
    private val args: Array<out String>,
    private val pwnedCraft: PwnedCraft = PwnedCraft.instance
) {
    init {
        processCommand()
    }

    /**
     * Processes the incoming command by determining its validity and type.
     *
     * If no arguments are provided, the syntax information for the
     * system commands is displayed by invoking `showSyntax`. Otherwise, it
     * delegates the handling of the command to `handleCommand`.
     *
     * The command structure and its behavior depend on the arguments
     * passed by the user, and the appropriate logic to process the
     * command is handled separately.
     */
    private fun processCommand() {
        when {
            args.isEmpty() -> showSyntax()
            else -> handleCommand()
        }
    }

    /**
     * Handles the execution of various commands based on the provided arguments.
     * The commands include operations such as retrieving OS information, managing
     * directories, creating files or directories, and interacting with files.
     *
     * Command overview:
     * - "os": Retrieves and displays the current operating system name.
     * - "usr": Retrieves and displays the current system username.
     * - "ls": Lists the contents of a specified directory.
     * - "goto": Changes the current directory to the specified path.
     * - "rm" or "remove": Removes a file or directory, with an optional force parameter.
     * - "mk": Creates a file or directory based on the specified type.
     * - "write": Prepares a specified file for writing.
     * - "kv": Writes a key-value pair to a specified file.
     *
     * If an unrecognized command is provided, the command syntax/usage is displayed to guide the user.
     */
    private fun handleCommand() {
        when (args[0].lowercase()) {
            "os" -> handleOS()
            "usr" -> handleGetCurrentUser()
            "ls" -> {
                val path = args.getOrNull(1) ?: "."
                handleListContent(path)
            }
            "goto" -> {
                if (args.size < 2) {
                    sendMessage("&cPlease specify a path")
                    return
                }
                handleGoTo(args[1])
            }
            "rm", "remove" -> {
                if (args.size < 2) {
                    sendMessage("&cPlease specify a path")
                    return
                }
                val force = args.getOrNull(2)?.toBoolean() ?: false
                handleRemove(args[1], force)
            }
            "mk" -> {
                if (args.size < 3) {
                    sendMessage("&cPlease specify type and path")
                    return
                }
                handleMake(args[1], args[2])
            }
            "write" -> {
                if (args.size < 2) {
                    sendMessage("&cPlease specify a file")
                    return
                }
                handleWriteToFile(args[1])
            }
            "kv" -> {
                if (args.size < 4) {
                    sendMessage("&cPlease specify file, key, and value")
                    return
                }
                handleKeyValueWriteToFile(args[1], args[2], args[3])
            }
            else -> showSyntax()
        }
    }

    /**
     * Retrieves the current directory associated with the sender's unique ID.
     *
     * @return The current directory as a String if the sender's unique ID is found in the mappings;
     *         returns "." if no matching directory is found.
     */
    private fun getCurrentDirectory(): String {
        return pwnedCraft.gotoDirsToPlayers
            .entries
            .find { it.value.contains(sender.uniqueId) }
            ?.key ?: "."
    }

    /**
     * Handles changing the current directory context for the user. Resolves the given path and validates
     * it as a directory. Adjusts user tracking for the directory change to reflect the new target path.
     *
     * @param path The path to navigate to. Can be relative (e.g., `./`, `../`), absolute, or special (e.g., `.`, `..`).
     */
    private fun handleGoTo(path: String) {
        val targetPath = when {
            path.startsWith("./") -> {
                // Handle relative path from current directory
                resolvePath(path.substring(2))
            }
            path.startsWith("../") -> {
                // Handle parent directory navigation
                File(getCurrentDirectory()).parent ?: "."
            }
            path == ".." -> {
                // Handle simple parent directory
                File(getCurrentDirectory()).parent ?: "."
            }
            path == "." -> {
                // Stay in current directory
                getCurrentDirectory()
            }
            // Check if it's an absolute Windows path (e.g., C:\, D:\)
            path.matches(Regex("[A-Za-z]:\\\\.*")) || path.matches(Regex("[A-Za-z]:/.*")) -> {
                // Use the absolute path directly
                path.replace('\\', '/')
            }
            else -> {
                // Handle relative path
                resolvePath(path)
            }
        }

        val targetFile = File(targetPath)
        if (!targetFile.exists() || !targetFile.isDirectory) {
            sendMessage("&cInvalid directory: $targetPath")
            return
        }

        // Create directory set if it doesn't exist
        pwnedCraft.gotoDirsToPlayers.computeIfAbsent(targetPath) { mutableSetOf() }

        // Remove player from old directory
        pwnedCraft.gotoDirsToPlayers.values.forEach { it.remove(sender.uniqueId) }

        // Add player to new directory
        pwnedCraft.gotoDirsToPlayers[targetPath]?.add(sender.uniqueId)

        sendMessage("&aChanged directory to: $targetPath")
    }

    /**
     * Resolves a given file path to its absolute or normalized form based on the current directory.
     *
     * @param path The path to resolve. It can be an absolute path, relative path, or path with notations like `./` or `../`.
     * @return The resolved and normalized path using forward slashes as the separator.
     */
    private fun resolvePath(path: String): String {
        // Check if it's an absolute Windows path
        if (path.matches(Regex("[A-Za-z]:\\\\.*")) || path.matches(Regex("[A-Za-z]:/.*"))) {
            return path.replace('\\', '/')
        }

        val currentDir = getCurrentDirectory()
        val baseFile = File(currentDir)

        return when {
            path.startsWith("/") -> path // Absolute path
            path.startsWith("./") -> File(baseFile, path.substring(2)).path
            path.startsWith("../") -> {
                val parent = baseFile.parent ?: "."
                File(parent, path.substring(3)).path
            }
            else -> File(baseFile, path).path
        }.replace('\\', '/') // Normalize path separators
    }

    /**
     * Retrieves the name of the current operating system using the system property "os.name"
     * and sends it as a message.
     *
     * This method interfaces with the system's environment to fetch the OS information and
     * uses the `sendMessage` function to output the required data.
     */
    private fun handleOS() {
        val osName = System.getProperty("os.name")
        sendMessage("Current operating system: $osName")
    }

    /**
     * Retrieves the current system username and sends it as a message.
     *
     * This method uses the `user.name` system property to obtain the username
     * of the active system user and utilizes the `sendMessage` function to
     * display the retrieved username.
     */
    private fun handleGetCurrentUser() {
        val username = System.getProperty("user.name")
        sendMessage("Current system username: $username")
    }

    /**
     * Handles listing the content of a directory specified by the provided path.
     * Verifies the input path, checks its existence and type, and then displays
     * the directory's content, listing directories and files separately.
     *
     * @param path The path to the directory whose contents should be listed.
     */
    private fun handleListContent(path: String) {
        val fullPath = resolvePath(path)
        val directory = File(fullPath)

        if (!directory.exists()) {
            sendMessage("&cDirectory does not exist: $fullPath")
            return
        }
        if (!directory.isDirectory) {
            sendMessage("&cPath is not a directory: $fullPath")
            return
        }

        val contents = directory.listFiles()
        if (contents == null) {
            sendMessage("&cUnable to list directory contents")
            return
        }

        sendMessage("Contents of $fullPath:")

        // Group files and directories
        val directories = contents.filter { it.isDirectory }.map { it.name }
        val files = contents.filter { !it.isDirectory }.map { it.name }

        // Display directories
        if (directories.isNotEmpty()) {
            sendMessage("Directories:&7 ${directories.joinToString(", ")}")
        }

        // Display files
        if (files.isNotEmpty()) {
            sendMessage("Files:&7 ${files.joinToString(", ")}")
        }

        if (contents.isEmpty()) {
            sendMessage("&7(empty directory)")
        }
    }

    /**
     * Handles the removal of a file or directory at the specified path.
     *
     * @param path The file or directory path to be removed. This can be an absolute or relative path.
     * @param force If true, allows the deletion of non-empty directories. If false, the method prevents
     *              deletion of non-empty directories.
     */
    private fun handleRemove(path: String, force: Boolean) {
        val fullPath = resolvePath(path)
        val file = File(fullPath)

        if (!file.exists()) {
            sendMessage("&cPath does not exist: $fullPath")
            return
        }

        if (file.isDirectory && !force && file.listFiles()?.isNotEmpty() == true) {
            sendMessage("&cDirectory is not empty. Use force=true to delete anyway.")
            return
        }

        val success = if (file.isDirectory) {
            file.deleteRecursively()
        } else {
            file.delete()
        }

        if (success) {
            sendMessage("&aSuccessfully removed: $fullPath")
        } else {
            sendMessage("&cFailed to remove: $fullPath")
        }
    }

    /**
     * Handles the creation of a file or directory at the specified path.
     * Validates the input, checks if the path already exists, and attempts to create
     * the specified type (either "file" or "dir"). Reports success or failure via messages.
     *
     * @param type The type of item to create. Must be either "file" or "dir" (case-insensitive).
     * @param path The file system path where the item should be created. Must not be null or blank.
     */
    private fun handleMake(type: String?, path: String?) {
        if (type.isNullOrBlank() || path.isNullOrBlank() || type.lowercase() !in listOf("file", "dir")) {
            sendMessage("&7${pwnedCraft.commandPrefix}system mk &a<file/dir> <path to file/directory>")
            return
        }

        val fullPath = resolvePath(path)
        val file = File(fullPath)

        if (file.exists()) {
            sendMessage("&cPath already exists: $fullPath")
            return
        }

        val success = when (type.lowercase()) {
            "file" -> file.createNewFile()
            "dir" -> file.mkdirs()
            else -> {
                sendMessage("&cInvalid type. Use 'file' or 'dir'")
                return
            }
        }

        if (success) {
            sendMessage("&aSuccessfully created ${type.lowercase()}: $fullPath")
        } else {
            sendMessage("&cFailed to create ${type.lowercase()}: $fullPath")
        }
    }

    /**
     * Handles the process of verifying if a file exists and preparing it for writing.
     *
     * @param path The path to the file that needs to be checked for existence and prepared for writing.
     */
    private fun handleWriteToFile(path: String) {
        val fullPath = resolvePath(path)
        val file = File(fullPath)

        if (!file.exists()) {
            sendMessage("&cFile does not exist: $fullPath")
            return
        }

        sendMessage("&aFile ready for writing: $fullPath")
    }

    /**
     * Writes or updates a key-value pair in a text file, with each pair formatted as "key=value".
     * If the key already exists in the file, its value is updated. If the key does not exist, a new
     * key-value pair is appended to the file.
     *
     * @param path The relative or absolute path to the file where the key-value pair should be written.
     * @param key The key to be written or updated in the file.
     * @param value The value associated with the key to be written or updated in the file.
     */
    private fun handleKeyValueWriteToFile(path: String, key: String, value: String) {
        val fullPath = resolvePath(path)
        val file = File(fullPath)

        if (!file.exists()) {
            sendMessage("&cFile does not exist: $fullPath")
            return
        }

        try {
            val lines = file.readLines().toMutableList()
            val separator = "="
            val newEntry = "$key$separator$value"

            val keyIndex = lines.indexOfFirst { it.startsWith("$key$separator") }
            if (keyIndex >= 0) {
                lines[keyIndex] = newEntry
            } else {
                lines.add(newEntry)
            }

            file.writeText(lines.joinToString("\n"))
            sendMessage("&aSuccessfully wrote key-value pair to file")
        } catch (e: Exception) {
            sendMessage("&cError writing to file: ${e.message}")
        }
    }

    /**
     * Displays the syntax guide for various system-related commands available in the application.
     *
     * The commands shown include:
     * - Retrieving the operating system of the server.
     * - Viewing the current user's username.
     * - Listing the contents of a specified directory.
     * - Navigating to a specific directory.
     * - Removing files or directories, with an optional "force" mode.
     * - Creating new files or directories.
     * - Writing data to a file.
     * - Manipulating key-value pairs in a specified file.
     *
     * The output is formatted and sent to the user through the provided `sender` instance.
     */
    private fun showSyntax() {
        TextBuilder("""
            ---------- &8&l[&aSystem Command Syntax&8&l]&r ----------
            &7${pwnedCraft.commandPrefix}system &aos &8&l| &7Shows the os of the server (Windows/MacOS/Linux).
            &7${pwnedCraft.commandPrefix}system &ausr &8&l| &7Shows the current user's username'.
            &7${pwnedCraft.commandPrefix}system ls &a<path to directory> &8&l| &7Lists the contents of a directory.
            &7${pwnedCraft.commandPrefix}system goto &a<path to directory> &8&l| &7Go to a directory.
            &7${pwnedCraft.commandPrefix}system rm &a<path to file/directory> &c<force?: true/false> &8&l| &7Removes a file or directory.
            &7${pwnedCraft.commandPrefix}system mk &a<file/dir> <path to file/directory> &8&l| &7Creates a new file or directory.
            &7${pwnedCraft.commandPrefix}system write &a<file> &8&l| &7Writes to a file.
            &7${pwnedCraft.commandPrefix}system kv &a<path to file> <key> <value> &8&l| &7Adds/updates a key-value pair to a file.
            &r-------------------------------------------
        """.trimIndent()).sendTo(sender, prefixOnNewLine = false)
    }

    /**
     * Sends a formatted message to the designated sender with an optional prefix placement.
     *
     * @param message The content of the message to be sent.
     * @param prefixOnNewLine Determines whether the prefix should be placed on a new line.
     * Defaults to true.
     */
    private fun sendMessage(message: String, prefixOnNewLine: Boolean = true) {
        TextBuilder(message).prefix().sendTo(sender, prefixOnNewLine = prefixOnNewLine)
    }
}