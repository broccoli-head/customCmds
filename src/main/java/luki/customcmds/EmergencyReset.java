package luki.customcmds;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import java.util.Collections;


public class EmergencyReset implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (sender.isOp()) {
            if (cmd.getName().equalsIgnoreCase("reset")) {
                //aktualizuje tablicę graczy online
                Functions.onlinePlayers.clear();
                for (Player player : Bukkit.getOnlinePlayers()) {
                    Functions.onlinePlayers.add(player);
                }
                Collections.shuffle(Functions.onlinePlayers);

                for (int i = 0; i < Functions.onlinePlayers.size(); i++) {
                    Player currentPlayer = Functions.onlinePlayers.get(i);
                    for (PotionEffect effect : currentPlayer.getActivePotionEffects())
                        currentPlayer.removePotionEffect(effect.getType());

                    currentPlayer.teleport(Bukkit.getWorld("lobby").getSpawnLocation());
                    currentPlayer.getInventory().clear();
                    currentPlayer.setHealth(20);
                }

                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "team empty Berek");
            }
        }

        else sender.sendMessage(ChatColor.RED + "Używaj wyłącznie, jeśli wiesz co robisz! (Tylko dla adminów)");
        return true;
    }
}

