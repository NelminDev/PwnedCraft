package dev.nelmin.minecraft.commands

import dev.nelmin.minecraft.PwnedCraft
import dev.nelmin.minecraft.builders.TextBuilder
import org.bukkit.entity.Player
import java.io.File

/**
 * The `SystemCommand` class handles system-like commands issued by a player in the context of the game.
 * It provides functionality to perform operations such as file creation, directory navigation, file removal,
 * and retrieving system information.
 *
 * @property sender The player who issued the command.
 * @property args The array of arguments passed with the command.
 * @property pwnedCraft The instance of the PwnedCraft application used for handling system paths and context.
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
     * Processes the system command by evaluating the provided arguments and executing the appropriate action.
     *
     * If no arguments are provided, it invokes `showSyntax()` to display the available syntax.
     * Otherwise, it delegates the command handling to `handleCommand()`.
     *
     * This method is a central entry point for parsing and dispatching system commands based on user input.
     */
    private fun processCommand() {
        when {
            args.isEmpty() -> showSyntax()
            else -> handleCommand()
        }
    }

    /**
     * Handles the execution of commands based on the first argument provided in the `args` list.
     *
     * This method serves as a dispatcher, interpreting the first argument to determine which
     * sub-command processing method to invoke. Each sub-command performs a specific functionality.
     *
     * Commands supported:
     * - "os": Invokes `handleOS()` to display the current operating system.
     * - "usr": Invokes `handleGetCurrentUser()` to display the current system's username.
     * - "ls": Invokes `handleListContent(path)` to list the contents of a directory. Defaults to the current directory if no path is provided.
     * - "goto": Invokes `handleGoTo(path)` to navigate to a specified directory. Warns if the required argument is missing.
     * - "rm" or "remove": Invokes `handleRemove(path, force)` to remove a file or directory. Warns if the required argument is missing.
     * - "mk": Invokes `handleMake(type, path)` to create a new file or directory. Warns if the required arguments are missing.
     * - "write": Invokes `handleWriteToFile(path)` to prepare a file for writing. Warns if the required argument is missing.
     * - "kv": Invokes `handleKeyValueWriteToFile(path, key, value)` to write a key-value pair to a file. Warns if the required arguments are missing.
     * - Any other command: Calls `showSyntax()` for guidance on command usage.
     *
     * Error handling:
     * - For commands requiring arguments, the method will send an appropriate message if arguments are missing or invalid.
     *
     * Delegates execution to respective private methods depending on the functionality needed.
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
     * Retrieves the current directory associated with the sender if available.
     * If no directory is found, returns the default directory ".".
     *
     * @return The current directory associated with the sender or "." if no directory is found.
     */
    private fun getCurrentDirectory(): String {
        return pwnedCraft.gotoDirsToPlayers
            .entries
            .find { it.value.contains(sender.uniqueId) }
            ?.key ?: "."
    }

    /**
     * Handles the process of navigating to a specified directory path by the user.
     * The method resolves the given path, validates it, updates the current directory
     * associated with the user, and notifies the user of the changes.
     *
     * @param path the directory path the user wishes to navigate to. Can be relative
     * or absolute, and supports Unix-like (`.` and `..`) as well as Windows-style paths.
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
     * Resolves the given file path into its absolute or normalized form based on the current directory context.
     *
     * The method handles both absolute and relative paths, supporting adjustments for Windows-style paths
     * and ensuring consistency in path separators.
     *
     * @param path the input path to resolve, which can be absolute or relative
     * @return the resolved and normalized file path as a String
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
     * Determines the current operating system using the system property "os.name"
     * and sends a message with the operating system information.
     */
    private fun handleOS() {
        val osName = System.getProperty("os.name")
        sendMessage("Current operating system: $osName")
    }

    /**
     * Handles the retrieval of the current system username and sends it as a message.
     *
     * This function utilizes the Java `System.getProperty` API to fetch the "user.name" property,
     * which typically corresponds to the username of the current operating system user. The retrieved
     * username is then sent as a message using the `sendMessage` method.
     */
    private fun handleGetCurrentUser() {
        val username = System.getProperty("user.name")
        sendMessage("Current system username: $username")
    }

    /**
     * Handles the listing of directory content for the specified path.
     * Checks if the given path exists and is a directory, then retrieves and categorizes its content into files and directories.
     * Sends messages displaying the contents or appropriate error messages.
     *
     * @param path The file path to the directory whose content needs to be listed, either absolute or relative.
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
     * Handles the removal of a file or directory at the specified path. If the path points to a directory
     * that is not empty, the `force` flag must be set to true to delete it.
     *
     * @param path The relative or absolute path of the file or directory to remove.
     * @param force If true, forces the removal of non-empty directories.
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
     * Handles the creation of a file or directory based on the provided type and path.
     * Valid types are "file" and "dir".
     *
     * @param type Specifies whether to create a file ("file") or a directory ("dir").
     *             If null or invalid, an error message is sent.
     * @param path The relative or absolute path of the file or directory to be created.
     *             If null or invalid, an error message is sent.
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
     * Handles the process of writing to a file by verifying if the file exists
     * at the provided path and preparing it for writing. Sends a message to indicate
     * whether the file is ready for writing or does not exist.
     *
     * @param path The path to the file that needs to be checked and prepared for writing.
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
     * Updates or adds a key-value pair in a specified file. If the key already exists in the file, its value is updated.
     * If the key does not exist, it appends the new key-value pair to the file. The file must exist in the specified path.
     *
     * @param path The relative or absolute file path where the key-value operation is performed.
     * @param key The key of the key-value pair to be written or updated in the file.
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
     * Displays the command syntax for the `system` command to the sender.
     * The syntax includes detailed instructions for various sub-commands such as:
     * - Showing the operating system.
     * - Displaying the current user's username.
     * - Listing directory contents.
     * - Navigating to a directory.
     * - Removing files or directories.
     * - Creating files or directories.
     * - Writing to files.
     * - Adding or updating key-value pairs in files.
     *
     * The displayed syntax provides a comprehensive guide to users for utilizing the `system` commands effectively.
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
     * Sends a message to the command sender, optionally adding a prefix on a new line.
     *
     * @param message The message to be sent to the sender.
     * @param prefixOnNewLine Determines whether the prefix should be added on a new line. Defaults to true.
     */
    private fun sendMessage(message: String, prefixOnNewLine: Boolean = true) {
        TextBuilder(message).prefix().sendTo(sender, prefixOnNewLine = prefixOnNewLine)
    }
}