package luki.customcmds;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Tridents implements CommandExecutor, Listener {

    static ArrayList<Player> alivePlayers = new ArrayList<Player>();
    static boolean gameStarting = false;
    static boolean gameIsPlaying = false;

    //lokalizacje, z których gracze zaczynają grę
    Location[] locations = {
            new Location(Bukkit.getWorld("badlands"), 242, 102, 610, 92, 3),
            new Location(Bukkit.getWorld("badlands"), 201, 105, 628, -151, 18),
            new Location(Bukkit.getWorld("badlands"), 189, 94, 604, -50, -6),
            new Location(Bukkit.getWorld("badlands"), 155, 92, 613, -111, 2),
            new Location(Bukkit.getWorld("badlands"), 158, 94, 581, -36, 2),
            new Location(Bukkit.getWorld("badlands"), 224, 108, 653, 141, 12),
            new Location(Bukkit.getWorld("badlands"), 125.300, 99, 581, -65, 5),
            new Location(Bukkit.getWorld("badlands"), 205.500, 102,  583.500, 0, 12),
    };


    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender.isOp()) {
            if (cmd.getName().equalsIgnoreCase("tridents")) {

                Functions.onlinePlayers.clear();
                Functions.onlinePlayers.addAll(Bukkit.getOnlinePlayers());

                Collections.shuffle(Functions.onlinePlayers);
                Tridents.alivePlayers.clear();
                Tridents.gameStarting = true;

                for (int i = 0; i < Functions.onlinePlayers.size(); i++) {
                    //dodanie każdego gracza do tablicy żywych
                    Player currentPlayer = Functions.onlinePlayers.get(i);
                    Tridents.alivePlayers.add(currentPlayer);

                    //usunięcie wszystkich aktywnych efektów
                    for (PotionEffect effect : currentPlayer.getActivePotionEffects())
                        currentPlayer.removePotionEffect(effect.getType());

                    currentPlayer.teleport(locations[i]);
                    currentPlayer.getInventory().clear();
                    currentPlayer.setHealth(20);

                    //dodanie saturacji, aby nie tracić głodu
                    currentPlayer.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, PotionEffect.INFINITE_DURATION, 255, false, false));
                    currentPlayer.sendTitle(ChatColor.GOLD + "Elytry z trójzębami", "", 20, 30, 10);
                }

                giveItems(); //daje każdemu trójzęby oraz elytry


                Bukkit.getScheduler().runTaskLater(JavaPlugin.getPlugin(CustomCmds.class), new Runnable() {
                    @Override
                    public void run() {
                        Functions.startTimer();
                        Bukkit.getScheduler().runTaskLater(JavaPlugin.getPlugin(CustomCmds.class), new Runnable() {
                            @Override
                            public void run() {
                                Tridents.gameIsPlaying = true;
                                Tridents.gameStarting = false;
                            }
                        }, 120);
                    }
                }, 50);

            }
        }
        else sender.sendMessage(ChatColor.RED + "Nie masz uprawnień żeby użyć tej komendy");
        return true;
    }


    @EventHandler
    public void checkDeathCount(PlayerDeathEvent event) {
        if (Tridents.gameIsPlaying) {
            Player deathPlayer = event.getEntity();
            Player killer = deathPlayer.getKiller();

            //komunikaty śmierci
            if (deathPlayer.getLastDamageCause().getCause() == EntityDamageEvent.DamageCause.LAVA)
                event.setDeathMessage(ChatColor.RED + deathPlayer.getName() + ChatColor.GREEN + " wpadł do lawy");
            if (deathPlayer.getLastDamageCause().getCause() == EntityDamageEvent.DamageCause.FLY_INTO_WALL)
                event.setDeathMessage(ChatColor.RED + deathPlayer.getName() + ChatColor.GREEN + " wleciał za mocno w ścianę");
            if (deathPlayer.getLastDamageCause().getCause() == EntityDamageEvent.DamageCause.FALL)
                event.setDeathMessage(ChatColor.RED + deathPlayer.getName() + ChatColor.GREEN + " spadł na ziemię");

            if(killer != null)
                event.setDeathMessage(ChatColor.RED + killer.getName() + ChatColor.GREEN + " wleciał za mocno w " + ChatColor.RED + deathPlayer.getName());

            deathPlayer.setGameMode(GameMode.SPECTATOR);

            //usunięcie umarłego gracza z tabeli żywych
            Tridents.alivePlayers.remove(deathPlayer);
            if (Tridents.alivePlayers.size() == 1) {
                Tridents.gameIsPlaying = false;
                Functions.endGame(Tridents.alivePlayers.get(0));
            }
        }
    }


    @EventHandler
    public void movementEvent(PlayerMoveEvent event)  {
        //zablokowanie poruszania graczy (w tej grze nie ma szklanych klatek, aby ułatwić start)
        if(Tridents.gameStarting) {
            Location fromLoc = event.getFrom();
            Location targetLoc = event.getTo();

            Location newLocation = fromLoc;
            newLocation.setPitch(targetLoc.getPitch());
            newLocation.setYaw(targetLoc.getYaw());

            event.getPlayer().teleport(newLocation);
        }
    }


    public void giveItems() {
        ItemStack trident = new ItemStack(Material.TRIDENT, 1);
        ItemMeta TridentMeta = trident.getItemMeta();

        TridentMeta.setDisplayName("Widelec");
        ArrayList<String> lore = new ArrayList<String>();
        lore.add("powerful widelec");
        TridentMeta.setLore(lore);

        TridentMeta.setUnbreakable(true);
        TridentMeta.addEnchant(Enchantment.RIPTIDE, 2, true);
        trident.setItemMeta(TridentMeta);

        ItemStack elytra = new ItemStack(Material.ELYTRA, 1);
        ItemMeta ElytraMeta = elytra.getItemMeta();
        ElytraMeta.setUnbreakable(true);
        elytra.setItemMeta(ElytraMeta);

        //ubiera automatycznie elytrę każdemu
        for (int i = 0; i < Functions.onlinePlayers.size(); i++) {
            Player currentPlayer = Functions.onlinePlayers.get(i);
            currentPlayer.getInventory().addItem(trident);
            currentPlayer.getInventory().setChestplate(elytra);
        }
    }
}
