package me.fdz.powerball;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.*;
import org.bukkit.material.Mushroom;
import  org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.Vector;
import org.bukkit.util.*;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class PowerBall extends JavaPlugin implements Listener{

    private FileConfiguration config = getConfig();

    @Override
    public void onEnable() {
        super.onEnable();

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
        Player player = event.getPlayer();

        event.setJoinMessage("Welcome, " + player.getName() + "to Server2!");
    }//Ends onPlayerJoin

    @EventHandler
    public void onPlayerSneaking(PlayerToggleSneakEvent event){
        Player player = event.getPlayer();

        if(!((config.getBoolean("TrackSneaking"))) && (player.isSneaking())) {
            player.sendMessage("You are Sneaking");
        }
        if(!(config.getBoolean("VelocityTracking")) && (player.isSneaking())){
            player.sendMessage(String.valueOf((player.getVelocity())));
        }
    }//Ends onPlayerSneaking

    @EventHandler
    public void playerLaunch(PlayerToggleSneakEvent event){
        Player player = event.getPlayer();

        if( player.isSneaking() && player.isOnGround() && (player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() == Material.HUGE_MUSHROOM_1) && config.getBoolean("PlayerLaunch")){
            player.setVelocity(new Vector(player.getVelocity().getX(), 5, player.getVelocity().getZ()));
        }
    }//End of playerLaunch

    @EventHandler
    public void playerFastFall(PlayerToggleSneakEvent event){
        Player player = event.getPlayer();

        if( player.isSneaking() && !(player.isOnGround()) && ((player.getVelocity().getY() <= 0) && (config.getBoolean("PlayerFastFall")))){
            player.setVelocity(new Vector(player.getVelocity().getX(), -5, player.getVelocity().getZ()));
        }
    }//End of playerFastFall

    @EventHandler
    public void playerBounce(PlayerMoveEvent event){
        final Player player = event.getPlayer();
        final PlayerMoveEvent a = event;

        getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
            public void run() {
                if (!(config.getBoolean("VelocityTracking"))) {
                    player.sendMessage(String.valueOf((player.getVelocity())));
                }

                if ((player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() == Material.HUGE_MUSHROOM_1) && (a.getFrom().getY() - a.getTo().getY() > .6 ) && config.getBoolean("PlayerBounce")) {
                    Vector c = new Vector(player.getVelocity().getX(), 3, player.getVelocity().getZ());
                    player.setVelocity(c);
                }
            }
        }, 5L);
    }// End of playerBounce

    @EventHandler
    public void playerDash(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if ( !( playersOnDashCooldown.contains(player) ) && !(player.isOnGround()) && (event.getAction() == Action.LEFT_CLICK_AIR) && (player.getInventory().getItemInMainHand().getType() == Material.SNOW_BALL) && config.getBoolean("PlayerDash")) {

            Vector dashSpeed =new Vector(player.getLocation().getDirection().getX() * 3, player.getLocation().getDirection().getY(), player.getLocation().getDirection().getZ() * 3 );
            player.setVelocity(dashSpeed);
            playersOnDashCooldown.add(player);
            dashCoolDown(player, 2);
        }
    }

    private ArrayList <Player> playersOnDashCooldown = new ArrayList<Player>();

    private void dashCoolDown(final Player player, int seconds){

            this.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
                public void run() {
                    playersOnDashCooldown.remove(player);
                }
            }, seconds * 20L);
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }//Ends onDisable
}//Ends PowerBall
