package fr.lumin0u.survivor.mobs.mob.zombies;

import fr.lumin0u.survivor.Survivor;
import fr.lumin0u.survivor.mobs.mob.EnemyWeaponAI;
import fr.lumin0u.survivor.weapons.guns.GrapplingHook;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class ZombieGrappler extends Zombie
{
	public ZombieGrappler(Location spawnLoc, double maxHealth, double walkSpeed)
	{
		super(ZombieType.GRAPPLER, spawnLoc, maxHealth, walkSpeed);
		
		giveWeapon(new GrapplingHook(this));
		ai = new EnemyWeaponAI(weapon, true);
		
		(new BukkitRunnable()
		{
			Vector increase;
			
			@Override
			public void run()
			{
				org.bukkit.entity.Zombie zomb = (org.bukkit.entity.Zombie) ZombieGrappler.this.ent;
				if(ZombieGrappler.this.dead)
				{
					this.cancel();
				}
				
				if(!zomb.isDead() && ZombieGrappler.this.target != null)
				{
					ai.run();
				}
			}
		}).runTaskTimer(Survivor.getInstance(), 2L, 1L);
	}
	
	@Override
	public void setArmor()
	{
		super.setArmor();
	}
}
