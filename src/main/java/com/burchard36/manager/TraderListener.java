package com.burchard36.manager;

import com.burchard36.Logger;
import com.burchard36.TraderVillagers;
import com.burchard36.config.JsonTradeOption;
import com.burchard36.config.PluginConfig;
import com.burchard36.config.VillagerTraderJson;
import com.burchard36.lib.MerchantHelper;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.*;
import org.bukkit.scheduler.BukkitRunnable;

public class TraderListener implements Listener {

    private final PluginConfig config;

    public TraderListener(final TraderManager manager) {
        this.config = manager.getPlugin().getPluginConfig();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public final void onTradeFinish(final InventoryClickEvent event) {
        if (!MerchantHelper.validateClickEvent(event)) return;
        final ItemStack selectedItem = event.getCurrentItem();
        final Player tradingPlayer = (Player) event.getWhoClicked();
        final MerchantInventory villagerInventory = (MerchantInventory) event.getClickedInventory();
        if (villagerInventory == null) throw new RuntimeException("Dude! Why is MerchantInventory null?? Did you change the Citizens2 NPC type? If not im sorry please contact a develoepr :( UwU We make a fucky wucky");
        final Villager villager = (Villager) villagerInventory.getHolder();
        if (villager == null) throw new RuntimeException("Villager was null? Why (Did you change the entity type of the Citizen2 NPC? onTradeFinish. Please contact a developer dude!! Im sorry!!");
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
            Logger.debug("Commands were found for this trade, running commands. . .", TraderVillagers.INSTANCE);
            final ConsoleCommandSender console = Bukkit.getConsoleSender();
            tradeSettings.commandsToExecute.forEach((command) -> {
                command = command.replace("%player%", tradingPlayer.getName());
                Logger.debug("Executing command: &e" + command, TraderVillagers.INSTANCE);
                Bukkit.dispatchCommand(console, command);
            });
        }

        if (currentTrade != null) {
            event.setCancelled(true);
            this.removeTradeItems(event, currentTrade);
            if (selectedItem == null) throw new RuntimeException("Dude! What happened :c There was a check for this idk what happened, please contact me!!");
            if (tradeSettings.giveItem) tradingPlayer.getInventory().addItem(selectedItem);
        }

        MerchantHelper.testLoadMerchantTrades(villagerInventory); // new array list so original instance isnt disturbed
        new BukkitRunnable() {
            @Override
            public void run() {
                if (villagerInventory.getItem(2) == null) return;
                final Villager entity = (Villager) villager.getWorld().getEntity(villager.getUniqueId());
                assert entity != null; // Come on, it seriously won't disappear a tick later...
                int index = villagerInventory.getSelectedRecipeIndex();
                MerchantRecipe recipe = entity.getRecipe(index);
                villagerInventory.setItem(2, recipe.getResult());
                tradingPlayer.updateInventory();
            }
        }.runTaskLater(TraderVillagers.INSTANCE, 1);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public final void onInvOpen(final InventoryOpenEvent event) {
        if (!MerchantHelper.isValidTraderVillager(event.getInventory())) {
            Logger.debug("Returning onInvOpen because MerchantInventory does not belong to a Citizens2 NPC!", TraderVillagers.INSTANCE);
        }

        final MerchantInventory villagerInventory = (MerchantInventory) event.getInventory();
        MerchantHelper.testLoadMerchantTrades(villagerInventory); // new array list so original instance isnt disturbed
    }

    private void removeTradeItems(final InventoryClickEvent event,
                                  final MerchantRecipe currentTrade) {
        event.getInventory().removeItem(currentTrade.getIngredients().get(0));
        if (currentTrade.getIngredients().get(1) != null) event.getInventory().removeItem(currentTrade.getIngredients().get(1));
    }
}
