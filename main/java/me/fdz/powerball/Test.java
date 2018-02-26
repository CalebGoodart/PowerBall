package me.fdz.powerball;

import org.bukkit.entity.Arrow;

public class Test extends Arrow{

    public int getKnockbackStrength() {
        return 0;
    }

    public PickupStatus getPickupStatus() {
        return null;
    }

    public boolean isCritical() {
        return false;
    }

    public void setCritical(boolean critical) {

    }

    public void setKnockbackStrength(int knockbackStrength) {

    }

    public void setPickupStatus(PickupStatus status) {

    }

    public Spigot spigot() {
        return null;
    }

}
