package fr.lumin0u.survivor.mobs.mob.boss;

import fr.lumin0u.survivor.mobs.mob.zombies.Zombie;
import fr.lumin0u.survivor.mobs.mob.zombies.ZombieType;
import fr.lumin0u.survivor.utils.TFSound;
import org.bukkit.Location;
import org.bukkit.Sound;

public class SkeletonClone extends Zombie
{
    public SkeletonClone(Location spawnLoc, double maxHealth, double walkSpeed) {
        super(ZombieType.SKELETON, spawnLoc, maxHealth, walkSpeed, TFSound.simple(Sound.ENTITY_SKELETON_HURT), TFSound.simple(Sound.ENTITY_SKELETON_DEATH));
    }
}
