package com.burchard36.manager;

import com.burchard36.Logger;
import com.burchard36.TraderVillagers;
import com.burchard36.config.JsonTradeOption;
import com.burchard36.config.VillagerTraderJson;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;

import java.util.ArrayList;
import java.util.List;

public class TraderManager {

    private final TraderVillagers plugin;
    private final TraderListener listener;

    public TraderManager(final TraderVillagers plugin) {
        this.plugin = plugin;
        this.listener = new TraderListener(this);
        plugin.getServer().getPluginManager().registerEvents(this.listener, plugin);
    }

    public final void serverShutdown() {
        HandlerList.unregisterAll(this.listener);
    }

    public final void loadNpc(final int npcId) {
        for (final VillagerTraderJson traderJson : this.plugin.getPluginConfig().villagerTraders) {
            if (traderJson.npcId != npcId) return;
            NPC traderNpc = CitizensAPI.getNPCRegistry().getById(traderJson.npcId);
            if (traderNpc == null) {
                Logger.error("Error when loading NPC ad VillagerTrader: Citizens2 NPC Was null");
                return;
            }

            final Entity npcEntity = traderNpc.getEntity();
            if (npcEntity.getType() != EntityType.VILLAGER) {
                Logger.log("&aSuccessfully set NPC with ID: " + traderJson.npcId + " to VILLAGER type.");
                traderNpc.setBukkitEntityType(EntityType.VILLAGER);
            }

            final Villager villager = (Villager) traderNpc.getEntity();
            villager.setProfession(Villager.Profession.FARMER);
            traderNpc.data().set("villager-trades", false);

            villager.setRecipes(new ArrayList<>());
            int x = 0;
            final List<MerchantRecipe> recipes = new ArrayList<>(villager.getRecipes());
            for (final JsonTradeOption tradeOption : traderJson.tradeOptions) {
                final ItemStack result = tradeOption.result.getItemStack();
                if (result == null) {
                    Logger.error("ResultStack was null when checking TradeOptions for NPC: " + traderJson.npcId);
                    return;
                }
                final MerchantRecipe recipe = new MerchantRecipe(result, 99999);
                final ItemStack cost1 = tradeOption.cost1.getItemStack();
                final ItemStack cost2 = tradeOption.cost2.getItemStack();
                if (cost1 != null) {
                    recipe.addIngredient(cost1);
                }
                if (cost2 != null) {
                    recipe.addIngredient(cost2);
                }
                Logger.log("Successfully added ingrediant to recipe");
                recipes.add(recipe);
                villager.setRecipes(recipes);
                villager.setRecipe(x, recipe);
            }
        }
    }

    public final TraderVillagers getPlugin() {
        return this.plugin;
    }
}
