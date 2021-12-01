package com.burchard36.manager;

import com.burchard36.Logger;
import com.burchard36.TraderVillagers;
import com.burchard36.config.JsonItemStack;
import com.burchard36.config.JsonTradeOption;
import com.burchard36.config.PluginConfig;
import com.burchard36.config.VillagerTraderJson;
import com.burchard36.lib.MerchantHelper;
import io.papermc.paper.event.player.PlayerTradeEvent;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.AbstractVillager;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.VillagerReplenishTradeEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.*;

import java.util.ArrayList;
import java.util.List;

public class TraderListener implements Listener {

    private final PluginConfig config;

    public TraderListener(final TraderManager manager) {
        this.config = manager.getPlugin().getPluginConfig();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public final void onTradeFinish(final InventoryClickEvent event) {
        final ItemStack selectedItem = event.getCurrentItem();
        if (event.getSlot() != 2
                && !event.isCancelled()
                && selectedItem != null
                && event.getClick() == ClickType.SHIFT_LEFT) return;
        final Player tradingPlayer = (Player) event.getWhoClicked();
        if (!(event.getClickedInventory() instanceof MerchantInventory)) {
            Logger.debug("Ignoring onTradeFinish because AbstractVillager's Inventory is not a MerchantInventory", TraderVillagers.INSTANCE);
            return;
        }

        final MerchantInventory villagerInventory = (MerchantInventory) event.getClickedInventory();
        final Villager villager = (Villager) villagerInventory.getHolder();
        final NPC npc = CitizensAPI.getNPCRegistry().getNPC(villager);
        if (npc == null) {
            Logger.debug("Returning PlayerTradeEvent because NPC was null when checked", TraderVillagers.INSTANCE);
            return;
        }

        final MerchantRecipe currentTrade = villagerInventory.getSelectedRecipe();
        final VillagerTraderJson tradeOptions = this.config.getTraderJsonByEntity(villager);
        if (tradeOptions == null) {
            Logger.debug("Returning onTradeFinish because VillagerTraderJson was null for this NPC!", TraderVillagers.INSTANCE);
            return;
        }

        final int selectedRecipeIndex = villagerInventory.getSelectedRecipeIndex();
        final JsonTradeOption tradeSettings = tradeOptions.tradeOptions.get(selectedRecipeIndex);
        if (tradeSettings == null) {
            Logger.debug("Returning onTradeFinish because JsonTradeOption was null for this NPC!", TraderVillagers.INSTANCE);
            return;
        }

        if (tradeSettings.commandsToExecute != null) {
            Logger.log("Commands werent null!");
            final ConsoleCommandSender console = Bukkit.getConsoleSender();
            tradeSettings.commandsToExecute.forEach((command) -> {
                command = command.replace("%player%", tradingPlayer.getName());
                Bukkit.dispatchCommand(console, command);
            });
        }

        if (currentTrade != null) {
            event.setCancelled(true);
            this.removeTradeItems(event, currentTrade);
            if (tradeSettings.giveItem) tradingPlayer.getInventory().addItem(selectedItem);
        }

        MerchantHelper.loadMerchantTrades(villagerInventory,
                new ArrayList<>(villagerInventory.getMerchant().getRecipes())); // new array list so original instance isnt disturbed
    }

    @EventHandler(priority = EventPriority.HIGH)
    public final void onInvOpen(final InventoryOpenEvent event) {
        if (!(event.getInventory() instanceof MerchantInventory)) {
            Logger.debug("Returning onInvOpen because Inventory is not MerchantInventory", TraderVillagers.INSTANCE);
            return;
        }

        final MerchantInventory villagerInventory = (MerchantInventory) event.getInventory();
        MerchantHelper.loadMerchantTrades(villagerInventory,
                new ArrayList<>(villagerInventory.getMerchant().getRecipes())); // new array list so original instance isnt disturbed
    }

    private void removeTradeItems(final InventoryClickEvent event,
                                  final MerchantRecipe currentTrade) {
        event.getInventory().removeItem(currentTrade.getIngredients().get(0));
        if (currentTrade.getIngredients().get(1) != null) event.getInventory().removeItem(currentTrade.getIngredients().get(1));
    }
}
