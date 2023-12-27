package luki.customcmds;

import org.bukkit.*;
import org.bukkit.block.Block;
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

import java.util.*;

public class Parkour implements CommandExecutor, Listener {

    static boolean gameIsPlaying = false;
    static ArrayList<Player> alivePlayers = new ArrayList<Player>();

    //lokalizacje, w których mają powstać szklane klatki, w których rodzą się gracze
    Location[] locations = {
            new Location(Bukkit.getWorld("parkour"), 14.5, 11, 30.5, 160, 10),
            new Location(Bukkit.getWorld("parkour"), -1.5, 11, -8.5, -30, 10),
            new Location(Bukkit.getWorld("parkour"), 25.5, 11, 3.5, 70, 10),
            new Location(Bukkit.getWorld("parkour"), 0.5, 11, 33.5, -150, 10),
            new Location(Bukkit.getWorld("parkour"), 24.5, 11, 20.5, 115, 10),
            new Location(Bukkit.getWorld("parkour"), 18.5, 11, -9.5, 35, 10),
            new Location(Bukkit.getWorld("parkour"), -10.5, 11, 2.5, -75, 10),
            new Location(Bukkit.getWorld("parkour"), 8.5, 11, -7.5, 3, 10),
            new Location(Bukkit.getWorld("parkour"), -3.5, 11, 19.5, -100, 10)
    };


    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender.isOp()) {
            if (cmd.getName().equalsIgnoreCase("parkour")) {

                Functions.onlinePlayers.clear();
                Functions.onlinePlayers.addAll(Bukkit.getOnlinePlayers());

                Collections.shuffle(Functions.onlinePlayers);
                Parkour.alivePlayers.clear();

                for (int i = 0; i < Functions.onlinePlayers.size(); i++) {
                    //buduje wokół każdego gracza szklaną klatkę
                    Functions.buildGlasses(locations);

                    //dodanie każdego gracza do tablicy żywych
                    Player currentPlayer = Functions.onlinePlayers.get(i);
                    Parkour.alivePlayers.add(currentPlayer);

                    //usunięcie wszystkich aktywnych efektów
                    for (PotionEffect effect : currentPlayer.getActivePotionEffects())
                        currentPlayer.removePotionEffect(effect.getType());

                    currentPlayer.teleport(locations[i]);
                    currentPlayer.getInventory().clear();
                    currentPlayer.setHealth(20);

                    //dodanie saturacji, aby nie tracić głodu
                    currentPlayer.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, PotionEffect.INFINITE_DURATION, 255, false, false));
                    currentPlayer.sendTitle(ChatColor.GOLD + "PARKOUR", "z patykami", 20, 30, 10);
                }

                giveItems(); //daje każdemu patyki z knockbackiem

                Bukkit.getScheduler().runTaskLater(JavaPlugin.getPlugin(CustomCmds.class), new Runnable() {
                    @Override
                    public void run() {
                        Functions.startTimer();
                        Bukkit.getScheduler().runTaskLater(JavaPlugin.getPlugin(CustomCmds.class), new Runnable() {
                            @Override
                            public void run() {
                                //od loc 1 do loc2 ustawiamy które bloki mają zostać usunięte (usuwa wszystkie szklane klatki)
                                Location loc1 = new Location(Bukkit.getWorld("parkour"), -16, 13, 42);
                                Location loc2 = new Location(Bukkit.getWorld("parkour"), 37, 8, -20);
                                Cuboid cuboid = new Cuboid(loc1, loc2);

                                for (Block block : cuboid)
                                    block.setType(Material.AIR);

                                for (int i = 0; i < Functions.onlinePlayers.size(); i++) {
                                    Player currentPlayer = Functions.onlinePlayers.get(i);
                                    //dodanie efektu speeda dla każdego gracza
                                    currentPlayer.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, PotionEffect.INFINITE_DURATION, 0, false, false));
                                }

                                Parkour.gameIsPlaying = true;
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
    public void onMove(PlayerMoveEvent event) {

        if (Parkour.gameIsPlaying) {
            Player player = event.getPlayer();

            //jeżeli gracz spadł poza wysokość -30 to umiera
            if (player.getLocation().getY() < -30) {
                //komunikaty śmierci
                if (player.getLastDamageCause().getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK || player.getLastDamageCause().getCause() == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK)
                    Bukkit.broadcastMessage(ChatColor.RED + player.getKiller().getName() + ChatColor.GREEN + " zrzucił patykiem " + ChatColor.RED + player.getName());
                else
                    Bukkit.broadcastMessage(ChatColor.RED + player.getName() + ChatColor.GREEN + " spadł do voida");

                //gracze, którzy zginęli obserwują grę w trybie spectator
                player.setGameMode(GameMode.SPECTATOR);
                player.teleport(Bukkit.getWorld("parkour").getSpawnLocation());

                //usunięcie gracza z tablicy żywych
                Parkour.alivePlayers.remove(player);
                if (Parkour.alivePlayers.size() == 1) {
                    Parkour.gameIsPlaying = false;
                    Functions.endGame(Parkour.alivePlayers.get(0));
                }
            }
        }
    }


    public void giveItems() {

        ItemStack kij = new ItemStack(Material.STICK, 1);
        ItemMeta metaKija = kij.getItemMeta();

        metaKija.setDisplayName("kijek");
        ArrayList<String> lore = new ArrayList<String>();
        lore.add("Potężny kij mocy");
        metaKija.setLore(lore);

        metaKija.addEnchant(Enchantment.KNOCKBACK, 1, true);
        kij.setItemMeta(metaKija);

        //daje każdemu graczowi patyki
        for (int i = 0; i < Functions.onlinePlayers.size(); i++) {
            Player currentPlayer = Functions.onlinePlayers.get(i);
            currentPlayer.getInventory().addItem(kij);
        }
    }

}