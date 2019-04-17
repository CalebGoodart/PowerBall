package me.fdz.powerball;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import java.util.Hashtable;
import java.util.UUID;

public class PowerBall extends JavaPlugin implements Listener{

    private FileConfiguration config = getConfig();
    private Hashtable <UUID, Integer> playerKills = new Hashtable<UUID, Integer>();
    private Hashtable<UUID, Player> playersOnDashCoolDown = new Hashtable<UUID, Player>();
    private boolean gameRunning = false;


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

        //makes the player boucne
        this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            public void run() {

                if (player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() == Material.BROWN_MUSHROOM_BLOCK
                        && player.getVelocity().getY() < 0) {

                    Vector c = new Vector(player.getVelocity().getX(), 3, player.getVelocity().getZ());
                    player.setVelocity(c);
                }

            }
        }, 0L, 1);


    }//Ends onPlayerJoin

    @EventHandler
    public void playerLaunch(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();

        if (player.isSneaking() && player.isOnGround() && (player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() == Material.BROWN_MUSHROOM_BLOCK) && config.getBoolean("PlayerLaunch")) {
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
    public void playerDash(PlayerInteractEvent event) {
        final Player player = event.getPlayer();

        if (event.getAction() == Action.LEFT_CLICK_AIR && player.getInventory().getItemInMainHand().getType() == Material.SNOWBALL) {

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
                }.runTaskLater(this, 3 * 20L);

            }

        }
    }

    @EventHandler
    public void fart(PlayerToggleSneakEvent event) {

        Player player = event.getPlayer();
        //player.sendMessage("Code executed");
        if (player.isSneaking()) {

            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_AMBIENT, (float) (Math.random() * 10), (float) (Math.random() * 10));

        }
    }


    public void StartGame() {
        gameRunning = true;

        int counter = 10;
        long temp = System.currentTimeMillis();

        while (counter > 0){

            if (System.currentTimeMillis() - temp == 1000){
                temp = System.currentTimeMillis();
                getServer().broadcastMessage(counter + " Seconds left!");
                counter--;
            }
        }

        this.getServer().broadcastMessage("Game is staring!");

        //spawn player and give flash

        for (Player player : this.getServer().getOnlinePlayers()){

            spawn(player);

        }
    }

    public void giveKit(Player player) {
        ItemStack p = new ItemStack(Material.SPLASH_POTION, 2);
        PotionMeta m = (PotionMeta) p.getItemMeta();
        m.addCustomEffect(new PotionEffect(PotionEffectType.SPEED, 1000 * 128, 10), true);
        p.setItemMeta(m);
        player.getInventory().setItem(1, p);
    }

    @EventHandler
    public void snowBallHit(ProjectileHitEvent event) {

        if (event.getEntity() instanceof Snowball && gameRunning){
            if (event.getEntity().getShooter() instanceof Player){
                if (event.getHitEntity() != null) {
                    Player killer = ((Player) event.getEntity().getShooter()).getPlayer();
                    Entity killed = event.getHitEntity();

                    if (playerKills.get(killer.getUniqueId()) == null){
                        playerKills.put(killer.getUniqueId(), 0);
                    }
                    playerKills.put(killer.getUniqueId(), playerKills.get(killer.getUniqueId()) + 1);
                    killer.sendMessage("Killed " + killed.getName());
                    if (playerKills.get(killer.getUniqueId()) == 10) {
                        endGame(killer);
                    }
                    if (killed instanceof Player) {
                        spawn((Player) killed);
                    } else {
                        killed.remove();
                    }
                }
            }
        }
    }

    public void spawn(Player player){
        if (player == null) return;
        player.getInventory().clear();
        player.getInventory().setItem(0, new ItemStack(Material.SNOWBALL, 64));
        giveKit(player);
        player.teleport(new Location(player.getWorld(), 0, 100 ,0));
    }

    @EventHandler
    public void falldamage(EntityDamageEvent event){

        if (event.getCause().equals(EntityDamageEvent.DamageCause.FALL)){
            event.setCancelled(true);
        }
    }

    public void endGame(Player winner){

        playerKills.clear();
        this.getServer().broadcastMessage(winner.getDisplayName() + " Has won!");

        for (Player player : this.getServer().getOnlinePlayers()){

            player.getInventory().clear();
            player.teleport(new Location(player.getWorld(), 0, 100, 0));
        }
        gameRunning = false;
    }

    public class startGame implements CommandExecutor {

        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

            StartGame();
            return true;
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }//Ends onDisable
}//Ends PowerBall


