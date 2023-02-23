package fr.lumin0u.survivor.mobs.mob;

import fr.lumin0u.survivor.utils.AABB;
import org.bukkit.Location;

public class BabyZombie extends Zombie {
    public BabyZombie(Location spawnLoc, double maxHealth, double walkSpeed) {
        super(spawnLoc, maxHealth, walkSpeed);
    }

    @Override
	public void spawnEntity(Location spawnLoc) {
        super.spawnEntity(spawnLoc);
        ((org.bukkit.entity.Zombie)this.ent).setBaby(true);
    }

    @Override
    public AABB getBodyHitbox() {
        return new AABB(this.ent.getLocation().clone().add(-0.3D, 0.0D, -0.3D), this.ent.getLocation().clone().add(0.3D, 0.8D, 0.3D));
    }

    @Override
    public AABB getHeadHitbox() {
        return new AABB(this.ent.getLocation().clone().add(-0.26D, 0.8D, -0.26D), this.ent.getLocation().clone().add(0.26D, 1.4D, 0.26D));
    }
}
