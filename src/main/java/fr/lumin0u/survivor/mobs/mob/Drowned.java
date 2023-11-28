package fr.lumin0u.survivor.mobs.mob;

import fr.lumin0u.survivor.GameManager;
import fr.lumin0u.survivor.mobs.Waves;
import fr.lumin0u.survivor.mobs.mob.boss.CloneZombie;
import fr.lumin0u.survivor.player.SvPlayer;
import fr.lumin0u.survivor.player.WeaponOwner;
import fr.lumin0u.survivor.utils.AABB;
import fr.lumin0u.survivor.utils.TFSound;
import org.bukkit.Location;
import org.bukkit.Sound;

public class Drowned extends Zombie
{
	public Drowned(Location spawnLoc, double maxHealth, double walkSpeed) {
		super(ZombieType.DROWNED, spawnLoc, maxHealth, walkSpeed, TFSound.simple(Sound.ENTITY_DROWNED_HURT), TFSound.simple(Sound.ENTITY_DROWNED_DEATH));
	}
	
	@Override
	public void kill(WeaponOwner killer, double coinsMultiplier) {
		super.kill(killer, coinsMultiplier);
		
		getGroup().update();
		
		double health = this.maxHealth * ZombieType.BABY_DROWNED.getHealthMul();
		double speed = this.walkSpeed * ZombieType.BABY_DROWNED.getSpeedMul();
		BabyDrowned baby1 = new BabyDrowned(getEntity().getLocation(), health, speed);
		BabyDrowned baby2 = new BabyDrowned(getEntity().getLocation(), health, speed);
		getGroup().getZombies().add(baby1);
		getGroup().getZombies().add(baby2);
	}
	
	public static class BabyDrowned extends Zombie
	{
		public BabyDrowned(Location spawnLoc, double maxHealth, double walkSpeed) {
			super(ZombieType.BABY_DROWNED, spawnLoc, maxHealth, walkSpeed, TFSound.simple(Sound.ENTITY_DROWNED_HURT, 1.5f), TFSound.simple(Sound.ENTITY_DROWNED_DEATH, 1.5f));
		}
		
		@Override
		public void spawnEntity(Location spawnLoc) {
			super.spawnEntity(spawnLoc);
			((org.bukkit.entity.Drowned)this.ent).setBaby(true);
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
}
