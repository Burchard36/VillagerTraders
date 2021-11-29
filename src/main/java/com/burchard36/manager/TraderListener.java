package com.burchard36.manager;

import com.burchard36.Logger;
import com.burchard36.TraderVillagers;
import com.burchard36.config.JsonItemStack;
import com.burchard36.config.JsonTradeOption;
import com.burchard36.config.PluginConfig;
import com.burchard36.config.VillagerTraderJson;
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
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.*;

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
                && selectedItem != null) return;
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
    }

    @EventHandler(priority = EventPriority.HIGH)
    public final void onInvOpen(final InventoryOpenEvent event) {
        if (!(event.getInventory() instanceof MerchantInventory)) {
            Logger.debug("Returning onInvOpen because Inventory is not MerchantInventory", TraderVillagers.INSTANCE);
            return;
        }

        final MerchantInventory villagerInventory = (MerchantInventory) event.getInventory();
        int loopCount = 0;
        for (final MerchantRecipe recipe : villagerInventory.getMerchant().getRecipes()) {

            final VillagerTraderJson traderJsonConfig = this.config.getTraderJsonByEntity((Villager)villagerInventory.getHolder());
            if (traderJsonConfig == null) {
                Logger.debug("Returning onTradeClick because Trader config was null for villager NPC", TraderVillagers.INSTANCE);
                return;
            }

            final JsonTradeOption traderOptions = traderJsonConfig.tradeOptions.get(loopCount);
            final JsonItemStack firstIngredientConfig = traderOptions.cost1;
            final JsonItemStack secondIngredientConfig = traderOptions.cost2;
            final JsonItemStack resultConfig = traderOptions.result;

            if (firstIngredientConfig == null) {
                Logger.error("Config for first JsonItemStack recipe was null, please double check your VillagerTrade configs!");
                return;
            }

            final ItemStack firstIngredient = firstIngredientConfig.getItemStack();
            if (firstIngredient == null) {
                Logger.error("ItemStack for First Ingredient was null! Please make sure this ItemStack recipe is configured correctly!");
                return;
            }

            MerchantRecipe newRecipe = null;
            ItemStack result = null;
            if (resultConfig.isMmoItem()) {
                result = resultConfig.getMmoItem();
                if (result == null) Logger.error("MMOItem with ID: " + resultConfig.mmoItemId + " and type: " + resultConfig.mmoItemType + " for VillagerTrades Result");
            } else if (resultConfig.isCustomItem()) {
                result = resultConfig.getCustomItem();
                if (result == null) Logger.error("CustomItem with ID: " + resultConfig.customItemsId + " Could not be found for VillagerTrades Result");
            } else result = resultConfig.getItemStack();

            if (result == null) {
                Logger.error("ItemStack for VillagerTrades Result was null when reached! Did you just ignore the errors up above or did you forget to specify a material in the configuration?");
                return;
            }

            newRecipe = new MerchantRecipe(result, Integer.MAX_VALUE);
            this.handleTradeOptions(newRecipe, traderOptions);
            this.handleIngredient(newRecipe, firstIngredientConfig, firstIngredient);
            if (secondIngredientConfig != null) this.handleIngredient(newRecipe, secondIngredientConfig, secondIngredientConfig.getItemStack());

            villagerInventory.getMerchant().setRecipe(loopCount, newRecipe);
            loopCount++;
        }
    }

    private void removeTradeItems(final InventoryClickEvent event,
                                  final MerchantRecipe currentTrade) {
        event.getInventory().removeItem(currentTrade.getIngredients().get(0));
        if (currentTrade.getIngredients().get(1) != null) event.getInventory().removeItem(currentTrade.getIngredients().get(1));
    }

    private void handleTradeOptions(final MerchantRecipe recipe,
                                    final JsonTradeOption tradeOptions) {
        if (tradeOptions.maxUses != null) {
            recipe.setMaxUses(tradeOptions.maxUses);

            recipe.setUses(recipe.getMaxUses());
        }
    }

    private void handleIngredient(final MerchantRecipe recipe,
                                  final JsonItemStack stackConfig,
                                  final ItemStack ingredientItemStack) {
        if (recipe == null) return;
            if (stackConfig.isMmoItem()) {
                final ItemStack mmoItem = stackConfig.getMmoItem();
                if (mmoItem == null) {
                    Logger.error("MMOItem with ID: " + stackConfig.mmoItemId + " and type: " + stackConfig.mmoItemType + " for VillagerTrades Ingredient");
                    return;
                }
                recipe.addIngredient(mmoItem);
            } else if (stackConfig.isCustomItem()) {
                final ItemStack customItem = stackConfig.getCustomItem();
                if (customItem == null) {
                    Logger.error("CustomItem with ID: " + stackConfig.customItemsId + " Could not be found for VillagerTrades Result");
                    return;
                }
                recipe.addIngredient(customItem);
            } else {
                recipe.addIngredient(ingredientItemStack);
            }
    }
}
