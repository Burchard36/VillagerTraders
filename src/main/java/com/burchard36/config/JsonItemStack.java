package com.burchard36.config;

import com.burchard36.Logger;
import com.burchard36.inventory.ItemWrapper;
import com.google.gson.annotations.SerializedName;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonItemStack {

    @SerializedName(value = "name")
    public String name;

    @SerializedName(value = "material")
    public String material;

    @SerializedName(value = "custom_items_id")
    public String customItemsId;

    @SerializedName(value = "mmo_item_id")
    public String mmoItemId;

    @SerializedName(value = "mmo_item_type")
    public String mmoItemType;

    @SerializedName(value = "lore")
    public List<String> lore;

    @SerializedName(value = "enchantments")
    public Map<Enchantment, Integer> enchantments;

    @SerializedName(value = "commands_to_execute")
    public String commandsToExecute;

    @SerializedName(value = "give_result_item")
    public boolean giveItem;

    public JsonItemStack(final ItemWrapper wrapper) {
        if (wrapper == null) return;
        this.name = wrapper.getDisplayName();
        this.material = wrapper.getItemStack().getType().name();
        this.lore = wrapper.getLore();
        this.enchantments = wrapper.getItemStack().getEnchantments();
        this.commandsToExecute = null;
        this.giveItem = true;
        this.customItemsId = null;
        this.mmoItemId = null;
        this.mmoItemType = null;
    }

    public final ItemStack getItemStack() {
        final Material stackMaterial = Material.getMaterial(this.material);
        if (stackMaterial == null) {
            Logger.error("Material was null when check: " + this.material);
            return null;
        }
        final ItemStack stack = new ItemStack(stackMaterial);
        final ItemWrapper wrapper = new ItemWrapper(stack);
        if (this.name != null) wrapper.setDisplayName(this.name);
        if (this.lore != null) wrapper.setItemLore(this.lore);
        if (this.enchantments != null) wrapper.addEnchantments((HashMap<Enchantment, Integer>)this.enchantments);
        //if (this.commandsToExecute != null) wrapper.addDataString("villager_command", this.commandsToExecute);
        return wrapper.getItemStack();
    }

    public final boolean isMmoItem() {
        return this.mmoItemId != null && this.mmoItemType != null;
    }

    public final ItemStack getMmoItem() {
        return MMOItems.plugin.getItem(Type.get(this.mmoItemType), this.mmoItemId);
    }

    public final boolean isCustomItem() {
        return this.customItemsId != null;
    }
}
