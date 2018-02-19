package me.fdz.powerball;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.util.Vector;

public class Test extends PlayerToggleSneakEvent{
    public Test(Player player, boolean isSneaking) {
        super(player, isSneaking);

        if( player.isSneaking() && player.isOnGround() && (player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType().equals(Material.CLAY) )){
            player.setVelocity(new Vector(player.getVelocity().getX(), 5, player.getVelocity().getZ()));
        }
    }
}
