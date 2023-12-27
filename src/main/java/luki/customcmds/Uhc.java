package luki.customcmds;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Collections;


public class Uhc implements CommandExecutor {

    public static BukkitTask task;
    int count;


    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender.isOp()) {
            if (cmd.getName().equalsIgnoreCase("uhc")) {

                Functions.onlinePlayers.clear();
                Functions.onlinePlayers.addAll(Bukkit.getOnlinePlayers());

                Collections.shuffle(Functions.onlinePlayers);

                if (args.length > 0) {
                    if (args[0].equalsIgnoreCase("start")) {

                        deleteEntities();

                        for (int i = 0; i < Functions.onlinePlayers.size(); i++) {
                            Player currentPlayer = Functions.onlinePlayers.get(i);
                            //kordynaty, w których zaczynamy oglądanie mapy
                            currentPlayer.teleport(new Location(Bukkit.getWorld("uhc"), -1549, 118, 1823, 0, 0));
                            currentPlayer.sendTitle("UHC", "Powodzenia!");
                        }

                        Bukkit.getScheduler().runTaskLater(JavaPlugin.getPlugin(CustomCmds.class), new Runnable() {
                            @Override
                            public void run() {
                                //dodatkowe 30 sekund na obejrzenie mapy w trybie spectator przed rozpoczęciem gry
                                count = 30;
                                task = Bukkit.getScheduler().runTaskTimer(JavaPlugin.getPlugin(CustomCmds.class), () -> {

                                    for (int i = 0; i < Functions.onlinePlayers.size(); i++) {
                                        Player currentPlayer = Functions.onlinePlayers.get(i);
                                        currentPlayer.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.GREEN + "Obejrz mapę! Gra zaczyna się za: " + ChatColor.RED + count));
                                    }

                                    if (count == 0) {
                                        task.cancel();
                                        //zablokowanie pvp w opcjach plugina Multiverse
                                        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "mvm set pvp deny uhc");

                                        for (int i = 0; i < Functions.onlinePlayers.size(); i++) {
                                            Player currentPlayer = Functions.onlinePlayers.get(i);
                                            //kordynaty, w których rozpoczyna się gra
                                            currentPlayer.teleport(new Location(Bukkit.getWorld("uhc"), -1549, 118, 1823, 0, 0));

                                            Bukkit.getScheduler().runTaskLater(JavaPlugin.getPlugin(CustomCmds.class), new Runnable() {
                                                @Override
                                                public void run() {
                                                    currentPlayer.setGameMode(GameMode.SURVIVAL);
                                                    currentPlayer.sendTitle("START!", "", 10, 30, 10);
                                                }
                                            }, 5);
                                        }
                                    }
                                    count--;
                                }, 20, 20);
                            }
                        }, 120);

                    }

                    //ustawienie pvp na true w ustawieniach świata (Multiverse plugin)
                    else if (args[0].equalsIgnoreCase("pvp")) {
                        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "mvm set pvp allow uhc");
                        for (int i = 0; i < Functions.onlinePlayers.size(); i++) {
                            Player currentPlayer = Functions.onlinePlayers.get(i);
                            currentPlayer.sendTitle(ChatColor.RED + "PVP: WŁĄCZONE", ChatColor.GREEN + "Czas na przygotowania się skończył!", 10, 40, 10);
                        }
                    }

                    else sender.sendMessage(ChatColor.RED + "Jako argument podaj " + ChatColor.BOLD + "'start'" + ChatColor.RESET + ChatColor.RED + " lub " + ChatColor.BOLD + "'pvp'");
                }

                else sender.sendMessage(ChatColor.RED + "Jako argument podaj " + ChatColor.BOLD + "'start'" + ChatColor.RESET + ChatColor.RED + " lub " + ChatColor.BOLD + "'pvp'");
            }
        }

        else sender.sendMessage(ChatColor.RED + "Nie masz uprawnień żeby użyć tej komendy");
        return true;
    }


    //resetowanie istniejących zwierząt (na mapie znajdują się spawnery)
    public void deleteEntities() {
        for (Enderman enderman : Bukkit.getWorld("uhc").getEntitiesByClass(Enderman.class))
            enderman.remove();
        for (Horse horse : Bukkit.getWorld("uhc").getEntitiesByClass(Horse.class))
            horse.remove();
        for (Pig pig : Bukkit.getWorld("uhc").getEntitiesByClass(Pig.class))
            pig.remove();
        for (Cow cow : Bukkit.getWorld("uhc").getEntitiesByClass(Cow.class))
            cow.remove();
        for (Chicken chicken : Bukkit.getWorld("uhc").getEntitiesByClass(Chicken.class))
            chicken.remove();
        for (Sheep sheep : Bukkit.getWorld("uhc").getEntitiesByClass(Sheep.class))
            sheep.remove();
    }

}