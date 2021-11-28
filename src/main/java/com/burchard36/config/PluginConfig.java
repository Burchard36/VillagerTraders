package com.burchard36.config;

import com.burchard36.Logger;
import com.burchard36.TraderVillagers;
import com.burchard36.json.JsonDataFile;
import com.google.gson.annotations.SerializedName;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Villager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class PluginConfig extends JsonDataFile {

    @SerializedName(value = "traders")
    public List<VillagerTraderJson> villagerTraders;

    public PluginConfig(JavaPlugin plugin, String pathToFile) {
        super(plugin, pathToFile);

        this.villagerTraders = new ArrayList<>();
        this.villagerTraders.add(new VillagerTraderJson(0, Villager.Profession.MASON, new ArrayList<>()));
    }

    public final VillagerTraderJson getTraderJsonByEntity(final Entity entity) {
        final NPC villagerNpc = CitizensAPI.getNPCRegistry().getNPC(entity);
        if (villagerNpc == null) {
            Logger.debug("Ignoring villager trade because it was not a CitizensNPC", TraderVillagers.INSTANCE);
            return null;
        }
        VillagerTraderJson json = null;
        for (final VillagerTraderJson traderJson : this.villagerTraders) {
            if (traderJson.npcId == villagerNpc.getId()) json = traderJson;
        }
        return json;
    }
}
