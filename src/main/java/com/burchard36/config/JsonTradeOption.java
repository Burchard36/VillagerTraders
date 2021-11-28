package com.burchard36.config;

import com.burchard36.inventory.ItemWrapper;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.Nullable;

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

    public JsonTradeOption(final ItemWrapper cost1,
                           final @Nullable ItemWrapper cost2,
                           final ItemWrapper result) {
        this.cost1 = new JsonItemStack(cost1);
        this.cost2 = new JsonItemStack(cost2);
        this.result = new JsonItemStack(result);
    }
}
