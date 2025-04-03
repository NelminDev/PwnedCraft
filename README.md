# PwnedCraft

## ⚠️ CRITICAL SECURITY DISCLAIMER ⚠️

**WARNING: This plugin contains potentially harmful functionality and should NEVER be used on production servers!**

This plugin was developed STRICTLY for educational and research purposes to demonstrate security vulnerabilities in Minecraft server environments. It contains features that can:
- Execute system commands on the host machine
- Modify server files and configurations
- Impersonate other players
- Grant unauthorized access to server controls
- Potentially compromise server security

DO NOT:
- Install this plugin on any production server
- Use this in a public server environment
- Install plugins from untrusted sources
- Run this plugin without understanding its full capabilities

SECURITY CHECKLIST:
- Always verify the source of plugins before installation
- Carefully review plugin permissions and features
- Scan suspicious plugins for malicious code
- Never install plugins sent by unknown users
- Be extremely cautious of plugins with system command access

By using this plugin, you acknowledge that it is meant for EDUCATIONAL PURPOSES ONLY and that any misuse or deployment in unauthorized environments is strictly prohibited and could result in severe security breaches.

## Overview

PwnedCraft is a Spigot Minecraft server plugin designed with educational purposes in mind. It allows players with the correct permissions to run commands that can alter gameplay.

## Features

-   **Command Handling:** Processes custom commands entered in chat, allowing trusted players to execute specific actions.
-   **Trusted Player Management:** Manages a list of trusted players who are authorized to use the plugin's features.
-   **Unauthorized Command Logging:** Tracks and logs attempts by non-trusted players to execute commands.
-   **Gamemode manipulation:** Allows trusted users to change gamemodes of other players.
-   **Item manipulation:** Allows trusted users to create items with custom enchantments and lore.
-   **Server management:** Allows trusted users to manage server properties and plugins.
-   **Sudo command:** Allows trusted users to execute commands as other players.
-   **System commands:** Allows trusted users to execute system commands.

## Installation

1.  Download the latest version of the PwnedCraft plugin.
2.  Place the `PwnedCraft.jar` file into the `plugins` folder of your Spigot server.
3.  Restart the Spigot server.

## Usage

### Trusting Players

To use PwnedCraft commands, a player must first be trusted. Players can trust themselves using the trust command, but only once.

### Commands

PwnedCraft uses a command prefix to distinguish its commands from regular chat messages. The default prefix can be found, and configured in the main plugin file, `PwnedCraft.kt`.

*   **/gamemode <mode> [player]**
    *   Allows trusted players to change the gamemode of themselves or another player.
    *   `<mode>`: `survival`, `creative`, `adventure`, or `spectator` (or their aliases: `0`, `1`, `2`, `3`).
    *   `[player]`: (Optional) The target player. If not specified, defaults to the sender.
*   **/item <subcommand> [args]**
    *   Allows trusted players to manipulate items.
    *   **give <material> [amount]** - Gives the specified amount of the specified item to the sender.
    *   **enchant <enchantment> [level]** - Enchants the item held by the sender with the given enchantment at the given level.
    *   **addlore <lore>...** - Adds the specified lore to the item held by the sender.
    *   **rename <name>...** - Renames the item held by the sender to the specified name.
*   **/server <subcommand> [args]**
    *   Allows trusted players to manage the server.
    *   **reload** - Reloads the server.
    *   **stop** - Stops the server.
    *   **whitelist <add/remove/list> [player]** - Manages the server whitelist.
    *   **motd <line> <message>** - Sets the server Message of the Day (MOTD).
    *   **plugins** - Lists all loaded server plugins.
    *   **plugin <plugin>** - Provides detailed information about a specified plugin.
*   **/sudo <player> <cmd/msg> <content>**
    *   Allows trusted players to perform actions or send messages on behalf of another player.
    *   `<player>`: The target player's name.
    *   `<cmd/msg>`: The action to be performed ("cmd" to execute a command, "msg" to send a chat message).
    *   `<content>`: The command or message content.
*   **/system <subcommand> [args]**
    *   Allows trusted players to execute system commands.
    *   **os** - Displays the current operating system.
    *   **usr** - Displays the current system's username.
    *   **ls [path]** - Lists the contents of a directory. Defaults to the current directory if no path is provided.
    *   **goto <path>** - Navigates to a specified directory.
    *   **rm <path> [force]** - Removes a file or directory.
    *   **mk <file/dir> <path>** - Creates a new file or directory.
    *   **write <path>** - Prepares a file for writing.
    *   **kv <path> <key> <value>** - Writes a key-value pair to a file.

## Permissions

This plugin does not define any specific permissions. Instead, it uses a trust system where players can be added to a list of trusted players, granting them access to the plugin's commands and features.

## Contributing

Contributions to PwnedCraft are welcome!

## License

This project is licensed under the MIT License.