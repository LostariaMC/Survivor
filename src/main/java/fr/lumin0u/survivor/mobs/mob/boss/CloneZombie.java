package fr.lumin0u.survivor.mobs.mob.boss;

import fr.lumin0u.survivor.mobs.mob.zombies.Zombie;
import fr.lumin0u.survivor.mobs.mob.zombies.ZombieType;
import org.bukkit.Location;

public class CloneZombie extends Zombie
{
    public CloneZombie(Location spawnLoc, double maxHealth, double walkSpeed) {
        super(ZombieType.SKELETON, spawnLoc, maxHealth, walkSpeed);
    }
}
