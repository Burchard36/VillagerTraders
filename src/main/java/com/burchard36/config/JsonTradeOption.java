package com.burchard36.config;

import com.burchard36.inventory.ItemWrapper;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class JsonTradeOption {

    @Expose
    @SerializedName(value = "cost_one")
    public JsonItemStack cost1;

    @Expose
    @SerializedName(value = "cost_two")
    public JsonItemStack cost2;

    @Expose
    @SerializedName(value = "result")
    public JsonItemStack result;

    @SerializedName(value = "max_trade_uses")
    public Integer maxUses;

    @SerializedName(value = "give_result_item")
    public boolean giveItem;

    @SerializedName(value = "commands_to_execute")
    public List<String> commandsToExecute;

    public JsonTradeOption(final ItemWrapper cost1,
                           final @Nullable ItemWrapper cost2,
                           final ItemWrapper result) {
        this.cost1 = new JsonItemStack(cost1);
        this.cost2 = new JsonItemStack(cost2);
        this.result = new JsonItemStack(result);
        this.result.mmoItemType = "SWORD";
        this.result.mmoItemId = "STARTER_SWORD";
        this.cost1.customItemsId = "TradeTest";

        this.maxUses = null;
        this.giveItem = true;
        this.commandsToExecute = new ArrayList<>();
        this.commandsToExecute.add("say hi %player%");
    }

    public JsonTradeOption() {}
}
