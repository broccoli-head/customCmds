package luki.customcmds;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;


public final class CustomCmds extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {

        System.out.println("Plugin " + this.getClass().getSimpleName() + " wlaczono pomyslnie!");

        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new Parkour(), this);
        getServer().getPluginManager().registerEvents(new Tridents(), this);
        getServer().getPluginManager().registerEvents(new Berek(), this);

        this.getCommand("parkour").setExecutor(new Parkour());
        this.getCommand("tridents").setExecutor(new Tridents());
        this.getCommand("berek").setExecutor(new Berek());
        this.getCommand("uhc").setExecutor(new Uhc());
        this.getCommand("reset").setExecutor(new EmergencyReset());
    }


    @Override
    public void onDisable() {
        System.out.println("Plugin zostal wylaczony.");
    }


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        player.setHealth(20);

        if (!player.hasPlayedBefore())
            event.setJoinMessage(ChatColor.GREEN + "Witaj na serwerze, " + ChatColor.RED + player.getDisplayName());

        else
            event.setJoinMessage((ChatColor.GREEN + "Witaj ponownie, " + ChatColor.RED + player.getDisplayName()));
    }


    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        event.setQuitMessage(ChatColor.RED + player.getDisplayName() + ChatColor.GREEN + " opuścił serwer!");
    }

}