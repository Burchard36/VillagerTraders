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
        final JsonTradeOption option = new JsonTradeOption(
                new ItemWrapper(new ItemStack(Material.SPRUCE_LOG, 4)),
                new ItemWrapper(new ItemStack(Material.AIR)),
                new ItemWrapper(new ItemStack(Material.GOLD_NUGGET, 8)));
        this.tradeOptions.add(option);
    }

    public final Villager.Profession getNpcProfession() {
        return Villager.Profession.valueOf(this.villagerType);
    }
}
