package fr.lumin0u.survivor.weapons;

import fr.lumin0u.survivor.GameManager;
import fr.lumin0u.survivor.StatsManager;
import fr.lumin0u.survivor.Survivor;
import fr.lumin0u.survivor.mobs.mob.Enemy;
import fr.lumin0u.survivor.player.SvDamageable;
import fr.lumin0u.survivor.player.SvPlayer;
import fr.lumin0u.survivor.player.WeaponOwner;
import fr.lumin0u.survivor.utils.Ray;
import fr.lumin0u.survivor.weapons.perks.Perk;
import fr.lumin0u.survivor.weapons.superweapons.Turret;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Random;

public interface IGun extends IWeapon
{
	public double getDmg();
	
	public double getRange();
	
	public double getAccuracy();
	
	public default void shoot() {
		
		WeaponOwner shooter = getOwner();
		Ray ray = new Ray(shooter.getShootLocation(), shooter.getShootLocation().getDirection().multiply(0.2D), getRange(), getAccuracy());
		
		rawShoot(shooter, (Weapon) this, ray, getDmg());
	}
	
	public static void rawShoot(WeaponOwner shooter, Weapon weapon, Ray ray, double dmg) {
		final int rSize = ray.getPoints().size();
		new BukkitRunnable()
		{
			int i = 0;
			int lastStop = 0;
			double m = 0.0D;
			double j = 0.0D;
			Random ra = new Random();
			
			@Override
			public void run()
			{
				for(; (double) this.i < Math.min((double) ray.getPoints().size(), 50.0D + (double) this.lastStop); ++this.i)
				{
					Location point = (Location) ray.getPoints().get(this.i);
					this.m += this.ra.nextBoolean() ? 0.012D : -0.012D;
					this.j += this.ra.nextBoolean() ? this.ra.nextDouble() * 2.4D : -(this.ra.nextDouble() * 2.4D);
					this.m = Math.abs(this.m);
					Vector x1 = (new Vector(-ray.getIncrease().normalize().getZ(), 0.0D, ray.getIncrease().normalize().getX())).normalize();
					Vector x2 = ray.getIncrease().normalize().crossProduct(x1).normalize();
					Location effectLoc = point.clone().add(x1.clone().multiply(this.m * Math.sin(this.j / ray.getLength() * Math.PI * 2.0D))).add(x2.clone().multiply(this.m * Math.cos(this.j / ray.getLength() * Math.PI * 2.0D)));
					if(ray.getPoints().indexOf(point) >= 2)
					{
						if(ray.getPoints().indexOf(point) == 2)
						{
							point.getWorld().spawnParticle(Particle.FLAME, effectLoc, 0);
						}
						else
						{
							if((double) this.ra.nextInt((int) (ray.getLength() * 5.0D)) > (double) this.i / 1.5D)
							{
								Color color;
								if(weapon.hasPerk(Perk.FIRE_BULLET))
									color = Color.fromRGB(ra.nextInt(ra.nextInt(50)) + 75, 75, 75);
								else
									color = Color.fromRGB(75, 75, 75);
								point.getWorld().spawnParticle(Particle.REDSTONE, effectLoc, 0, new DustOptions(color, 1));
							}
							
							for(SvDamageable ent : shooter.getTargetType().getDamageables(GameManager.getInstance()))
							{
								if(ent.getBodyHitbox().contains(point) || ent.getHeadHitbox().contains(point))
								{
									if(shooter instanceof SvPlayer)
									{
										StatsManager.increaseWeaponHits(weapon);
									}
									
									if(ent instanceof Enemy)
										((Enemy)ent).damage(dmg, shooter, weapon, ent.getHeadHitbox().contains(point), ray.getIncrease().normalize().multiply(0.05D), weapon instanceof Turret ? 0.7D : 1.0D);
									else
										ent.damage(dmg, shooter, weapon, ent.getHeadHitbox().contains(point), ray.getIncrease().normalize().multiply(0.05D));
									this.cancel();
									return;
								}
							}
						}
					}
				}
				
				if(this.i < rSize - 1)
				{
					this.lastStop = this.i;
				}
				else
				{
					if(ray.getEnd() != null && ray.getPoints().size() > 3)
					{
						for(int i = 0; i < 20; ++i)
						{
							ray.getPoints().get(0).getWorld().spawnParticle(Particle.BLOCK_CRACK, ray.getPoints().get(ray.getPoints().size() - 4), 0, ray.getEnd().getBlockData());
						}
					}
					
					this.cancel();
				}
			}
		}.runTaskTimer(Survivor.getInstance(), 0, 1);
	}
}
