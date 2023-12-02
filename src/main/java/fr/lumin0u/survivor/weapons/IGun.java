package fr.lumin0u.survivor.weapons;

import fr.lumin0u.survivor.GameManager;
import fr.lumin0u.survivor.Survivor;
import fr.lumin0u.survivor.player.SvDamageable;
import fr.lumin0u.survivor.player.WeaponOwner;
import fr.lumin0u.survivor.utils.MCUtils;
import fr.lumin0u.survivor.utils.Ray;
import fr.lumin0u.survivor.weapons.perks.Perk;
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
	
	public static void rawShoot(WeaponOwner shooter, Weapon weapon, Ray ray, double baseDmg) {
		final int rSize = ray.getPoints().size();
		boolean fireBullet = Perk.FIRE_BULLET.testRandomDropAndHas(weapon);
		boolean explosiveBullet = Perk.EXPLOSIVE_BULLETS.testRandomDropAndHas(weapon);
		boolean critBullet = Perk.CRIT_BULLETS.testRandomDropAndHas(weapon);
		
		double dmg = critBullet ? baseDmg * 3.5 : baseDmg;
		
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
								
								if(fireBullet) {
									int red = ra.nextInt(255 - 150) + 150;
									int green = ra.nextInt(red - 150) + 150;
									color = Color.fromRGB(red, green, 75);
								}
								else if(critBullet) {
									int red = ra.nextInt(255 - 150) + 200;
									int green = ra.nextInt(red - 150) + 100;
									color = Color.fromRGB(red, green, 75);
								}
								else if(explosiveBullet) {
									color = Color.fromRGB(25, 25, 25);
								}
								else {
									color = Color.fromRGB(75, 75, 75);
								}
								
								point.getWorld().spawnParticle(Particle.REDSTONE, effectLoc, 0, new DustOptions(color, 1));
							}
							
							for(SvDamageable ent : shooter.getTargetType().getDamageables(GameManager.getInstance()))
							{
								if(ent.getBodyHitbox().contains(point) || ent.getHeadHitbox().contains(point))
								{
									ent.damage(dmg, shooter, weapon, ent.getHeadHitbox().contains(point), ray.getIncrease().normalize().multiply(0.05D));
									
									if(explosiveBullet) {
										MCUtils.explosion(shooter, weapon, dmg * 2, point, 2, 0, shooter.getTargetType());
									}
									if(fireBullet) {
										ent.setFireTime(60, shooter, weapon);
									}
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
					Location lastPoint = ray.getPoints().size() > 3 ? ray.getPoints().get(ray.getPoints().size() - 4) : ray.getStart();
					
					if(ray.getEnd() != null) {
						for(int i = 0; i < 20; ++i) {
							lastPoint.getWorld().spawnParticle(Particle.BLOCK_CRACK, lastPoint, 0, ray.getEnd().getBlockData());
						}
					}
					
					if(explosiveBullet) {
						MCUtils.explosion(shooter, weapon, dmg * 2, lastPoint, 2, 0, shooter.getTargetType());
					}
					this.cancel();
					return;
				}
			}
		}.runTaskTimer(Survivor.getInstance(), 0, 1);
	}
}
