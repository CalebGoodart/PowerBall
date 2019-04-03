package me.fdz.powerball;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.UUID;
import java.util.logging.Handler;

public class PowerBall extends JavaPlugin implements Listener {

    private FileConfiguration config = getConfig();


    public class MyEvent extends Event {

        public Player getPlayer() {
            return player;
        }

        Player player;

        public MyEvent(Player player) {

            this.player = player;
        }

        private final HandlerList HANDLERS = new HandlerList();

        public HandlerList getHandlers() {
            return HANDLERS;
        }

        public HandlerList getHandlerList() {
            return HANDLERS;
        }
    }

    @Override
    public void onEnable() {
        super.onEnable();

        this.getCommand("startgame").setExecutor(new startGame());
        //Fired when the server enables the plugin
        config.addDefault("TrackSneaking", true);
        config.addDefault("VelocityTracking", true);
        config.addDefault("PlayerLaunch", true);
        config.addDefault("PlayerFastFall", true);
        config.addDefault("PlayerBounce", true);
        config.addDefault("PlayerDash", true);
        config.options().copyDefaults(true);
        saveConfig();

        // Enable our class to check for new players using onPlayerJoin()
        getServer().getPluginManager().registerEvents(this, this);
    }//Ends OnEnable

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();

        event.setJoinMessage("Welcome, " + player.getName() + "to Server2!");

        this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            public void run() {
                Bukkit.getPluginManager().callEvent(new MyEvent(player));
            }
        }, 0, 1);


    }//Ends onPlayerJoin

    @EventHandler
    public void onPlayerSneaking(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();

        if (!((config.getBoolean("TrackSneaking"))) && (player.isSneaking())) {
            player.sendMessage("You are Sneaking");
        }
        if (!(config.getBoolean("VelocityTracking")) && (player.isSneaking())) {
            player.sendMessage(String.valueOf((player.getVelocity())));
        }
    }//Ends onPlayerSneaking

    @EventHandler
    public void playerLaunch(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();

        if (player.isSneaking() && player.isOnGround() && (player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() == Material.DIRT) && config.getBoolean("PlayerLaunch")) {
            player.setVelocity(new Vector(player.getVelocity().getX(), 5, player.getVelocity().getZ()));
        }
    }//End of playerLaunch

    @EventHandler
    public void playerFastFall(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();

        if (player.isSneaking() && !(player.isOnGround()) && ((player.getVelocity().getY() <= 0) && (config.getBoolean("PlayerFastFall")))) {
            player.setVelocity(new Vector(player.getVelocity().getX(), -5, player.getVelocity().getZ()));
        }
    }//End of playerFastFall

    @EventHandler
    public void playerBounce(MyEvent event) {
        Player player = event.getPlayer();

        if (player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() == Material.DIRT && player.getVelocity().getY() > .6) {
            player.sendMessage("on dirt");
            Vector c = new Vector(player.getVelocity().getX(), 3, player.getVelocity().getZ());
            player.setVelocity(c);
        }



/*
        if ((player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() == Material.DIRT) && (a.getFrom().getY() - a.getTo().getY() > .6) && config.getBoolean("PlayerBounce")) {

        }


        player.sendMessage("moved");
        getServer().getScheduler().runTaskLater(this, new Runnable() {
            public void run() {
                if (!(config.getBoolean("VelocityTracking"))) {
                    player.sendMessage(String.valueOf((player.getVelocity())));
                }


            }
        }, 5L);
        */


    }// End of playerBounce

    @EventHandler
    public void playerDash(PlayerInteractEvent event) {
        final Player player = event.getPlayer();

        if (event.getAction() == Action.LEFT_CLICK_AIR && player.getInventory().getItemInMainHand().getType() == Material.ARROW) {

            if (playersOnDashCoolDown.contains(player)) {

                player.sendMessage("On CoolDown!");

            } else {

                Vector dashSpeed = new Vector(player.getLocation().getDirection().getX() * 3,
                        player.getLocation().getDirection().getY(),
                        player.getLocation().getDirection().getZ() * 3);
                player.setVelocity(dashSpeed);
                playersOnDashCoolDown.put(player.getUniqueId(), player);

                new BukkitRunnable() {
                    public void run() {
                        playersOnDashCoolDown.remove(player.getUniqueId());
                    }
                }.runTaskLater(this, 3);

            }

        }
    }

    private Hashtable<UUID, Player> playersOnDashCoolDown = new Hashtable<UUID, Player>();

    @EventHandler
    public void onPlayerSquat(PlayerToggleSneakEvent event) {

        Player player = event.getPlayer();
        //player.sendMessage("Code executed");
        if (player.isSneaking()) {

            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_AMBIENT, (float) (Math.random() * 10), (float) (Math.random() * 10));

        }
    }


    public void StartGame() {


        new BukkitRunnable() {

            int counter = 10;

            public void run() {

                if (counter <= 0) {

                    getServer().broadcastMessage("starting game");
                    this.cancel();

                } else {
                    getServer().broadcastMessage(counter + " Seconds left!");
                    counter--;
                }
            }
        }.runTaskTimer(this, 0, 20);

        this.getServer().broadcastMessage("Game is staring!");

        //spawn player and give flash

        giveKit();

        // wait for win condition

    }

    public void giveKit() {


    }

    public void snowBallHit(EntityDamageByEntityEvent event) {

        event.getEntity().sendMessage("HIT");
        event.getDamager().sendMessage("Hitted");
    }

    public class startGame implements CommandExecutor {


        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            if (sender instanceof Player) {
                StartGame();
            }

            // If the player (or console) uses our command correct, we can return true
            return true;
        }


    }


    @Override
    public void onDisable() {
        super.onDisable();
    }//Ends onDisable
}//Ends PowerBall
