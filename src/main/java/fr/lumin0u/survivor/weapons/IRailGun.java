package fr.lumin0u.survivor.weapons;


import fr.lumin0u.survivor.GameManager;
import fr.lumin0u.survivor.StatsManager;
import fr.lumin0u.survivor.Survivor;
import fr.lumin0u.survivor.player.SvDamageable;
import fr.lumin0u.survivor.player.SvPlayer;
import fr.lumin0u.survivor.player.WeaponOwner;
import fr.lumin0u.survivor.utils.Ray;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public interface IRailGun extends IGun
{
	public Color getRailColor();
	
	@Override
	public default void shoot() {
		
		WeaponOwner shooter = getOwner();
		Weapon weapon = (Weapon) this;
		Ray ray = new Ray(shooter.getShootLocation(), shooter.getShootLocation().getDirection().multiply(0.2D), getRange(), getAccuracy());
		double dmg = getDmg();
		
		double ballSpeed = 600.0D;
		final Random ra = new Random();
		
		new BukkitRunnable()
		{
			int i = 0;
			int lastStop = 0;
			double m = 0.0D;
			double j = 0.0D;
			final List<SvDamageable> hit = new ArrayList<>();
			
			@Override
			public void run()
			{
				for(; (double) this.i < Math.min((double) ray.getPoints().size(), 150.0D + (double) this.lastStop); ++this.i)
				{
					Location point = (Location) ray.getPoints().get(this.i);
					this.m += ra.nextBoolean() ? 0.012D : -0.012D;
					this.j += ra.nextBoolean() ? ra.nextDouble() * 2.4D : -(ra.nextDouble() * 2.4D);
					this.m = Math.abs(this.m);
					Vector x1 = (new Vector(-ray.getIncrease().normalize().getZ(), 0.0D, ray.getIncrease().normalize().getX())).normalize();
					Vector x2 = ray.getIncrease().normalize().crossProduct(x1).normalize();
					Location effectLoc = point.clone().add(x1.clone().multiply(this.m * 2.0D * Math.sin(this.j / getRange() * Math.PI * 2.0D))).add(x2.clone().multiply(this.m * 2.0D * Math.cos(this.j / getRange() * Math.PI * 2.0D)));
					if(ray.getPoints().indexOf(point) >= 2)
					{
						if((double) ra.nextInt((int) (getRange() * 5.0D)) > (double) this.i / 1.5D)
						{
							point.getWorld().spawnParticle(Particle.REDSTONE, effectLoc, 0, new DustOptions(getRailColor(), 1));
						}
						
						for(SvDamageable ent : shooter.getTargetType().getDamageables(GameManager.getInstance()))
						{
							if(ent.getBodyHitbox().contains(point) || ent.getHeadHitbox().contains(point))
							{
								if(!this.hit.contains(ent))
								{
									if(shooter instanceof SvPlayer)
										StatsManager.increaseWeaponHits(weapon);
									ent.damage(dmg, shooter, weapon, false, ray.getIncrease().normalize().multiply(0.05D));
									this.hit.add(ent);
								}
							}
						}
					}
				}
				
				if(this.i >= ray.getPoints().size() - 1)
				{
					this.cancel();
				}
				
				this.lastStop = this.i;
			}
		}.runTaskTimer(Survivor.getInstance(), 0L, 1L);
	}
}
