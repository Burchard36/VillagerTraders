package com.burchard36.manager;

import com.burchard36.Logger;
import com.burchard36.TraderVillagers;
import com.burchard36.config.JsonTradeOption;
import com.burchard36.config.PluginConfig;
import com.burchard36.config.VillagerTraderJson;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TraderListener implements Listener {

    private final PluginConfig config;

    public TraderListener(final TraderManager manager) {
        this.config = manager.getPlugin().getPluginConfig();
    }

    @EventHandler
    public void onTradeClick(final InventoryClickEvent event) {
        Logger.debug("Holder: " + event.getInventory().getHolder(), TraderVillagers.INSTANCE);
        if (!(event.getInventory() instanceof MerchantInventory)) {
            Logger.debug("Returning onTradeClick because Inventory is not MerchantInventory", TraderVillagers.INSTANCE);
            return;
        }
        final MerchantInventory villagerInventory = (MerchantInventory) event.getInventory();
        final VillagerTraderJson traderJsonConfig = this.config.getTraderJsonByEntity(villagerInventory.getMerchant().getTrader());
        if (traderJsonConfig == null) {
            Logger.debug("Returning onTradeClick because Trader config was null for villager NPC", TraderVillagers.INSTANCE);
            return;
        }

        final MerchantRecipe selectedRecipe = villagerInventory.getSelectedRecipe();
        if (selectedRecipe == null) {
            Logger.debug("Returning onTradeClick because selected MerchantRecipe was null when checked", TraderVillagers.INSTANCE);
            return;
        }

        final HashMap<Integer, ItemStack> recipeIngredients = this.getVillagerTradeIngredients(selectedRecipe);
        final ItemStack firstIngredient = recipeIngredients.get(0);
        final ItemStack secondIngredient = recipeIngredients.get(1); // Might be null!!
        ItemStack resultItem = selectedRecipe.getResult();

        final JsonTradeOption traderOptions = traderJsonConfig.tradeOptions.get(villagerInventory.getSelectedRecipeIndex());


    }

    private HashMap<Integer, ItemStack> getVillagerTradeIngredients(final MerchantRecipe recipe) {
        final HashMap<Integer, ItemStack> ingredients = new HashMap<>();
        int x = 0;
        for (final ItemStack ingredient : recipe.getIngredients()) {
            ingredients.put(x, ingredient);
            x++;
        }
        return ingredients;
    }

    private List<ItemStack> getAllOf(final Player player, final Material material) {
        return new ArrayList<>(player.getInventory().all(material).values());
    }
}
