package com.burchard36.lib;

import com.burchard36.Logger;
import com.burchard36.TraderVillagers;
import com.burchard36.config.JsonItemStack;
import com.burchard36.config.JsonTradeOption;
import com.burchard36.config.VillagerTraderJson;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.MerchantInventory;
import org.bukkit.inventory.MerchantRecipe;

import java.util.ArrayList;
import java.util.List;

public class MerchantHelper {

    public static void testLoadMerchantTrades(final MerchantInventory merchantInventory) {
        final Merchant merchant = merchantInventory.getMerchant();
        merchant.setRecipes(new ArrayList<>());
        final Villager villager = (Villager) merchantInventory.getHolder();
        final VillagerTraderJson traderJson = getTradeJson(villager);


    }

    private static VillagerTraderJson getTradeJson(final Villager villager) {
        final VillagerTraderJson json = TraderVillagers.INSTANCE.getPluginConfig().getTraderJsonByEntity(villager);
        return null;
    }

    public static void loadMerchantTrades(final MerchantInventory merchantInventory,
                                          final List<MerchantRecipe> merchantRecipes) {
        final Merchant merchant = merchantInventory.getMerchant();
        merchant.setRecipes(new ArrayList<>());
        int loopCount = 0;
        for (final MerchantRecipe recipe : merchantRecipes) {
            Logger.debug("Loading recipe for NPC. . .", TraderVillagers.INSTANCE);
            final VillagerTraderJson traderJsonConfig = TraderVillagers.INSTANCE.getPluginConfig().getTraderJsonByEntity((Villager) merchantInventory.getHolder());
            if (traderJsonConfig == null) {
                Logger.debug("Returning loadMerchantTrades because Trader config was null for villager NPC", TraderVillagers.INSTANCE);
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
                Logger.debug("Item was a MMO Item", TraderVillagers.INSTANCE);
                result = resultConfig.getMmoItem();
                if (result == null) Logger.error("MMOItem with ID: " + resultConfig.mmoItemId + " and type: " + resultConfig.mmoItemType + " for VillagerTrades Result");
            } else if (resultConfig.isCustomItem()) {
                Logger.debug("Item was a Custom Item", TraderVillagers.INSTANCE);
                result = resultConfig.getCustomItem();
                if (result == null) Logger.error("CustomItem with ID: " + resultConfig.customItemsId + " Could not be found for VillagerTrades Result");
            } else {
                Logger.debug("Item was a Vanilla Item", TraderVillagers.INSTANCE);
                result = resultConfig.getItemStack();
            }

            if (result == null) {
                Logger.error("ItemStack for VillagerTrades Result was null when reached! Did you just ignore the errors up above or did you forget to specify a material in the configuration?");
                return;
            }

            newRecipe = new MerchantRecipe(result, Integer.MAX_VALUE);
            handleTradeOptions(newRecipe, traderOptions);
            handleIngredient(newRecipe, firstIngredientConfig, firstIngredient);
            if (secondIngredientConfig != null) handleIngredient(newRecipe, secondIngredientConfig, secondIngredientConfig.getItemStack());

            final List<MerchantRecipe> newRecipes = new ArrayList<>(merchant.getRecipes());
            newRecipes.add(newRecipe);
            merchant.setRecipes(newRecipes);
            int x = 0;
            for (final MerchantRecipe r : merchant.getRecipes()) {
                Logger.debug("Set recipe at slot: " + x, TraderVillagers.INSTANCE);
                merchant.setRecipe(x, r);
                x++;
            }
            loopCount++;
        }
    }

    private static void handleTradeOptions(final MerchantRecipe recipe,
                                    final JsonTradeOption tradeOptions) {
        if (tradeOptions.maxUses != null) {
            recipe.setMaxUses(tradeOptions.maxUses);

            recipe.setUses(recipe.getMaxUses());
        }
    }

    private static void handleIngredient(final MerchantRecipe recipe,
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
