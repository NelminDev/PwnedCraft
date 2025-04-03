package dev.nelmin.minecraft.builders

import dev.nelmin.minecraft.PwnedCraft
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.SkullMeta
import java.util.*

/**
 * A utility class for constructing and modifying item stacks in Minecraft.
 * Provides methods to customize the item stack's metadata such as display name,
 * lore, enchantments, item flags, and other customizable properties.
 *
 * @constructor Creates an instance of `ItemBuilder` with a specified material and amount.
 *
 * @param material The material type for the item stack.
 * @param amount The amount of items in the stack. Default is 1.
 */
open class ItemBuilder(material: Material, amount: Int = 1) {
    /**
     * Represents the core `ItemStack` object used for managing the item properties and metadata
     * in the `ItemBuilder` class and its subclasses.
     *
     * This variable is initialized with a specified material and amount, enabling the creation
     * and manipulation of custom item stacks within the Bukkit/Spigot API.
     *
     * It serves as a base for modifying attributes like display name, enchantments, lore,
     * custom model data, and other metadata through the utility methods of the `ItemBuilder`.
     */
    val itemStack: ItemStack = ItemStack(material, amount)
    /**
     * Represents the metadata of an `ItemStack`, allowing modification of its attributes such as
     * display name, lore, enchantments, and other properties. This property is initialized with
     * the metadata of the current `itemStack` and is used throughout the `ItemBuilder` methods
     * for item customization.
     *
     * This property is crucial for modifying and applying item-specific attributes to the
     * corresponding `ItemStack` instance.
     */
    private val itemMeta: ItemMeta = itemStack.itemMeta!!

    /**
     * Sets the display name of the item.
     *
     * @param displayName The string to set as the display name for the item.
     * @return The current instance of ItemBuilder for chaining further method calls.
     */
    fun setDisplayName(displayName: String): ItemBuilder {
        itemMeta.setDisplayName(displayName)
        return this
    }

    /**
     * Adds a single line of text to the lore of the associated item.
     *
     * @param line The line of text to be added to the item's lore.
     * @return The current instance of ItemBuilder with the updated lore.
     */
    fun addLoreLine(line: String): ItemBuilder {
        val lore = itemMeta.lore ?: mutableListOf()
        lore.add(line)
        itemMeta.lore = lore
        return this
    }

    /**
     * Sets the lore (description text) of the item being built.
     *
     * @param lore The list of strings to be used as the item's lore. Each string represents a line of text in the lore.
     * @return The current instance of `ItemBuilder` for method chaining.
     */
    fun setLore(lore: List<String>): ItemBuilder {
        itemMeta.lore = lore
        return this
    }

    /**
     * Adds an enchantment to the item with the specified level and optionally ignores level restrictions.
     *
     * @param enchantment The enchantment to be added to the item.
     * @param level The level of the enchantment to be applied.
     * @param ignoreLevelRestriction Whether to bypass the level restriction for the enchantment.
     * @return The current instance of ItemBuilder for method chaining.
     */
    fun addEnchant(enchantment: Enchantment, level: Int, ignoreLevelRestriction: Boolean): ItemBuilder {
        itemMeta.addEnchant(enchantment, level, ignoreLevelRestriction)
        return this
    }

    /**
     * Removes all enchantments from the associated item.
     *
     * @return The current instance of `ItemBuilder` for method chaining.
     */
    fun clearEnchantments(): ItemBuilder {
        itemMeta.removeEnchantments()
        return this
    }

    /**
     * Adds a visual glowing effect to the item by applying an "invisible" enchantment and hiding it from the lore.
     * This method ensures that the item has a unique glow without altering its functional properties.
     *
     * @return The current instance of ItemBuilder with the applied glowing effect.
     */
    fun glow(): ItemBuilder {
        if (itemMeta.enchants.isEmpty()) {
            itemMeta.addEnchant(Enchantment.UNBREAKING, 1, true)
            itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS)
        }
        return this
    }

    /**
     * Sets custom model data for the item represented by this `ItemBuilder`.
     *
     * Custom model data is an integer value used to differentiate variations
     * of the same item type within texture packs or other customizations.
     *
     * @param data The integer value representing the custom model data to be set.
     * @return The current `ItemBuilder` instance for chaining additional operations.
     */
    fun setCustomModelData(data: Int): ItemBuilder {
        itemMeta.setCustomModelData(data)
        return this
    }

    /**
     * Adds one or more `ItemFlag` values to the item's metadata.
     *
     * ItemFlags are used to control the display of specific item attributes in Minecraft,
     * such as hiding enchantments or unbreakable status.
     *
     * @param flags The `ItemFlag` values to be added. These can be one or more flags
     *              that determine the hidden properties of the item.
     * @return The current `ItemBuilder` instance for chaining further operations.
     */
    fun addItemFlags(vararg flags: ItemFlag): ItemBuilder {
        itemMeta.addItemFlags(*flags)
        return this
    }

    /**
     * Sets the unbreakable status of the item.
     *
     * @param unbreakable Specifies whether the item should be unbreakable.
     * @return The current instance of `ItemBuilder`, allowing for method chaining.
     */
    fun setUnbreakable(unbreakable: Boolean): ItemBuilder {
        itemMeta.isUnbreakable = unbreakable
        return this
    }

    /**
     * Finalizes the item building process by applying the configured metadata
     * to the item stack and returning the resulting item.
     *
     * @return The constructed ItemStack with the applied item metadata.
     */
    open fun toItem(): ItemStack {
        itemStack.itemMeta = itemMeta
        return itemStack
    }

    /**
     * A subclass of `ItemBuilder` that specializes in building a player head (`PLAYER_HEAD`) item.
     * This class provides methods to configure the player head's owner using various input types.
     */
    class Head : ItemBuilder(Material.PLAYER_HEAD) {
        /**
         * Represents the `SkullMeta` instance associated with the `Head`
         * item being built by the `Head` class. This provides specialized
         * metadata for player head items, allowing additional customization
         * such as assigning an owning player or setting the owner.
         *
         * This property is used in methods like `setOwner(owner: String)`,
         * `setOwner(owner: OfflinePlayer)`, and `setOwner(uuid: UUID)` to
         * manage specific properties of the player head. It is also updated
         * when finalizing the `ItemStack` instance in `toItem()`.
         */
        private val skullMeta: SkullMeta = itemStack.itemMeta as SkullMeta

        /**
         * Sets the owner of the player's head using the provided player name.
         *
         * @param owner The name of the player whose head will be set as the owner.
         * @return The current instance of the `Head` class for chaining method calls.
         * @deprecated since 1.12.1. Use `setOwner(uuid)` instead.
         */
        @Deprecated(
            "since 1.12.1",
            ReplaceWith("setOwner(uuid)", "dev.nelmin.minecraft.builders.ItemBuilder.Head.setOwner"),
            DeprecationLevel.WARNING
        )
        fun setOwner(owner: String): Head {
            skullMeta.owner = owner
            return this
        }

        /**
         * Sets the owner of the player head to the specified `OfflinePlayer`.
         *
         * @param owner The `OfflinePlayer` that will be set as the owner of the player head.
         * @return The current instance of `Head` for method chaining.
         */
        fun setOwner(owner: OfflinePlayer): Head {
            skullMeta.owningPlayer = owner
            return this
        }

        /**
         * Sets the owner of the player head using the specified UUID.
         *
         * @param uuid The UUID of the player to set as the owner of the player head.
         * @return The current instance of the Head class with the updated owner.
         */
        fun setOwner(uuid: UUID): Head {
            skullMeta.owningPlayer = PwnedCraft.instance.server.getOfflinePlayer(uuid)
            return this
        }

        /**
         * Converts the current `Head` instance into an `ItemStack` with the associated `skullMeta`.
         *
         * @return An `ItemStack` representing the head with the configured metadata.
         */
        override fun toItem(): ItemStack {
            itemStack.itemMeta = skullMeta
            return itemStack
        }
    }
}