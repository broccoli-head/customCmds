package luki.customcmds;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.ArrayList;
import java.util.Collections;


public class Berek implements CommandExecutor, Listener {

    int count = 0;
    static int round;
    static Player tag;
    public static BukkitTask task;
    static boolean gameIsPlaying = false;
    static ArrayList<Player> alivePlayers = new ArrayList<Player>();
    static Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
    static Team team = scoreboard.getTeam("Berek");

    //lokalizacja, gdzie gracze mają oczekiwać na start gry
    Location waitRoom = new Location(Bukkit.getWorld("statek"), 13.5, 25, 55.5, -180, 0);

    //lokalizacja, w której gracze zaczynają rozgrywkę
    Location statekSpawn = new Location(Bukkit.getWorld("statek"), 22.5, 22, 11.5, 70, 4);


    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender.isOp()) {
            if (cmd.getName().equalsIgnoreCase("berek")) {

                Functions.onlinePlayers.clear();
                Functions.onlinePlayers.addAll(Bukkit.getOnlinePlayers());
                Collections.shuffle(Functions.onlinePlayers);

                Berek.alivePlayers.clear();
                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "team empty Berek");

                for (int i = 0; i < Functions.onlinePlayers.size(); i++) {
                    //dodanie każdego gracza do tablicy żywych
                    Berek.alivePlayers.add(Functions.onlinePlayers.get(i));
                    Player currentPlayer = Functions.onlinePlayers.get(i);

                    //usunięcie wszystkich aktywnych efektów
                    for (PotionEffect effect : currentPlayer.getActivePotionEffects())
                        currentPlayer.removePotionEffect(effect.getType());

                    currentPlayer.teleport(waitRoom);
                    currentPlayer.getInventory().clear();
                    currentPlayer.setHealth(20);

                    //dodanie saturacji, aby nie tracić głodu
                    currentPlayer.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, PotionEffect.INFINITE_DURATION, 255, false, false));
                    currentPlayer.sendTitle(ChatColor.GOLD + "BEREK", ChatColor.AQUA + "(TNT tag)", 20, 30, 10);
                }


                Bukkit.getScheduler().runTaskLater(JavaPlugin.getPlugin(CustomCmds.class), new Runnable() {
                    @Override
                    public void run() {
                        Functions.startTimer();
                        Bukkit.getScheduler().runTaskLater(JavaPlugin.getPlugin(CustomCmds.class), new Runnable() {
                            @Override
                            public void run() {

                                for (int i = 0; i < Functions.onlinePlayers.size(); i++) {
                                    Player currentPlayer = Functions.onlinePlayers.get(i);
									currentPlayer.teleport(statekSpawn);
                                    //daje każdemu graczowi efekt speeda
                                    currentPlayer.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, PotionEffect.INFINITE_DURATION, 1, false, false));
                                }

                                Berek.round = 1;    //licznik rund
                                Berek.gameIsPlaying = true;
                                startRound(40);
                            }
                        }, 120);
                    }
                }, 50);

            }
        }
        else sender.sendMessage(ChatColor.RED + "Nie masz uprawnień żeby użyć tej komendy");
        return true;
    }


    public void becomeTag(Player player) {
        if (Berek.tag != null) {
            //usuwa poprzedniego berka
            Berek.team.removeEntry(Berek.tag.getName());
            Berek.tag.getInventory().clear();
        }

        Berek.tag = player;
        Berek.team.addEntry(Berek.tag.getName());

        //ustawia TNT na głowę nowego berka
        ItemStack tnt = new ItemStack(Material.TNT, 1);
        Berek.tag.getInventory().setHelmet(tnt);
        Berek.tag.getInventory().addItem(tnt);
    }


    public Player choosingRandomPlayer() {
        //losowanie gracza
        int index = (int)(Math.random() * Berek.alivePlayers.size());
        return Berek.alivePlayers.get(index);
    }


    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {
        if (Berek.gameIsPlaying) {
            Player hitPlayer = (Player) event.getEntity();
            Player damager = (Player) event.getDamager();

            //przekazanie TNT od jednego gracza do drugiego (zmiana berka)
            if (damager == Berek.tag) {
                becomeTag(hitPlayer);
                Bukkit.broadcastMessage(ChatColor.RED + damager.getName() + ChatColor.GREEN + " oddał TNT graczowi " + ChatColor.RED + hitPlayer.getDisplayName());
            }
        }
    }


    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        //zablokowanie możliwości ściągnięcia z głowy TNT
        if (Berek.gameIsPlaying) {
            if (event.getSlotType() == InventoryType.SlotType.ARMOR)
                event.setCancelled(true);
        }
    }
	
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
        if (Berek.gameIsPlaying) {
            Player player = event.getPlayer();
            Block block = player.getLocation().getBlock().getRelative(BlockFace.DOWN);

            //jeżeli gracz dotyka piasku, froglighta lub gliny - staje się berkiem
            if (block.getType() == Material.SAND || block.getType() == Material.VERDANT_FROGLIGHT || block.getType() == Material.CLAY) {
                if (player != Berek.tag) {
                    if (alivePlayers.contains(player)) {
                        becomeTag(player);
                        Bukkit.broadcastMessage(ChatColor.RED + player.getDisplayName() + ChatColor.GREEN + " wpadł do wody, więc teraz jest berkiem!");
                    }
                }
                else {
                    Bukkit.broadcastMessage(ChatColor.RED + player.getDisplayName() + ChatColor.GREEN + " wpadł do wody");
                }
                //teleportuje berka z powrotem na spawn
                player.teleport(statekSpawn);
            }
        }
	}


	public void startRound(int time) {
        Berek.round++;  //licznik rund
        becomeTag(choosingRandomPlayer());

        //odgłos rozpoczęcia gry
        Bukkit.getWorld("statek").playSound(Berek.tag.getLocation(), Sound.ENTITY_ENDER_DRAGON_AMBIENT, 1, 0);

        for (int i = 0; i < Functions.onlinePlayers.size(); i++) {
            Player currentPlayer = Functions.onlinePlayers.get(i);
            currentPlayer.teleport(statekSpawn);
        }

        //timer odliczający do końca rundy - wybucha gracz, który posiada TNT
        count = time;
		task = Bukkit.getScheduler().runTaskTimer(JavaPlugin.getPlugin(CustomCmds.class), ()-> {
            for (int i = 0; i < Functions.onlinePlayers.size(); i++) {
                Player currentPlayer = Functions.onlinePlayers.get(i);
                //każdy widzi timer nad swoim ekwipunkiem
				currentPlayer.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.GREEN + "TNT wybucha za: " + ChatColor.RED + count));
            }

            if(count == 0) {
                task.cancel();
                tntExplode();
            }
            count--;

        }, 20, 20);
	}
	
	
	public void tntExplode() {
        //dźwięk oraz particle wybuchu
        Bukkit.getWorld("statek").spawnParticle(Particle.EXPLOSION_HUGE, Berek.tag.getLocation(), 3);
        Bukkit.getWorld("statek").playSound(Berek.tag.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1, 0);

        //usunięcie berka oraz ustawienie gracza do trybu spectator
        Berek.tag.setGameMode(GameMode.SPECTATOR);
        Berek.team.removeEntry(Berek.tag.getName());
        Berek.tag.getInventory().clear();
        Berek.alivePlayers.remove(Berek.tag);

        //jeżeli pozostał tylko jeden gracz, kończy grę
        if (Berek.alivePlayers.size() == 1) {
            Berek.gameIsPlaying = false;
            Functions.endGame(Berek.alivePlayers.get(0));
        }

        //w przeciwnym razie rozpoczyna kolejną rundę
        else {
            Bukkit.getScheduler().runTaskLater(JavaPlugin.getPlugin(CustomCmds.class), new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < Functions.onlinePlayers.size(); i++) {
                        Player currentPlayer = Functions.onlinePlayers.get(i);
                        currentPlayer.sendTitle(ChatColor.RED + "Runda " + Berek.round, "", 10, 20, 10);
                    }

                    //czas trwania rund w zależności od ilości graczy
                    if (Berek.alivePlayers.size() > 5) startRound(40);
                    else if (Berek.alivePlayers.size() > 2) startRound(30);
                    else if (Berek.alivePlayers.size() == 2) startRound(20);
                }
            }, 40);
        }
	}
}