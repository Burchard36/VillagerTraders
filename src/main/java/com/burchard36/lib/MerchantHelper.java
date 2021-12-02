package com.burchard36.lib;

import com.burchard36.Logger;
import com.burchard36.TraderVillagers;
import com.burchard36.config.JsonItemStack;
import com.burchard36.config.JsonTradeOption;
import com.burchard36.config.VillagerTraderJson;
import com.jojodmo.customitems.api.CustomItemsAPI;
import net.citizensnpcs.api.CitizensAPI;
import org.bukkit.Location;
import org.bukkit.entity.Villager;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.*;

import java.util.ArrayList;
import java.util.List;

public class MerchantHelper {

    public static void testLoadMerchantTrades(final MerchantInventory merchantInventory) {
        Logger.debug("Loading Merchant Trades. . .", TraderVillagers.INSTANCE);
        final Merchant merchant = merchantInventory.getMerchant();
        merchant.setRecipes(new ArrayList<>());
        final Villager villager = (Villager) merchantInventory.getHolder();
        if (villager == null) return;
        final VillagerTraderJson traderJson = getTradeJson(villager);
        final List<JsonTradeOption> traderOptions = traderJson.tradeOptions;

        for (final JsonTradeOption option : traderOptions) {
            Logger.debug("Loading Individual Merchant Trade. . .", TraderVillagers.INSTANCE);
            final List<MerchantRecipe> currentRecipes = new ArrayList<>(merchant.getRecipes());

            if (option.cost1 == null) throw new RuntimeException("One of your configuration options for Citizens2 NPCS with ID: " + traderJson.npcId + " Is missing a first ingredient at slot 0 (The first ingredient) This is a fatal error! Please review your configurations");
            if (option.result == null) throw new RuntimeException("One of your configuration options for Citizens2 NPCS with ID: " + traderJson.npcId + " Is missing the result for the TraderJson! This is a fatal error! Please review your configurations");

            final ItemStack cost1 = handlePluginHooks(option.cost1);
            final ItemStack cost2 = handlePluginHooks(option.cost2);
            final ItemStack result = handlePluginHooks(option.result);

            final MerchantRecipe recipe = new MerchantRecipe(result, option.getMaxUses());
            recipe.addIngredient(cost1);
            if (cost2 != null) recipe.addIngredient(cost2);
            currentRecipes.add(recipe);
            merchant.setRecipes(currentRecipes);
        }

        int x = 0;
        for (final MerchantRecipe recipe : merchant.getRecipes()) {
            merchant.setRecipe(x, recipe);
            x++;
        }
    }

    public static boolean isValidTraderVillager(final Inventory inventory) {
        return (isCitizensNpc(inventory.getHolder())) && (isValidVillagerNpc(inventory));
    }

    public static boolean isCitizensNpc(final InventoryHolder inventoryHolder) {
        if (inventoryHolder == null) return false;
        return inventoryHolder instanceof Villager && CitizensAPI.getNPCRegistry().isNPC((Villager) inventoryHolder);
    }

    public static boolean isValidVillagerNpc(final Inventory inventory) {
        if (inventory == null) return false;
        if (!(inventory instanceof MerchantInventory)) {
            Logger.debug("isValidVillagerNpc returning false because the inventory provided is not a MerchantInventory, maybe a custom plugin made this Inventory?", TraderVillagers.INSTANCE);
            return false;
        }


        if (!(inventory.getHolder() instanceof Villager)) {
            Logger.debug("isValidVillagerNpc returning false because that's not a villager! (Or the inventory opened was created by a different custom plugin)", TraderVillagers.INSTANCE);
            return false;
        }

        return true;
    }

    /**
     * Checks if an InventoryClickEvent may pass to handle the JsonTradeOptions
     * @param event InventoryClickEvent to check
     * @return true if event may pass, false if not.
     */
    public static boolean validateClickEvent(final InventoryClickEvent event) {
        if (event.isCancelled()) {
            Logger.debug("Cancelling onTradeFinish because event was finished", TraderVillagers.INSTANCE);
            return false;
        }

        if (event.getCurrentItem() == null) {
            Logger.debug("Canceling onTradeFinish because current item was empty", TraderVillagers.INSTANCE);
            return false;
        }

        if (event.getClick() != ClickType.LEFT) {
            Logger.debug("Cancelling onTradeFinish because the slot needed to be clicked was incorrect", TraderVillagers.INSTANCE);
            return false;
        }

        if (event.getClickedInventory() == null) {
            Logger.debug("Canceling onTradeFinish because the clicked inventory was null!", TraderVillagers.INSTANCE);
            return false;
        }

        if (!isCitizensNpc(event.getClickedInventory().getHolder())) {
            Logger.debug("Cancelling onTradeFinish because the InventoryHolder is not a CitizensNPC!! (Consider contacting a developer)", TraderVillagers.INSTANCE);
            return false;
        }

        if (event.getSlot() != 2 && !isValidVillagerNpc(event.getClickedInventory())) {
            Logger.debug("Cancelling onTradeFinish because this was a invalid VillagerNPC! Was it made by a plugin?", TraderVillagers.INSTANCE);
            return false;
        }

        if (event.getClickedInventory().getItem(2) == null) {
            Logger.debug("Canceling onTradeFinish because result slot was empty!", TraderVillagers.INSTANCE);
            return false;
        }

        return true;
    }

    /**
     * Handles plugin hooks, can return a Vanilla ItemStack, or an ItemStack provided by a supported plugin
     * @param jsonStack JsonItemStack to load
     * @return Will never return null, however will throw a RuntimeError when loading.
     */
    private static ItemStack handlePluginHooks(final JsonItemStack jsonStack) {
        if (jsonStack == null) return null;

        ItemStack stack = null;
        /* The double check is a failsafe in case someone puts both a
         * MMOItems and a CustomItems field on one items
         */
        if (jsonStack.isCustomItem() && !jsonStack.isMmoItem()) {
            stack = jsonStack.getCustomItem();
            if (stack == null) throw new RuntimeException("CustomItems ingredient for VillagerTrade does not exist! Please review your configurations!");
        } else if (jsonStack.isMmoItem() && !jsonStack.isCustomItem()) {
            stack = jsonStack.getMmoItem();
            if (stack == null) throw new RuntimeException("MMOItems Ingredient for VillagerTrade does not exist! Please review your configurations!");
        } else stack = jsonStack.getItemStack();

        if (stack == null) throw new RuntimeException("Ingredient for VillagerTrade couldnt load, unknown reason (Did you specify an item material? This item was detected as a vanilla item!)");

        return stack;
    }

    /**
     * Gets the VillagerTraderJson safely for a Villager entity
     * @param villager Villager to get VillagerTraderJson of
     * @return VillagerTraderJson if Villager is a Citizens2 NPC, new RuntimeException is thrown when not, this is a fatal error
     */
    private static VillagerTraderJson getTradeJson(final Villager villager) {
        final VillagerTraderJson json = TraderVillagers.INSTANCE.getPluginConfig().getTraderJsonByEntity(villager);
        if (json == null) {
            final Location loc = villager.getLocation();
            throw new RuntimeException("Cannot load VillagerTraderJson for villager at location X: " + loc.getX() + " Y: " + loc.getY() + " Z: " + loc.getZ() + " In World: " + loc.getWorld().getName());
        } else return json;
    }
}
