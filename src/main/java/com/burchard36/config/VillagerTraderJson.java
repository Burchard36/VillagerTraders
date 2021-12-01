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

        JsonTradeOption optionn = new JsonTradeOption();
        JsonItemStack cost11 = new JsonItemStack(new ItemWrapper(new ItemStack(Material.GOLD_NUGGET, 1)));
        cost11.mmoItemType = "SWORD";
        cost11.mmoItemId = "STARTER_SWORD";
        JsonItemStack cost22 = new JsonItemStack(new ItemWrapper(new ItemStack(Material.SPRUCE_WOOD, 1)));
        cost22.customItemsId = "TradeTest";
        JsonItemStack result2 = new JsonItemStack(new ItemWrapper(new ItemStack(Material.GOLD_INGOT, 1)));

        optionn.cost1 = cost11;
        optionn.cost2 = cost22;
        optionn.result = result2;
        this.tradeOptions.add(optionn);
    }

    public final Villager.Profession getNpcProfession() {
        return Villager.Profession.valueOf(this.villagerType);
    }
}
