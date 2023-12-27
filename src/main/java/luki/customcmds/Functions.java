package luki.customcmds;

import org.bukkit.*;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;

public class Functions implements Listener {

    static ArrayList<Player> onlinePlayers = new ArrayList<Player>();
    public static BukkitTask task;
    public static int count = 5;
	
	
    public static void startTimer() {
        count = 5;  //odliczanie 5 sekund do rozpoczęcia gry

        task = Bukkit.getScheduler().runTaskTimer(JavaPlugin.getPlugin(CustomCmds.class), ()-> {
            for (int i = 0; i < onlinePlayers.size(); i++) {
                Player currentPlayer = (Player)onlinePlayers.get(i);
                currentPlayer.sendTitle(ChatColor.RED + String.valueOf(count), ChatColor.YELLOW + "Gra zaczyna się za...", 0, 20, 0);

                if(count == 0) {
                    currentPlayer.resetTitle();
                    currentPlayer.sendTitle(ChatColor.GREEN + "START!", "", 0, 40, 10);
                    task.cancel();
                }
            }
            count--;

        }, 20, 20);
    }


    //funkcja do budowania szklanych klatek wokół graczy
    public static void buildGlasses(Location[] locations) {
        for (int i = 0; i < onlinePlayers.size(); i++) {

            Location loc = locations[i];
            World world = Bukkit.getWorld("parkour");
            double x = loc.getX();
            double y = loc.getY();
            double z = loc.getZ();

            //obudowuje gracza z każdej strony, tak aby nie mógł wyjść
            Location[] glassLocations = {
                    new Location(world, x, y-1, z),
                    new Location(world,x+1, y, z),
                    new Location(world, x-1, y, z),
                    new Location(world, x, y, z+1),
                    new Location(world, x, y, z-1),
                    new Location(world, x+1, y+1, z),
                    new Location(world, x-1, y+1, z),
                    new Location(world, x, y+1, z+1),
                    new Location(world, x, y+1, z-1),
                    new Location(world, x+1, y+2, z),
                    new Location(world, x-1, y+2, z),
                    new Location(world, x, y+2, z+1),
                    new Location(world, x, y+2, z-1),
            };

            for (int j = 0; j < glassLocations.length; j++) {
                glassLocations[j].getBlock().setType(Material.GLASS);
            }

        }
    }

    public static void endGame(Player winner) {
        Bukkit.getScheduler().runTaskLater(JavaPlugin.getPlugin(CustomCmds.class), new Runnable() {
            @Override
            public void run() {
                //komunikat o wygranym graczu
                Bukkit.broadcastMessage("\n\n" + ChatColor.YELLOW + "=" + ChatColor.AQUA + "=" + ChatColor.YELLOW + "=" + ChatColor.AQUA + "=" + ChatColor.YELLOW + "=" + ChatColor.AQUA + "=" + ChatColor.YELLOW + "="  + ChatColor.AQUA + "=" + ChatColor.YELLOW + "=" + ChatColor.AQUA + "=" + ChatColor.YELLOW + "=" + ChatColor.AQUA + "=" + ChatColor.YELLOW + "=" + ChatColor.AQUA + "=" + ChatColor.YELLOW + "=" + ChatColor.AQUA + "=" + ChatColor.YELLOW + "=" + ChatColor.AQUA + "=" + ChatColor.YELLOW + "=" + ChatColor.AQUA + "=" + ChatColor.YELLOW + "="  + ChatColor.AQUA + "=" + ChatColor.YELLOW + "=" + ChatColor.AQUA + "=" + ChatColor.YELLOW + "=");
                Bukkit.broadcastMessage("\n\n" + ChatColor.RED + "   " + ChatColor.BOLD + winner.getName().toUpperCase() + " WYGRYWA!!!");
                Bukkit.broadcastMessage("\n\n" + ChatColor.YELLOW + "=" + ChatColor.AQUA + "=" + ChatColor.YELLOW + "=" + ChatColor.AQUA + "=" + ChatColor.YELLOW + "=" + ChatColor.AQUA + "=" + ChatColor.YELLOW + "="  + ChatColor.AQUA + "=" + ChatColor.YELLOW + "=" + ChatColor.AQUA + "=" + ChatColor.YELLOW + "=" + ChatColor.AQUA + "=" + ChatColor.YELLOW + "=" + ChatColor.AQUA + "=" + ChatColor.YELLOW + "=" + ChatColor.AQUA + "=" + ChatColor.YELLOW + "=" + ChatColor.AQUA + "=" + ChatColor.YELLOW + "=" + ChatColor.AQUA + "=" + ChatColor.YELLOW + "="  + ChatColor.AQUA + "=" + ChatColor.YELLOW + "=" + ChatColor.AQUA + "=" + ChatColor.YELLOW + "=" + "\n\n ");
            }
        }, 5);


		count = 0;
		task = Bukkit.getScheduler().runTaskTimer(JavaPlugin.getPlugin(CustomCmds.class), ()-> {
            //5 sekund celebracji wygranej
            if(count == 5) {
                task.cancel();
            }

            //fajerwerki pojawiające się tam, gdzie wygrany gracz chodzi
            Firework firework = winner.getWorld().spawn(winner.getLocation(), Firework.class);
            FireworkMeta data = firework.getFireworkMeta();
            data.addEffect(FireworkEffect.builder().withColor(Color.RED).withColor(Color.GREEN).with(FireworkEffect.Type.BALL_LARGE).withFlicker().build());
            data.setPower(1);
            firework.setFireworkMeta(data);

            count++;

        }, 20, 20);
		

        //po skończonej grze, resetuje efekty, życie oraz ekwipunki graczy
        Bukkit.getScheduler().runTaskLater(JavaPlugin.getPlugin(CustomCmds.class), new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < Functions.onlinePlayers.size(); i++) {
                    Player currentPlayer = Functions.onlinePlayers.get(i);

                    for (PotionEffect effect : currentPlayer.getActivePotionEffects())
                        currentPlayer.removePotionEffect(effect.getType());

                    currentPlayer.getInventory().clear();
                    currentPlayer.setHealth(20);
                    //teleportowanie graczy do lobby
                    currentPlayer.teleport(Bukkit.getWorld("lobby").getSpawnLocation());
                }
            }
        }, 140);
    }

}
