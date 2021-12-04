package com.burchard36;

import com.burchard36.config.Configs;
import com.burchard36.config.PluginConfig;
import com.burchard36.json.PluginDataMap;
import com.burchard36.json.PluginJsonWriter;
import com.burchard36.manager.TraderManager;
import net.Indyuce.mmoitems.MMOItems;
import net.citizensnpcs.api.event.NPCSpawnEvent;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class TraderVillagers extends JavaPlugin implements Listener, Api {

    public static TraderVillagers INSTANCE;
    private ApiLib lib;
    private TraderManager traderManager;
    private boolean mmoItemsEnabled;
    private MMOItems mmoItemsInstance;
    private boolean customItemsEnabled;

    @EventHandler
    public void onNpcSpawn(NPCSpawnEvent event) {
        INSTANCE = this;
        final NPC npc = event.getNPC();
        Logger.debug("Attempting to loading NPC with ID: " + npc.getId(), this);
        this.traderManager.loadNpc(npc.getId());
    }

    @Override
    public void onEnable() {
        /* Enable or Disable MMOItems hook */
        Bukkit.getPluginManager().registerEvents(this, this);
        if (!Bukkit.getPluginManager().isPluginEnabled("MMOItems")) {
            this.mmoItemsEnabled = false;
            this.mmoItemsInstance = null;
            Logger.log("MMOItems not enabled, disabling MMOItems hook");
        } else {
            this.mmoItemsEnabled = true;
            this.mmoItemsInstance = (MMOItems) Bukkit.getPluginManager().getPlugin("MMOItems");
            Logger.log("MMOItems was detected running on this server! Hook was enabled!");
        }

        if (!Bukkit.getPluginManager().isPluginEnabled("Customitems")) {
            this.customItemsEnabled = false;
        } else {
            this.customItemsEnabled = true;
            Logger.log("CustomItems was detected running on this server! Hook was enabled!");
        }

        PluginConfig defaultConfig = new PluginConfig(this, "config.json");
        this.lib = new ApiLib().initializeApi(this);
        final PluginJsonWriter writer = this.lib.getPluginDataManager().getJsonWriter();
        this.lib.getPluginDataManager().registerPluginMap(Configs.DEFAULT, new PluginDataMap(writer));

        this.lib.getPluginDataManager().loadDataFileToMap(Configs.DEFAULT, "default_config", defaultConfig);
        this.traderManager = new TraderManager(TraderVillagers.this);
    }

    @Override
    public void onDisable() {
        this.traderManager.serverShutdown();
    }

    public PluginConfig getPluginConfig() {
        return (PluginConfig)
                this.lib.getPluginDataManager().getDataFileFromMap(Configs.DEFAULT, "default_config");
    }

    @Override
    public boolean isDebug() {
        return true;
    }

    public boolean isMmoItemsEnabled() {
        return this.mmoItemsEnabled;
    }

    public MMOItems getMmoItemsInstance() {
        return this.mmoItemsInstance;
    }

    public boolean isCustomItemsEnabled() {
        return this.customItemsEnabled;
    }
}
