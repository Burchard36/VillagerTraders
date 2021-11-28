package com.burchard36;

import com.burchard36.config.Configs;
import com.burchard36.config.PluginConfig;
import com.burchard36.json.PluginDataMap;
import com.burchard36.json.PluginJsonWriter;
import com.burchard36.manager.TraderManager;
import net.citizensnpcs.api.event.CitizensEnableEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.event.NPCSpawnEvent;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public final class TraderVillagers extends JavaPlugin implements Listener, Api {

    public static TraderVillagers INSTANCE;
    private ApiLib lib;
    private TraderManager traderManager;

    @EventHandler
    public void onNpcSpawn(NPCSpawnEvent event) {
        INSTANCE = this;
        final NPC npc = event.getNPC();
        this.traderManager.loadNpc(npc.getId());
    }

    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(this, this);
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
}
