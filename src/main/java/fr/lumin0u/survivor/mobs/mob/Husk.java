package fr.lumin0u.survivor.mobs.mob;

import fr.lumin0u.survivor.utils.AABB;
import fr.lumin0u.survivor.utils.TFSound;
import org.bukkit.Location;

public class Husk extends Zombie {
	public Husk(Location spawnLoc, double maxHealth, double walkSpeed) {
		super(ZombieType.HUSK, spawnLoc, maxHealth, walkSpeed, TFSound.HUSK_HURT, TFSound.HUSK_DEATH);
	}
	
	@Override
	public AABB getBodyHitbox() {
		return new AABB(this.ent.getLocation().clone().add(-0.48D, 0.0D, -0.48D), this.ent.getLocation().clone().add(0.48D, 1.7D, 0.48D));
	}
	
	@Override
	public AABB getHeadHitbox() {
		return new AABB(this.ent.getLocation().clone().add(-0.4D, 1.6D, -0.4D), this.ent.getLocation().clone().add(0.4D, 2.1D, 0.4D));
	}
}