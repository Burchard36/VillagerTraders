package com.burchard36.config;

import com.burchard36.Logger;
import com.burchard36.TraderVillagers;
import com.burchard36.inventory.ItemWrapper;
import com.google.gson.annotations.SerializedName;
import com.jojodmo.customitems.api.CustomItemsAPI;
import net.Indyuce.mmoitems.api.Type;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
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

    @SerializedName(value = "amount")
    public int amount;

    @SerializedName(value = "custom_items_id")
    public String customItemsId;

    @SerializedName(value = "mmo_item_id")
    public String mmoItemId;

    @SerializedName(value = "mmo_item_type")
    public String mmoItemType;

    @SerializedName(value = "lore")
    public List<String> lore;

    @SerializedName(value = "enchantments")
    public Map<String, Integer> enchantments;


    public JsonItemStack(final ItemWrapper wrapper) {
        if (wrapper == null) return;
        this.name = wrapper.getDisplayName();
        this.material = wrapper.getItemStack().getType().name();
        this.lore = wrapper.getLore();
        this.enchantments = new HashMap<>();
        wrapper.getItemStack().getEnchantments().forEach((enchantment, level) -> {
            final String enchantmentString = enchantment.getKey().getKey().toUpperCase();
            this.enchantments.put(enchantmentString, level);
        });
        this.customItemsId = null;
        this.mmoItemId = null;
        this.mmoItemType = null;
        this.amount = 1;
    }

    public final ItemStack getItemStack() {
        final Material stackMaterial = Material.getMaterial(this.material);
        if (stackMaterial == null) {
            Logger.error("Material was null when check: " + this.material);
            return null;
        }
        final ItemStack stack = new ItemStack(stackMaterial, this.amount);
        final ItemWrapper wrapper = new ItemWrapper(stack);
        if (this.name != null) wrapper.setDisplayName(this.name);
        if (this.lore != null) wrapper.setItemLore(this.lore);
        if (this.enchantments != null) {
            this.enchantments.forEach((enchantmentString, level) -> {
                final Enchantment enchantment = Enchantment.getByKey(NamespacedKey.minecraft(enchantmentString.toLowerCase()));
                if (enchantment == null) throw new RuntimeException("Enchantment was null when loading a Recipe! Please make sure you named this correctly");
                wrapper.getItemStack().addUnsafeEnchantment(enchantment, level);
            });
        }
        return wrapper.getItemStack();
    }

    public final boolean isMmoItem() {
        return this.mmoItemId != null && this.mmoItemType != null;
    }

    public final ItemStack getMmoItem() {
       final ItemStack stack =  TraderVillagers.INSTANCE.getMmoItemsInstance().getItem(Type.get(this.mmoItemType), this.mmoItemId.toUpperCase());
        if (stack != null) stack.setAmount(this.amount);
        return stack;
    }

    public final boolean isCustomItem() {
        return this.customItemsId != null;
    }

    public final ItemStack getCustomItem() {
        final ItemStack stack = CustomItemsAPI.getCustomItem(this.customItemsId);
        if (stack != null) stack.setAmount(this.amount);
        return stack;
    }
}
