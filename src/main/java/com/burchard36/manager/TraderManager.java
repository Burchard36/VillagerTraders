package com.burchard36.manager;

import com.burchard36.Logger;
import com.burchard36.TraderVillagers;
import com.burchard36.config.VillagerTraderJson;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Material;
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
            if (traderJson.npcId != npcId) continue;
            Logger.log("Loading custom recipes for NPC with ID: " + traderJson.npcId);
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
            villager.setProfession(traderJson.getNpcProfession());
            Logger.log("Successfully set profession of VillagerNPC: " + traderJson.villagerType);
            if (!traderNpc.data().has("villager-trades")) traderNpc.data().set("villager-trades", false);

            villager.setRecipes(new ArrayList<>());
            final MerchantRecipe recipe = new MerchantRecipe(new ItemStack(Material.DIRT), 1);
            recipe.addIngredient(new ItemStack(Material.DIRT));
            final List<MerchantRecipe> recipes = new ArrayList<>(villager.getRecipes());
            recipes.add(recipe);
            villager.setRecipes(recipes); // We physically load trades when the inventory gets opened.
        }
    }

    public final TraderVillagers getPlugin() {
        return this.plugin;
    }
}
