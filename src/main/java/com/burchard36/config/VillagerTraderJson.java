package com.burchard36.config;

import com.burchard36.inventory.ItemWrapper;
import com.google.gson.annotations.SerializedName;
import org.bukkit.Material;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class VillagerTraderJson {

    @SerializedName(value = "citizens_npc_id")
    public int npcId;

    @SerializedName(value = "villager_type")
    public String villagerType;

    @SerializedName(value = "villager_trade_options")
    public List<JsonTradeOption> tradeOptions;

    public VillagerTraderJson(final int npcId,
                              final Villager.Profession villagerType,
                              final List<JsonTradeOption> tradeOptions) {
        this.npcId = npcId;
        this.villagerType = villagerType.name();
        this.tradeOptions = tradeOptions;
        JsonTradeOption option = new JsonTradeOption();
        JsonItemStack cost1 = new JsonItemStack(new ItemWrapper(new ItemStack(Material.SPRUCE_WOOD, 1)));
        JsonItemStack cost2 = new JsonItemStack(new ItemWrapper(new ItemStack(Material.SPRUCE_WOOD, 1)));
        JsonItemStack result = new JsonItemStack(new ItemWrapper(new ItemStack(Material.GOLD_NUGGET, 1)));
        cost1.customItemsId = "TradeTest";
        cost2.customItemsId = "TradeTest";
        result.mmoItemType = "SWORD";
        result.mmoItemId = "STARTER_SWORD";

        option.cost1 = cost1;
        option.cost2 = cost2;
        option.result = result;
        option.giveItem = false;
        List<String> commands = new ArrayList<>();
        commands.add("say %player% hello");
        option.commandsToExecute = commands;
        option.maxUses = Integer.MAX_VALUE;
        this.tradeOptions.add(option);

        cost1 = new JsonItemStack(new ItemWrapper(new ItemStack(Material.GOLD_NUGGET, 1)));
        cost1.mmoItemType = "SWORD";
        cost1.mmoItemId = "STARTER_SWORD";
        cost2 = new JsonItemStack(new ItemWrapper(new ItemStack(Material.SPRUCE_WOOD, 1)));
        cost2.customItemsId = "TradeTest";
        result = new JsonItemStack(new ItemWrapper(new ItemStack(Material.GOLD_INGOT, 1)));

        option.cost1 = cost1;
        option.cost2 = cost2;
        option.result = result;
        this.tradeOptions.add(option);
    }

    public final Villager.Profession getNpcProfession() {
        return Villager.Profession.valueOf(this.villagerType);
    }
}
